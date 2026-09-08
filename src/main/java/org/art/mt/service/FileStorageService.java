package org.art.mt.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Validates the file is an image, stores it under the upload dir with a unique
     * name, deletes the previous file at previousUrl (if any and locally-hosted),
     * and returns the new file's public "/uploads/..." URL.
     */
    public String storeImage(MultipartFile file, String filenamePrefix, String previousUrl) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Uploaded file must be an image");
        }

        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);

            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf('.'));
            }
            String filename = filenamePrefix + "-" + UUID.randomUUID() + extension;
            Files.copy(file.getInputStream(), uploadPath.resolve(filename));

            if (previousUrl != null && previousUrl.startsWith("/uploads/")) {
                Path previousFile = uploadPath.resolve(previousUrl.substring("/uploads/".length()));
                Files.deleteIfExists(previousFile);
            }

            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }
}
