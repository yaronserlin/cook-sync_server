package com.cooksync_server.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dtos.response.cloudinary.CloudinarySignatureResponse;

import lombok.RequiredArgsConstructor;

/**
 * Generates short-lived, signed upload authorizations for the Cloudinary
 * client SDK, so the API secret never has to leave the server.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final String UPLOAD_FOLDER = "CookSyncApp";

    private final Cloudinary cloudinary;

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
