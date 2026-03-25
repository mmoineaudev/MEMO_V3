package com.memo.service;

import com.memo.model.ActivityEntry;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing CSV storage of activity entries.
 * Handles directory creation, file operations, and entry persistence.
 */
public class CsvStorageService {
    
    private final Path storageDirectory;
    
    /**
     * Create a storage service with default directory (./log/).
     */
    public CsvStorageService() {
        this("./log");
    }
    
    /**
     * Create a storage service with custom directory path.
     */
    public CsvStorageService(String storagePath) {
        this.storageDirectory = Path.of(storagePath);
    }
    
    /**
     * Create storage directory if it doesn't exist.
     * Returns true if directory was created or already exists.
     * Returns false if creation failed.
     */
    public boolean ensureDirectoryExists() {
        try {
            if (!Files.exists(storageDirectory)) {
                Files.createDirectories(storageDirectory);
            }
            return Files.exists(storageDirectory) && Files.isWritable(storageDirectory);
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Get the storage directory path.
     */
    public Path getStorageDirectory() {
        return storageDirectory;
    }
    
    /**
     * List all CSV files in storage directory matching a pattern prefix.
     */
    public List<String> listFilesWithPattern(String pattern) {
        List<String> files = new ArrayList<>();
        try {
            if (Files.exists(storageDirectory)) {
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(storageDirectory)) {
                    for (Path entry : stream) {
                        if (Files.isRegularFile(entry)) {
                            String fileName = entry.getFileName().toString();
                            if (fileName.startsWith(pattern)) {
                                files.add(fileName);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            // Log error, return empty list
        }
        return files;
    }
    
    /**
     * Read all lines from a CSV file.
     */
    public List<String> readFile(String fileName) {
        Path filePath = storageDirectory.resolve(fileName);
        try {
            if (Files.exists(filePath)) {
                return Files.readAllLines(filePath);
            }
        } catch (IOException e) {
            // Will be handled by caller
        }
        return new ArrayList<>();
    }
    
    /**
     * Read all entries from a CSV file.
     */
    public List<ActivityEntry> readEntries(String fileName) {
        List<String> lines = readFile(fileName);
        return lines.stream()
                .map(line -> ActivityEntry.fromCsv(line))
                .filter(entry -> entry != null)
                .collect(Collectors.toList());
    }
    
    /**
     * Append a line to a CSV file, creating it if necessary.
     * Normalizes line endings to LF.
     */
    public boolean appendToFile(String fileName, String line) {
        Path filePath = storageDirectory.resolve(fileName);
        try {
            String normalizedLine = line.replace("\r\n", "\n").replace("\r", "\n") + "\n";
            Files.writeString(filePath, normalizedLine, 
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Write all entries to a CSV file (atomic write via temp file).
     */
    public boolean writeAllEntries(String fileName, List<ActivityEntry> entries) {
        Path filePath = storageDirectory.resolve(fileName);
        Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        
        try {
            // Build content
            StringBuilder content = new StringBuilder();
            for (ActivityEntry entry : entries) {
                content.append(entry.toCsv()).append("\n");
            }
            
            // Write to temp file
            Files.writeString(tempFile, content.toString());
            
            // Atomic rename
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return true;
        } catch (IOException e) {
            // Clean up temp file on failure
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ex) {
                // Ignore cleanup errors
            }
            return false;
        }
    }
    
    /**
     * Check if a file exists in storage directory.
     */
    public boolean fileExists(String fileName) {
        return Files.exists(storageDirectory.resolve(fileName));
    }
    
    /**
     * Delete a file from storage directory.
     */
    public boolean deleteFile(String fileName) {
        try {
            Files.deleteIfExists(storageDirectory.resolve(fileName));
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
