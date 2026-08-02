package com.cooksync_server.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dtos.response.cloudinary.CloudinarySignatureResponse;

import lombok.RequiredArgsConstructor;

/**
 * Service class generating short-lived signed upload authorizations for Cloudinary SDK.
 * Ensures the API secret remains securely on the backend server.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final String UPLOAD_FOLDER = "CookSyncApp";

    private final Cloudinary cloudinary;

    /**
     * Generates a signed upload signature payload for client direct uploads.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @return CloudinarySignatureResponse DTO containing signature details and timestamp
     */
    public CloudinarySignatureResponse generateUploadSignature() {
        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, Object> params = ObjectUtils.asMap(
                "timestamp", timestamp,
                "folder", UPLOAD_FOLDER
        );

        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret, (int) timestamp);

        return new CloudinarySignatureResponse(
                signature,
                timestamp,
                cloudinary.config.apiKey,
                cloudinary.config.cloudName
        );
    }
}
