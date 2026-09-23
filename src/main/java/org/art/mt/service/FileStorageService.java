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

    /** Public URL prefix the stored files are served under, and the matching security/resource pattern. */
    public static final String UPLOADS_URL_PREFIX = "/uploads/";
    public static final String UPLOADS_URL_PATTERN = UPLOADS_URL_PREFIX + "**";

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Validates the file is an image, stores it under the upload dir with a unique
     * name, deletes the previous file at previousUrl (if any and locally-hosted),
     * and returns the new file's public URL under UPLOADS_URL_PREFIX.
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

            if (previousUrl != null && previousUrl.startsWith(UPLOADS_URL_PREFIX)) {
                Path previousFile = uploadPath.resolve(previousUrl.substring(UPLOADS_URL_PREFIX.length()));
                Files.deleteIfExists(previousFile);
            }

            return UPLOADS_URL_PREFIX + filename;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file", e);
        }
    }
}
