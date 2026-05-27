package com.marketplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Value("${app.upload.max-file-size:5242880}")
    private long maxFileSize;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    public String saveImage(MultipartFile file) throws IOException {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + maxFileSize + " bytes");
        }

        if (!isValidImageType(file.getContentType())) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: JPEG, PNG, GIF, WebP");
        }

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + "." + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        // Return relative path for storage in database
        return "uploads/products/" + uniqueFilename;
    }

    public void deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty() || imagePath.startsWith("http")) {
            return; // Don't delete external URLs
        }

        try {
            Path filePath = Paths.get(imagePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't throw - image deletion failure shouldn't break product operations
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        for (String allowedType : ALLOWED_TYPES) {
            if (contentType.equals(allowedType)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
