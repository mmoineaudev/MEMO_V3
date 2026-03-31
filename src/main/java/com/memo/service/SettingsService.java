package com.memo.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing application settings and preferences.
 */
public class SettingsService {
    
    private static final String SETTINGS_FILE = "settings.json";
    
    private final Path settingsDir;
    private final Map<String, String> settings;
    
    public SettingsService(String dirPath) {
        this.settingsDir = Path.of(dirPath);
        this.settings = new HashMap<>();
        
        // Load existing settings if they exist
        loadSettings();
    }
    
    /**
     * Gets a string setting value.
     * 
     * @param key The setting key
     * @param defaultValue Default value if setting not found
     * @return The setting value or default
     */
    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }
    
    /**
     * Sets a string setting value.
     * 
     * @param key The setting key
     * @param value The value to set
     */
    public void setSetting(String key, String value) {
        settings.put(key, value);
    }
    
    /**
     * Removes a setting.
     * 
     * @param key The setting key to remove
     */
    public void removeSetting(String key) {
        settings.remove(key);
    }
    
    /**
     * Gets all current settings.
     * 
     * @return Map of all settings
     */
    public Map<String, String> getAllSettings() {
        return new HashMap<>(settings);
    }
    
    /**
     * Clears all settings.
     */
    public void clearAllSettings() {
        settings.clear();
    }
    
    /**
     * Gets an integer setting value.
     * 
     * @param key The setting key
     * @param defaultValue Default value if setting not found or invalid
     * @return The integer value or default
     */
    public int getIntegerSetting(String key, int defaultValue) {
        String value = settings.get(key);
        
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a boolean setting value.
     * 
     * @param key The setting key
     * @param defaultValue Default value if setting not found or invalid
     * @return The boolean value or default
     */
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = settings.get(key);
        
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        
        String lowerValue = value.trim().toLowerCase();
        
        if ("true".equals(lowerValue)) {
            return true;
        } else if ("false".equals(lowerValue)) {
            return false;
        } else {
            // Invalid value, return default
            return defaultValue;
        }
    }
    
    /**
     * Saves all settings to a JSON file.
     */
    public void saveSettings() {
        try {
            // Create directory if it doesn't exist
            Files.createDirectories(settingsDir);
            
            Path settingsFile = settingsDir.resolve(SETTINGS_FILE);
            
            // Simple JSON format (not using external library for simplicity)
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                if (!first) {
                    json.append(",");
                }
                json.append("\"").append(escapeJson(entry.getKey())).append("\":")
                   .append("\"").append(escapeJson(entry.getValue())).append("\"");
                first = false;
            }
            
            json.append("}");
            
            Files.write(settingsFile, json.toString().getBytes());
        } catch (IOException e) {
            // Silently handle save errors
        }
    }
    
    /**
     * Loads settings from a JSON file.
     */
    public void loadSettings() {
        Path settingsFile = settingsDir.resolve(SETTINGS_FILE);
        
        if (!Files.exists(settingsFile)) {
            return;
        }
        
        try {
            String content = Files.readString(settingsFile);
            
            // Simple JSON parsing (not using external library for simplicity)
            // This is a basic parser for our simple key-value format
            
            int braceStart = content.indexOf('{');
            int braceEnd = content.lastIndexOf('}');
            
            if (braceStart == -1 || braceEnd == -1) {
                return;
            }
            
            String jsonContent = content.substring(braceStart + 1, braceEnd);
            
            // Parse key-value pairs
            parseSimpleJson(jsonContent);
        } catch (IOException e) {
            // Silently handle load errors
        }
    }
    
    private void parseSimpleJson(String json) {
        int pos = 0;
        
        while (pos < json.length()) {
            // Skip whitespace and commas
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
            
            if (pos >= json.length()) break;
            
            if (json.charAt(pos) == ',') {
                pos++;
                continue;
            }
            
            // Parse key
            if (json.charAt(pos) == '"') {
                int keyStart = pos + 1;
                int keyEnd = json.indexOf('"', keyStart);
                
                if (keyEnd == -1) break;
                
                String key = unescapeJson(json.substring(keyStart, keyEnd));
                pos = keyEnd + 1;
                
                // Skip colon
                while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                    pos++;
                }
                
                if (pos >= json.length() || json.charAt(pos) != ':') break;
                pos++;
                
                // Parse value
                while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                    pos++;
                }
                
                if (pos >= json.length() || json.charAt(pos) != '"') break;
                
                int valStart = pos + 1;
                int valEnd = json.indexOf('"', valStart);
                
                if (valEnd == -1) break;
                
                String value = unescapeJson(json.substring(valStart, valEnd));
                settings.put(key, value);
                
                pos = valEnd + 1;
            } else {
                pos++;
            }
        }
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private String unescapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\\"", "\"")
                   .replace("\\\\", "\\")
                   .replace("\\n", "\n")
                   .replace("\\r", "\r")
                   .replace("\\t", "\t");
    }
}