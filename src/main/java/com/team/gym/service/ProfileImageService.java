package com.team.gym.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ProfileImageService {

    private final S3Client s3Client;
    private final String bucket;

    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    private static final List<String> ALLOWED_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/*" // temp whitelist type for testing
    );

    public ProfileImageService(
            S3Client s3Client,
            @Value("${cloudflare.r2.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    public String uploadUserProfileImage(Long userId, MultipartFile file) {
        validate(file);

        String extension = getExtension(file.getContentType());

        String objectKey = "profile-images/"
                + userId + "/"
                + UUID.randomUUID() + extension;

        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

        } catch (IOException e) {
            throw new RuntimeException("upload_failed");
        }

        return objectKey;
    }

    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;

        s3Client.deleteObject(b -> b
                .bucket(bucket)
                .key(objectKey)
        );
    }

    // Validation
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file_required");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("file_too_large");
        }

        String type = file.getContentType();
        if (type == null || !ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException("invalid_file_type");
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("unsupported_type");
        };
    }
}