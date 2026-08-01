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
 * Controller for providing Cloudinary authorization details to the client.
 */
@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @GetMapping("/signature")
    public ResponseEntity<ApiResponse<CloudinarySignatureResponse>> getSignature() {
        CloudinarySignatureResponse response = cloudinaryService.generateUploadSignature();
        return ResponseEntity.ok(new ApiResponse<>(true, response, null, "Cloudinary signature generated"));
    }
}
