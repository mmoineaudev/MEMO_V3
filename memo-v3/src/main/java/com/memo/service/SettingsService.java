package com.memo.service;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Service for managing application settings.
 * UC-009: Configure storage directory
 */
public class SettingsService {
    
    private static final String SETTINGS_FILE = "settings.properties";
    private static final String KEY_STORAGE_DIR = "storage.directory";
    private static final String DEFAULT_STORAGE_DIR = System.getProperty("user.home") + File.separator + ".MEMO";
    
    private final Properties properties;
    
    /**
     * Create settings service.
     */
    public SettingsService() {
        this.properties = new Properties();
        loadSettings();
        // Ensure default storage directory is set if not configured
        if (!properties.containsKey(KEY_STORAGE_DIR)) {
            setStorageDirectory(DEFAULT_STORAGE_DIR);
        }
    }
    
    /**
     * Load settings from file.
     */
    private void loadSettings() {
        Path settingsPath = Paths.get(SETTINGS_FILE);
        if (Files.exists(settingsPath)) {
            try (InputStream input = Files.newInputStream(settingsPath)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Could not load settings: " + e.getMessage());
            }
        }
    }
    
    /**
     * Save settings to file.
     */
    public void saveSettings() {
        Path settingsPath = Paths.get(SETTINGS_FILE);
        try (OutputStream output = Files.newOutputStream(settingsPath)) {
            properties.store(output, "MEMO_V3 Settings");
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }
    
    /**
     * Get storage directory path.
     */
    public String getStorageDirectory() {
        String dir = properties.getProperty(KEY_STORAGE_DIR);
        return dir != null && !dir.isBlank() ? dir : DEFAULT_STORAGE_DIR;
    }
    
    /**
     * Set storage directory path.
     */
    public void setStorageDirectory(String directory) {
        if (directory != null && !directory.isBlank()) {
            properties.setProperty(KEY_STORAGE_DIR, directory);
            saveSettings();
        }
    }
    
    /**
     * Check if custom storage directory is configured.
     */
    public boolean isCustomStorageConfigured() {
        String dir = properties.getProperty(KEY_STORAGE_DIR);
        return dir != null && !dir.isBlank() && !DEFAULT_STORAGE_DIR.equals(dir);
    }
    
    /**
     * Reset to default storage directory.
     */
    public void resetToDefault() {
        properties.remove(KEY_STORAGE_DIR);
        saveSettings();
    }
}
