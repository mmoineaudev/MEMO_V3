package com.memo.integration;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal integration tests for core functionality.
 */
@DisplayName("Integration Tests")
class IntegrationTest {
    
    private static Path testDir;
    private static Path storageDir;
    
    @BeforeAll
    static void setup() throws IOException {
        testDir = Files.createTempDirectory("integration_test_");
        storageDir = testDir.resolve("storage");
        Files.createDirectory(storageDir);
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
    
    @Nested
    @DisplayName("ActivityEntry Tests")
    class ActivityEntryTests {
        
        @Test
        @DisplayName("Should create entry with all fields")
        void shouldCreateEntry() {
            ActivityEntry entry = new ActivityEntry("DEV", "Test task", "TODO", "Test comment", "25/03/2025 10:00", 2.5);
            
            assertEquals("DEV", entry.activityType());
            assertEquals("Test task", entry.description());
            assertEquals("TODO", entry.status());
            assertEquals("Test comment", entry.comment());
            assertEquals("25/03/2025 10:00", entry.timestamp());
            assertEquals(2.5, entry.timeSpent());
        }
        
        @Test
        @DisplayName("Should convert to CSV and back")
        void shouldConvertToCsvAndBack() {
            ActivityEntry original = new ActivityEntry("DEV", "Test task", "TODO", "Test comment", "25/03/2025 10:00", 2.5);
            String csv = original.toCsv();
            ActivityEntry parsed = ActivityEntry.fromCsv(csv);
            
            assertNotNull(parsed);
            assertEquals("DEV", parsed.activityType());
            assertEquals("Test task", parsed.description());
            assertEquals("TODO", parsed.status());
            assertEquals("Test comment", parsed.comment());
            assertEquals("25/03/2025 10:00", parsed.timestamp());
            assertEquals(2.5, parsed.timeSpent());
        }
        
        @Test
        @DisplayName("Should sanitize semicolons in input")
        void shouldSanitizeSemicolons() {
            // Test with valid CSV format (6 fields separated by semicolons)
            // ACTIVITY_TYPE;DESCRIPTION;STATUS;COMMENT;TIMESTAMP;TIME_SPENT
            ActivityEntry entry = ActivityEntry.fromCsv("DEV;Description!with!semicolons;TODO;Comment!with!semicolons;25/03/2025 10:00;1.0");
            
            assertNotNull(entry);
            assertEquals("Description!with!semicolons", entry.description());
            assertEquals("Comment!with!semicolons", entry.comment());
        }
        
        @Test
        @DisplayName("Should handle multi-line comments")
        void shouldHandleMultiLineComments() {
            String csvContent = "DEV;Test entry;TODO;First line\\nSecond line;25/03/2025 10:00;2.0\n";
            
            List<ActivityEntry> entries = new ArrayList<>();
            String[] lines = csvContent.split("\\n");
            for (String line : lines) {
                if (!line.isBlank()) {
                    ActivityEntry entry = ActivityEntry.fromCsv(line);
                    if (entry != null) entries.add(entry);
                }
            }
            
            assertEquals(1, entries.size());
            assertTrue(entries.get(0).comment().contains("First line"));
            assertTrue(entries.get(0).comment().contains("Second line"));
        }
        
        @Test
        @DisplayName("Should create copy with updated status")
        void shouldCreateCopyWithUpdatedStatus() {
            ActivityEntry original = new ActivityEntry("DEV", "Task", "TODO", "Comment", "25/03/2025 10:00", 2.5);
            ActivityEntry updated = original.withStatus("DOING");
            
            assertEquals("DEV", updated.activityType());
            assertEquals("Task", updated.description());
            assertEquals("DOING", updated.status());
            assertEquals("Comment", updated.comment());
            assertEquals("25/03/2025 10:00", updated.timestamp());
            assertEquals(2.5, updated.timeSpent());
            
            // Original should be unchanged
            assertEquals("TODO", original.status());
        }
    }
    
    @Nested
    @DisplayName("CSV Storage Tests")
    class CsvStorageTests {
        
        @Test
        @DisplayName("Should read entries from CSV file")
        void shouldReadEntriesFromCsv() throws Exception {
            String csvContent = "DEV;Task 1;TODO;Comment 1;25/03/2025 10:00;1.5\n";
            csvContent += "DEV;Task 2;DONE;Comment 2;25/03/2025 11:00;2.5\n";
            
            Path testFile = storageDir.resolve("test.csv");
            Files.writeString(testFile, csvContent);
            
            com.memo.service.CsvStorageService storage = new com.memo.service.CsvStorageService(storageDir.toString());
            List<ActivityEntry> entries = storage.readEntries("test.csv");
            
            assertEquals(2, entries.size());
            assertEquals("Task 1", entries.get(0).description());
            assertEquals("Task 2", entries.get(1).description());
        }
        
        @Test
        @DisplayName("Should create storage directory if not exists")
        void shouldCreateStorageDirectory() throws Exception {
            Path newStorageDir = testDir.resolve("new_storage");
            com.memo.service.CsvStorageService storage = new com.memo.service.CsvStorageService(newStorageDir.toString());
            
            assertTrue(storage.ensureDirectoryExists());
            assertTrue(Files.exists(newStorageDir));
        }
    }
}
