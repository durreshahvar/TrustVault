
package com.trustvault.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private final Path storageDirectory;

    public LocalFileStorageService(
            @Value("${trustvault.storage.directory:./private-uploads}")
            String directory) throws IOException {

        this.storageDirectory = Path.of(directory)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(this.storageDirectory);
    }

    public String store(InputStream inputStream) throws IOException {
        String storageKey = UUID.randomUUID().toString();
        Path destination = storageDirectory.resolve(storageKey).normalize();

        if (!destination.getParent().equals(storageDirectory)) {
            throw new IOException("Invalid storage path");
        }

        try (InputStream input = inputStream) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        return storageKey;
    }

    public Path resolve(String storageKey) {
        if (storageKey == null ||
                !storageKey.matches("[a-fA-F0-9-]{36}")) {
            throw new IllegalArgumentException("Invalid storage key");
        }

        Path resolved = storageDirectory.resolve(storageKey).normalize();

        if (!resolved.getParent().equals(storageDirectory)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        return resolved;
    }
}

