package com.cooksync_server.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.config.JwtUtil;
import com.dtos.request.auth.ChangePasswordRequestDTO;
import com.dtos.request.auth.DeleteAccountRequestDTO;
import com.dtos.request.auth.EmailUpdateRequestDTO;
import com.dtos.request.auth.ForgotPasswordRequestDTO;
import com.dtos.request.auth.LoginRequestDTO;
import com.dtos.request.auth.PrivacySettingsUpdateRequestDTO;
import com.dtos.request.auth.ProfileUpdateRequestDTO;
import com.dtos.request.auth.RegisterRequestDTO;
import com.dtos.request.auth.ResetPasswordRequestDTO;
import com.dtos.request.auth.TokenRefreshRequestDTO;
import com.dtos.response.auth.AuthResponse;
import com.dtos.response.user.UserResponse;
import com.cooksync_server.entities.PasswordResetToken;
import com.cooksync_server.entities.RefreshToken;
import com.cooksync_server.entities.User;
import com.cooksync_server.exceptions.ResourceNotFoundException;
import com.cooksync_server.mappers.UserMapper;
import com.cooksync_server.exceptions.auth.InvalidCredentialsException;
import com.cooksync_server.exceptions.auth.UnauthorizedActionException;
import com.cooksync_server.exceptions.auth.UserAlreadyExistsException;
import com.cooksync_server.repositories.PasswordResetTokenRepository;
import com.cooksync_server.repositories.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class handling user authentication, registration, token refresh, password changes, and account settings.
 * Includes SLF4J structured logging for monitoring security events.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Slf4j
@Service
public class AuthService implements IAuthService{

    /** How long a forgot-password reset token remains valid after being issued. */
    private static final long RESET_TOKEN_VALIDITY_MS = 30 * 60 * 1000L;

    /** Grace period after a deletion request during which logging back in restores the account. */
    private static final long DELETION_GRACE_PERIOD_DAYS = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final IAccountDeletionService accountDeletionService;
    private final String dummyPasswordHash;

    /**
     * Constructs AuthService with required dependencies and initializes timing attack dummy hash.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userRepository repository for user persistence
     * @param passwordEncoder encoder for BCrypt password hashing
     * @param jwtUtil utility for JWT generation and verification
     * @param refreshTokenService service for managing session refresh tokens
     * @param passwordResetTokenRepository repository for forgot-password reset tokens
     * @param emailService service used to deliver password-reset emails
     * @param accountDeletionService service handling the self-service account-deletion lifecycle
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService, PasswordResetTokenRepository passwordResetTokenRepository,
            EmailService emailService, IAccountDeletionService accountDeletionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.accountDeletionService = accountDeletionService;
        this.dummyPasswordHash = passwordEncoder.encode("dummy_password_for_timing_attack_prevention");
    }

    /**
     * Registers a new user account with encoded password and issues initial access and refresh tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request registration details payload
     * @return AuthResponse containing access token, refresh token, and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequestDTO request) {
        log.info("Processing user registration attempt for email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected - email already exists: {}", request.email());
            throw new UserAlreadyExistsException("Email is already registered");
        }

        User newUser = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .isAdmin(false)
                .termsAccepted(request.termsAccepted())
                .marketingOptIn(request.marketingOptIn())
                .build();

        try {
            userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            log.warn("DataIntegrityViolation during registration for email: {}", request.email());
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getId(), newUser.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(newUser.getId());

        log.info("User registered successfully with ID: {}", newUser.getId());
        return new AuthResponse(token, refreshToken.getToken(), newUser.getId(), newUser.getFirstName(), newUser.getLastName(), newUser.isAdmin(), newUser.getAvatarUrl());
    }

    /**
     * Authenticates user credentials with constant-time password comparison to prevent timing attacks.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request login credentials payload
     * @return AuthResponse containing fresh tokens and user info
     */
    @Transactional
    public AuthResponse login(LoginRequestDTO request) {
        log.info("Processing user login attempt for email: {}", request.email());
        Optional<User> optionalUser = userRepository.findByEmail(request.email());

        String hashToTest = optionalUser.map(User::getPasswordHash).orElse(this.dummyPasswordHash);
        boolean isPasswordMatch = passwordEncoder.matches(request.password(), hashToTest);

        if (optionalUser.isEmpty() || !isPasswordMatch) {
            log.warn("Login failed - invalid credentials for email: {}", request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = optionalUser.get();
        if (!user.isEnabled()) {
            if (isWithinDeletionGracePeriod(user)) {
                log.info("Login during deletion grace period - restoring account ID: {}", user.getId());
                accountDeletionService.restoreFromPendingDeletion(user);
            } else {
                log.warn("Login failed - account disabled for user ID: {}", user.getId());
                throw new UnauthorizedActionException("This account has been disabled.");
            }
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info("User logged in successfully with ID: {}", user.getId());
        return new AuthResponse(token, refreshToken.getToken(), user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
    }

    /**
     * Renews access token using a valid refresh token payload.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request refresh token request payload
     * @return AuthResponse containing new access token and existing refresh token
     */
    @Transactional
    public AuthResponse refreshToken(TokenRefreshRequestDTO request) {
        String requestRefreshToken = request.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
                    return new AuthResponse(token, requestRefreshToken, user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
                })
                .orElseThrow(() -> new UnauthorizedActionException("Refresh token is not in database or is invalid!"));
    }

    /**
     * Validates active JWT token context and returns user profile details without issuing new tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail authenticated user email
     * @return AuthResponse with profile details
     */
    @Transactional(readOnly = true)
    public AuthResponse validateToken(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        return new AuthResponse(null, null, user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
    }

    /**
     * Fetches the authenticated user's full profile, including fields not carried by
     * {@link AuthResponse} (city, bio, privacy preferences).
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail authenticated user email
     * @return the user's full profile
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        return UserMapper.toResponse(user);
    }

    /**
     * Revokes active user refresh tokens upon logout.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail authenticated user email
     */
    @Transactional
    public void logout(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        refreshTokenService.deleteByUserId(user.getId());
    }

    /**
     * Updates user avatar picture URL.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param avatarUrl new profile picture URL
     */
    @Transactional
    public void updateAvatar(String userEmail, String avatarUrl) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    /**
     * Updates user first name, last name, city, and bio profile details.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param request profile update request DTO
     */
    @Transactional
    public void updateProfile(String userEmail, ProfileUpdateRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setCity(request.city());
        user.setBio(request.bio());
        userRepository.save(user);
    }

    /**
     * Updates the user's public-profile privacy preferences.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param request privacy settings update request DTO
     */
    @Transactional
    public void updatePrivacySettings(String userEmail, PrivacySettingsUpdateRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setShowRecipesPublicly(request.showRecipesPublicly());
        user.setShowFavoritesPublicly(request.showFavoritesPublicly());
        userRepository.save(user);
    }

    /**
     * Changes user account password following verification of current password, revoking existing sessions.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param request password change request DTO
     */
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }

    /**
     * Updates user account email address following password verification and issues updated tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail current authenticated user email
     * @param request email update request DTO
     * @return AuthResponse containing updated tokens reflecting new email address
     */
    @Transactional
    public AuthResponse updateEmail(String userEmail, EmailUpdateRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        String newEmail = request.newEmail().trim().toLowerCase();
        if (!newEmail.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        user.setEmail(newEmail);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.isAdmin());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
        return new AuthResponse(token, refreshToken.getToken(), user.getId(), user.getFirstName(), user.getLastName(), user.isAdmin(), user.getAvatarUrl());
    }

    /**
     * Deactivates user account (soft delete) and revokes active refresh tokens.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     */
    @Transactional
    public void deactivateAccount(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));
        user.setEnabled(false);
        user.setStatus(User.AccountStatus.DEACTIVATED);
        userRepository.save(user);
        refreshTokenService.deleteByUserId(user.getId());
    }

    /**
     * Starts the 30-day self-service account-deletion grace period following password
     * verification. Distinct from {@link #deactivateAccount(String)}: this also hides the
     * user's reviews and starts the countdown to permanent purge; a plain deactivation does
     * neither. Logging back in within the grace period restores the account via
     * {@link #login(LoginRequestDTO)}.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param userEmail target user email
     * @param request delete-account request DTO carrying the current password for verification
     */
    @Transactional
    public void requestAccountDeletion(String userEmail, DeleteAccountRequestDTO request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", userEmail));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        accountDeletionService.requestDeletion(user);
    }

    /**
     * Determines whether a disabled account is still within its 30-day account-deletion grace
     * period and therefore eligible to be restored by logging back in, as opposed to a plain
     * deactivation (never self-service restorable) or an already-lapsed deletion request (the
     * scheduled purge job should have already erased it, but login is rejected defensively
     * either way since {@link #login(LoginRequestDTO)} only reaches this check for existing rows).
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param user the disabled account attempting to log in
     * @return true if the account has a pending deletion request within the grace period
     */
    private boolean isWithinDeletionGracePeriod(User user) {
        return user.getStatus() == User.AccountStatus.DEACTIVATED
                && user.getDeletionRequestedAt() != null
                && user.getDeletionRequestedAt().isAfter(LocalDateTime.now().minusDays(DELETION_GRACE_PERIOD_DAYS));
    }

    /**
     * Initiates the forgot-password flow: if the email belongs to a registered account, issues a
     * fresh one-time reset token and emails it to the user. Always succeeds silently for unknown
     * emails as well, so the response never reveals whether an address is registered.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request forgot-password request payload
     */
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.email());
        if (optionalUser.isEmpty()) {
            log.info("Forgot-password requested for unknown email: {}", request.email());
            return;
        }

        User user = optionalUser.get();
        passwordResetTokenRepository.deleteByUserId(user.getId());

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(RESET_TOKEN_VALIDITY_MS))
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken.getToken());
        log.info("Password reset token issued for user ID: {}", user.getId());
    }

    /**
     * Completes the forgot-password flow: validates the reset token, updates the account
     * password, marks the token used, and revokes all active sessions for the account.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @param request reset-password request payload
     */
    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new UnauthorizedActionException("Invalid or expired reset token"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new UnauthorizedActionException("Invalid or expired reset token");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenService.deleteByUserId(user.getId());
        log.info("Password reset completed for user ID: {}", user.getId());
    }
}
