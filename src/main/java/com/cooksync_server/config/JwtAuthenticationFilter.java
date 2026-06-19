package com.cooksync_server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. חילוץ ה-Header שנקרא "Authorization"
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. אם אין Header או שהוא לא מתחיל ב-"Bearer " - תעביר הלאה (הבקשה תידחה בהמשך אם היא דורשת הרשאה)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. חיתוך המילה "Bearer " כדי לקבל נטו את הטוקן
        jwt = authHeader.substring(7);

        try {
            // 4. חילוץ האימייל מהטוקן
            userEmail = jwtUtil.extractEmail(jwt);

            // 5. אם מצאנו אימייל ועדיין אין אימות בקונטקסט של Spring Security
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // בדיקה האם הטוקן תקין (לא פג תוקף ושייך לאימייל)
                if (jwtUtil.validateToken(jwt, userEmail)) {

                    // חילוץ תפקיד המשתמש מהטוקן (Admin או משתמש רגיל)
                    boolean isAdmin = jwtUtil.extractClaim(jwt, claims -> claims.get("isAdmin", Boolean.class));
                    String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    // יצירת אובייקט אימות רשמי של Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // שמירת האימות בהקשר של הבקשה הנוכחית
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // אם הטוקן פגום או פג תוקף - נתפוס את השגיאה ונמשיך הלאה (המשתמש פשוט לא יהיה מחובר)
            System.out.println("Invalid JWT Token: " + e.getMessage());
        }

        // 6. המשך בשרשרת הפילטרים הרגילה
        filterChain.doFilter(request, response);
    }
}
