package com.cooksync_server.controllers;

import com.cooksync_server.services.CloudinaryService;
import com.dtos.response.ApiResponse;
import com.dtos.response.cloudinary.CloudinarySignatureResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller providing Cloudinary signed authorization details to authenticated client apps.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
 */
@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    /**
     * Generates a signed upload signature payload for client-side direct media uploads.
     *
     * Complexity:
     * Time: O(1)
     * Space: O(1)
     *
     * @return response entity containing CloudinarySignatureResponse payload
     */
    @GetMapping("/signature")
    public ResponseEntity<ApiResponse<CloudinarySignatureResponse>> getSignature() {
        CloudinarySignatureResponse response = cloudinaryService.generateUploadSignature();
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Cloudinary signature generated"));
    }
}
