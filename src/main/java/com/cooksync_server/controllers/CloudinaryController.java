package com.cooksync_server.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dtos.response.ApiResponse;
import com.dtos.response.cloudinary.CloudinarySignatureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for providing Cloudinary authorization details to the client.
 */
@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final Cloudinary cloudinary;

    @GetMapping("/signature")
    public ResponseEntity<ApiResponse<CloudinarySignatureResponse>> getSignature() {
        long timestamp = System.currentTimeMillis() / 1000;
        
        // Prepare parameters to sign
        Map<String, Object> params = ObjectUtils.asMap(
                "timestamp", timestamp,
                "folder", "CookSyncApp"
        );

        // Generate signature
        String signature = cloudinary.apiSignRequest(params, cloudinary.config.apiSecret, (int) timestamp);

        CloudinarySignatureResponse response = new CloudinarySignatureResponse(
                signature,
                timestamp,
                cloudinary.config.apiKey,
                cloudinary.config.cloudName
        );

        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Cloudinary signature generated"));
    }
}
