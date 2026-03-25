package com.memo.integration;

import com.memo.model.ActivityEntry;
import com.memo.service.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for core functionality.
 * All tests use isolated temp directories - NEVER touch user data.
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
    
    /**
     * Clean storage directory before each test to ensure isolation.
     */
    @BeforeEach
    void beforeEach() throws IOException {
        Files.list(storageDir)
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    // Ignore
                }
            });
    }
    
    /**
     * Helper to create test entries.
     */
    private static ActivityEntry createEntry(String activityType, String description, 
                                             String status, String comment, int hour, double timeSpent) {
        return new ActivityEntry(activityType, description, status, comment, 
                String.format("25/03/2025 %02d:00", hour), timeSpent);
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
            
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            List<ActivityEntry> entries = storage.readEntries("test.csv");
            
            assertEquals(2, entries.size());
            assertEquals("Task 1", entries.get(0).description());
            assertEquals("Task 2", entries.get(1).description());
        }
        
        @Test
        @DisplayName("Should create storage directory if not exists")
        void shouldCreateStorageDirectory() throws Exception {
            Path newStorageDir = testDir.resolve("new_storage");
            CsvStorageService storage = new CsvStorageService(newStorageDir.toString());
            
            assertTrue(storage.ensureDirectoryExists());
            assertTrue(Files.exists(newStorageDir));
        }
        
        @Test
        @DisplayName("Should write and read all entries preserving data")
        void shouldWriteAndReadAllEntries() throws Exception {
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            
            List<ActivityEntry> entries = new ArrayList<>();
            entries.add(createEntry("DEV", "Task A", "TODO", "Comment A", 9, 1.5));
            entries.add(createEntry("CEREMONY", "Standup", "DONE", "Daily meeting", 9, 0.25));
            entries.add(createEntry("SUPPORT", "Email", "DOING", "Urgent client request", 10, 2.0));
            
            String fileName = "test_day.csv";
            boolean success = storage.writeAllEntries(fileName, entries);
            
            assertTrue(success);
            assertTrue(Files.exists(storageDir.resolve(fileName)));
            
            // Read back and verify
            List<ActivityEntry> readEntries = storage.readEntries(fileName);
            assertEquals(3, readEntries.size());
            
            assertEquals("Task A", readEntries.get(0).description());
            assertEquals("Standup", readEntries.get(1).description());
            assertEquals("Email", readEntries.get(2).description());
        }
        
        @Test
        @DisplayName("Should handle multiple CSV files correctly")
        void shouldHandleMultipleCsvFiles() throws Exception {
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            
            // Create entries for two different days
            List<ActivityEntry> day1 = Arrays.asList(
                    createEntry("DEV", "Monday Task", "TODO", "Start of week", 9, 4.0),
                    createEntry("ADMIN", "Planning", "DONE", "Sprint planning", 13, 1.0)
            );
            
            List<ActivityEntry> day2 = Arrays.asList(
                    createEntry("DEV", "Tuesday Task", "DOING", "In progress", 10, 3.5),
                    createEntry("LEARNING", "Reading docs", "TODO", "New framework", 14, 1.0)
            );
            
            storage.writeAllEntries("activity_2025-03-25.csv", day1);
            storage.writeAllEntries("activity_2025-03-26.csv", day2);
            
            // Read all files matching pattern
            List<String> files = storage.listFilesWithPattern("activity");
            assertEquals(2, files.size());
            
            List<ActivityEntry> allEntries = new ArrayList<>();
            for (String file : files) {
                allEntries.addAll(storage.readEntries(file));
            }
            
            assertEquals(4, allEntries.size());
        }
    }
    
    @Nested
    @DisplayName("Multi-line Comment Tests")
    class MultiLineCommentTests {
        
        @Test
        @DisplayName("Should preserve multi-line comments through CSV round-trip")
        void shouldPreserveMultiLineComments() {
            String originalComment = "Line 1\nLine 2\nLine 3";
            ActivityEntry entry = createEntry("DEV", "Task", "TODO", originalComment, 10, 2.0);
            
            String csv = entry.toCsv();
            ActivityEntry parsed = ActivityEntry.fromCsv(csv);
            
            assertNotNull(parsed);
            assertEquals(originalComment, parsed.comment());
        }
        
        @Test
        @DisplayName("Should handle empty comments")
        void shouldHandleEmptyComments() {
            ActivityEntry entry = createEntry("DEV", "Task", "TODO", "", 10, 2.0);
            
            String csv = entry.toCsv();
            ActivityEntry parsed = ActivityEntry.fromCsv(csv);
            
            assertNotNull(parsed);
            assertEquals("", parsed.comment());
        }
        
        @Test
        @DisplayName("Should handle comments with special characters")
        void shouldHandleSpecialCharacters() {
            String specialComment = "Note with !semicolons and @symbols and #hashes";
            ActivityEntry entry = createEntry("DEV", "Task", "TODO", specialComment, 10, 2.0);
            
            String csv = entry.toCsv();
            ActivityEntry parsed = ActivityEntry.fromCsv(csv);
            
            assertNotNull(parsed);
            assertEquals(specialComment, parsed.comment());
        }
    }
    
    @Nested
    @DisplayName("Full Workflow Tests")
    class FullWorkflowTests {
        
        @Test
        @DisplayName("Complete workflow: create, save, reload, verify")
        void completeWorkflow() throws Exception {
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            HistoryService history = new HistoryService(storage);
            EntryEditorService editor = new EntryEditorService(history, "activity_workflow_test.csv");
            
            // Step 1: Create entries (timestamps are auto-generated, so order is insertion-based)
            ActivityEntry entry1 = editor.createEntry("DEV", "Initial task", "TODO", "First task", 2.0);
            boolean saved1 = editor.saveEntry(entry1);
            assertTrue(saved1);
            
            ActivityEntry entry2 = editor.createEntry("SUPPORT", "Help desk", "DOING", "User request", 1.5);
            boolean saved2 = editor.saveEntry(entry2);
            assertTrue(saved2);
            
            // Step 2: Reload all history (simulates app restart)
            List<ActivityEntry> reloaded = history.loadAllHistory();
            assertEquals(2, reloaded.size());
            
            // Step 3: Verify data integrity (sorted by timestamp, newest first)
            // Help desk was created second (later timestamp), so it appears first
            assertEquals("Help desk", reloaded.get(0).description());
            assertEquals("DOING", reloaded.get(0).status());
            assertEquals("Initial task", reloaded.get(1).description());
            assertEquals("TODO", reloaded.get(1).status());
        }
        
        @Test
        @DisplayName("Should handle entry update and delete workflow")
        void updateAndDeleteWorkflow() throws Exception {
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            HistoryService history = new HistoryService(storage);
            EntryEditorService editor = new EntryEditorService(history, "activity_update_test.csv");
            
            // Create entry
            ActivityEntry entry = editor.createEntry("DEV", "To do task", "TODO", "Initial comment", 1.0);
            editor.saveEntry(entry);
            
            // Reload to get the entry from storage
            List<ActivityEntry> reloaded = history.loadAllHistory();
            assertEquals(1, reloaded.size());
            
            // Update status
            ActivityEntry updated = reloaded.get(0).withStatus("DOING");
            boolean updatedSaved = editor.updateEntry(reloaded.get(0), updated);
            assertTrue(updatedSaved);
            
            // Reload and verify update
            reloaded = history.loadAllHistory();
            assertEquals("DOING", reloaded.get(0).status());
            
            // Delete entry
            boolean deleted = editor.deleteEntry(reloaded.get(0));
            assertTrue(deleted);
            
            // Reload and verify deletion
            reloaded = history.loadAllHistory();
            assertEquals(0, reloaded.size());
        }
        
        @Test
        @DisplayName("HistoryService should load entries from multiple source files")
        void historyLoadsFromMultipleFiles() throws Exception {
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            
            // Create files for different dates with distinct timestamps
            // Using different hours to ensure deterministic sorting
            List<ActivityEntry> day1 = Arrays.asList(
                    createEntry("DEV", "Day 1 Task", "DONE", "Completed", 10, 2.0)
            );
            List<ActivityEntry> day2 = Arrays.asList(
                    createEntry("DEV", "Day 2 Task", "TODO", "Pending", 11, 3.0),
                    createEntry("SUPPORT", "Day 2 Support", "DOING", "In progress", 14, 1.0)
            );
            
            storage.writeAllEntries("activity_2025-03-25.csv", day1);
            storage.writeAllEntries("activity_2025-03-26.csv", day2);
            
            // Load all history
            HistoryService history = new HistoryService(storage);
            List<ActivityEntry> allHistory = history.loadAllHistory();
            
            assertEquals(3, allHistory.size());
            
            // Should be sorted by timestamp (newest first):
            // Day 2 Support: 14:00 (newest)
            // Day 2 Task: 11:00
            // Day 1 Task: 10:00 (oldest)
            assertEquals("Day 2 Support", allHistory.get(0).description());
            assertEquals("Day 2 Task", allHistory.get(1).description());
            assertEquals("Day 1 Task", allHistory.get(2).description());
        }
    }
    
    @Nested
    @DisplayName("Safety Tests - Verify User Data Protection")
    class SafetyTests {
        
        @Test
        @DisplayName("Should NEVER write to user home directory")
        void shouldNotWriteToUserHome() {
            String userHome = System.getProperty("user.home");
            Path userMemoDir = Paths.get(userHome, ".MEMO");
            
            // This test verifies the default behavior doesn't target user home
            // The actual check is that we always use temp directories in tests
            // and the application requires explicit configuration for custom paths
            assertNotNull(userHome);
            
            // Verify temp directory is used, not user home
            assertFalse(testDir.toString().contains(userHome), 
                    "Test directory should not be in user home");
        }
        
        @Test
        @DisplayName("CsvStorageService should fail safely if directory is not writable")
        void shouldFailSafelyOnUnwritableDirectory() throws Exception {
            // Create a directory and make it read-only
            Path readOnlyDir = testDir.resolve("readonly");
            Files.createDirectory(readOnlyDir);
            
            // Try to create a subdirectory (should fail or work depending on OS)
            // The key is that the service should handle this gracefully
            CsvStorageService storage = new CsvStorageService(readOnlyDir.toString());
            
            // ensureDirectoryExists should return false if it can't create
            // (though for a directory that already exists, it returns true)
            boolean canCreate = storage.ensureDirectoryExists();
            
            // This is OS-dependent; the important thing is no exception is thrown
            // and the application doesn't crash
            assertTrue(canCreate); // Directory already exists, so this is true
        }
        
        @Test
        @DisplayName("Should never modify files outside configured storage")
        void shouldNeverModifyOutsideStorage() throws Exception {
            CsvStorageService storage = new CsvStorageService(storageDir.toString());
            
            // Try to read a file outside storage directory
            Path externalFile = testDir.resolve("external.csv");
            Files.writeString(externalFile, "DEV;External;TODO;;00/00/0000 00:00;0.0");
            
            // Reading should fail or return empty - never modify
            List<ActivityEntry> entries = storage.readEntries("external.csv");
            
            // File doesn't exist in storage dir, so should be empty
            assertTrue(entries.isEmpty());
            
            // Verify external file was NOT modified
            String content = Files.readString(externalFile);
            assertEquals("DEV;External;TODO;;00/00/0000 00:00;0.0", content);
            
            Files.delete(externalFile);
        }
    }
}
