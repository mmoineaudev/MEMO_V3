package com.memo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SettingsService.
 */
class SettingsServiceTest {
    
    private SettingsService settingsService;
    private Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        // Create a temporary directory for settings storage
        tempDir = Files.createTempDirectory("memo_settings_");
        settingsService = new SettingsService(tempDir.toString());
    }
    
    @Test
    void testGetSettingReturnsDefaultValueWhenNotSet() {
        String value = settingsService.getSetting("theme", "dark");
        
        assertEquals("dark", value);
    }
    
    @Test
    void testGetSettingWithCustomDefaultValue() {
        String value = settingsService.getSetting("nonexistent", "default_value");
        
        assertEquals("default_value", value);
    }
    
    @Test
    void testSetAndGetSetting() {
        settingsService.setSetting("theme", "light");
        
        String value = settingsService.getSetting("theme", "dark");
        
        assertEquals("light", value);
    }
    
    @Test
    void testRemoveSetting() {
        settingsService.setSetting("theme", "light");
        settingsService.removeSetting("theme");
        
        String value = settingsService.getSetting("theme", "dark");
        
        assertEquals("dark", value); // Should return default value
    }
    
    @Test
    void testGetAllSettings() {
        settingsService.setSetting("theme", "light");
        settingsService.setSetting("fontSize", "14");
        
        Map<String, String> allSettings = settingsService.getAllSettings();
        
        assertNotNull(allSettings);
        assertEquals("light", allSettings.get("theme"));
        assertEquals("14", allSettings.get("fontSize"));
    }
    
    @Test
    void testGetAllSettingsEmpty() {
        Map<String, String> allSettings = settingsService.getAllSettings();
        
        assertNotNull(allSettings);
        assertTrue(allSettings.isEmpty());
    }
    
    @Test
    void testClearAllSettings() {
        settingsService.setSetting("theme", "light");
        settingsService.setSetting("fontSize", "14");
        
        settingsService.clearAllSettings();
        
        Map<String, String> allSettings = settingsService.getAllSettings();
        
        assertTrue(allSettings.isEmpty());
    }
    
    @Test
    void testGetSettingAsInteger() {
        settingsService.setSetting("fontSize", "16");
        
        int value = settingsService.getIntegerSetting("fontSize", 12);
        
        assertEquals(16, value);
    }
    
    @Test
    void testGetSettingAsIntegerWithInvalidValueReturnsDefault() {
        settingsService.setSetting("invalidInt", "not_a_number");
        
        int value = settingsService.getIntegerSetting("invalidInt", 12);
        
        assertEquals(12, value); // Should return default for invalid values
    }
    
    @Test
    void testGetSettingAsBooleanTrue() {
        settingsService.setSetting("autoSave", "true");
        
        boolean value = settingsService.getBooleanSetting("autoSave", false);
        
        assertTrue(value);
    }
    
    @Test
    void testGetSettingAsBooleanFalse() {
        settingsService.setSetting("autoSave", "false");
        
        boolean value = settingsService.getBooleanSetting("autoSave", true);
        
        assertFalse(value);
    }
    
    @Test
    void testGetSettingAsBooleanWithInvalidValueReturnsDefault() {
        settingsService.setSetting("invalidBool", "maybe");
        
        boolean value = settingsService.getBooleanSetting("invalidBool", true);
        
        assertTrue(value); // Should return default for invalid values
    }
    
    @Test
    void testPersistSettingsToFile() throws Exception {
        settingsService.setSetting("theme", "dark");
        settingsService.setSetting("fontSize", "14");
        
        settingsService.saveSettings();
        
        Path settingsFile = tempDir.resolve("settings.json");
        
        assertTrue(Files.exists(settingsFile));
    }
    
    @Test
    void testLoadSettingsFromFile() throws Exception {
        // First save some settings
        settingsService.setSetting("theme", "dark");
        settingsService.saveSettings();
        
        // Create new instance and load
        SettingsService newService = new SettingsService(tempDir.toString());
        String value = newService.getSetting("theme", "light");
        
        assertEquals("dark", value);
    }
    
    @Test
    void testLoadSettingsFromNonExistentFileUsesDefaults() {
        // Create service with empty directory
        SettingsService newService = new SettingsService(tempDir.toString());
        String value = newService.getSetting("theme", "light");
        
        assertEquals("light", value); // Should return default
    }
}