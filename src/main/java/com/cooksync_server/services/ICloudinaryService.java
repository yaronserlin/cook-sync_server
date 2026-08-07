package com.cooksync_server.services;

import java.util.Map;
import org.springframework.stereotype.Service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dtos.response.cloudinary.CloudinarySignatureResponse;
import lombok.RequiredArgsConstructor;

public interface ICloudinaryService {
    CloudinarySignatureResponse generateUploadSignature();
}