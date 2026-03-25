package com.memo.service;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SettingsService Tests")
class SettingsServiceTest {
    
    private static Path testDir;
    private SettingsService settingsService;
    
    @BeforeAll
    static void setup() throws IOException {
        testDir = Files.createTempDirectory("settings_test_");
    }
    
    @AfterAll
    static void teardown() throws IOException {
        if (testDir != null && Files.exists(testDir)) {
            Files.walk(testDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore
                    }
                });
        }
    }
    
    @BeforeEach
    void beforeEach() {
        settingsService = new SettingsService();
        settingsService.resetToDefault();
    }
    
    @AfterEach
    void afterEach() {
        settingsService.resetToDefault();
    }
    
    @Nested
    class StorageDirectory {
        
        @Test
        @DisplayName("should return default storage directory when not configured")
        void shouldReturnDefaultStorageDirectory() {
            String dir = settingsService.getStorageDirectory();
            
            assertEquals("./log", dir);
        }
        
        @Test
        @DisplayName("should set and return custom storage directory")
        void shouldSetAndReturnCustomStorageDirectory() {
            settingsService.setStorageDirectory("/custom/path");
            
            assertEquals("/custom/path", settingsService.getStorageDirectory());
        }
        
        @Test
        @DisplayName("should persist storage directory to file")
        void shouldPersistStorageDirectoryToFile() {
            settingsService.setStorageDirectory("/persisted/path");
            
            // Create new instance to verify persistence
            SettingsService newService = new SettingsService();
            assertEquals("/persisted/path", newService.getStorageDirectory());
        }
        
        @Test
        @DisplayName("should detect custom storage configuration")
        void shouldDetectCustomStorageConfiguration() {
            assertFalse(settingsService.isCustomStorageConfigured());
            
            settingsService.setStorageDirectory("/custom");
            
            assertTrue(settingsService.isCustomStorageConfigured());
        }
        
        @Test
        @DisplayName("should reset to default storage directory")
        void shouldResetToDefaultStorageDirectory() {
            settingsService.setStorageDirectory("/custom");
            settingsService.resetToDefault();
            
            assertEquals("./log", settingsService.getStorageDirectory());
            assertFalse(settingsService.isCustomStorageConfigured());
        }
    }
}
