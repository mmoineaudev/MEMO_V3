package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EntryEditorService Tests")
class EntryEditorServiceTest {
    
    private static Path testStorageDir;
    private static CsvStorageService csvStorage;
    private static HistoryService historyService;
    private static EntryEditorService entryEditorService;
    
    @BeforeAll
    static void setup() throws IOException {
        testStorageDir = Files.createTempDirectory("memo_entry_test_");
        csvStorage = new CsvStorageService(testStorageDir.toString());
    }
    
    @AfterAll
    static void teardown() throws IOException {
        if (testStorageDir != null && Files.exists(testStorageDir)) {
            Files.walk(testStorageDir)
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
        historyService = new HistoryService(csvStorage);
        entryEditorService = new EntryEditorService(historyService, "test_activity.csv");
        csvStorage.deleteFile("test_activity.csv");
    }
    
    @DisplayName("UC-001: Create New Activity Entry")
    @Nested
    class CreateEntry {
        
        @Test
        @DisplayName("should create valid entry with all fields")
        void shouldCreateValidEntryWithAllFields() {
            ActivityEntry entry = entryEditorService.createEntry(
                    "DEV", "Code review", "TODO", "Review PR #123", 0.5
            );
            
            assertNotNull(entry);
            assertEquals("DEV", entry.activityType());
            assertEquals("Code review", entry.description());
            assertEquals("TODO", entry.status());
            assertEquals("Review PR #123", entry.comment());
            assertEquals(0.5, entry.timeSpent());
            assertNotNull(entry.timestamp());
        }
        
        @Test
        @DisplayName("should handle null time spent as zero")
        void shouldHandleNullTimeSpentAsZero() {
            ActivityEntry entry = entryEditorService.createEntry(
                    "DEV", "Code review", "TODO", "Review PR #123", null
            );
            
            assertEquals(0.0, entry.timeSpent());
        }
        
        @Test
        @DisplayName("should throw exception for empty activity type")
        void shouldThrowExceptionForEmptyActivityType() {
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.createEntry("", "Test", "TODO", "Comment", 0.5);
            });
        }
        
        @Test
        @DisplayName("should throw exception for blank activity type")
        void shouldThrowExceptionForBlankActivityType() {
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.createEntry("   ", "Test", "TODO", "Comment", 0.5);
            });
        }
        
        @Test
        @DisplayName("should throw exception for empty description")
        void shouldThrowExceptionForEmptyDescription() {
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.createEntry("DEV", "", "TODO", "Comment", 0.5);
            });
        }
        
        @Test
        @DisplayName("should throw exception for blank description")
        void shouldThrowExceptionForBlankDescription() {
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.createEntry("DEV", "   ", "TODO", "Comment", 0.5);
            });
        }
        
        @Test
        @DisplayName("should throw exception for empty status")
        void shouldThrowExceptionForEmptyStatus() {
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.createEntry("DEV", "Test", "", "Comment", 0.5);
            });
        }
        
        @Test
        @DisplayName("should throw exception for blank status")
        void shouldThrowExceptionForBlankStatus() {
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.createEntry("DEV", "Test", "   ", "Comment", 0.5);
            });
        }
    }
    
    @Nested
    class SaveEntry {
        
        @Test
        @DisplayName("should save new entry to file")
        void shouldSaveNewEntryToFile() {
            ActivityEntry entry = entryEditorService.createEntry(
                    "DEV", "New task", "TODO", "Comment", 1.0
            );
            
            boolean success = entryEditorService.saveEntry(entry);
            
            assertTrue(success);
            assertTrue(csvStorage.fileExists("test_activity.csv"));
            
            List<ActivityEntry> saved = csvStorage.readEntries("test_activity.csv");
            assertEquals(1, saved.size());
            assertEquals("New task", saved.get(0).description());
        }
        
        @Test
        @DisplayName("should generate filename with current date")
        void shouldGenerateFilenameWithCurrentDate() {
            String fileName = entryEditorService.generateFileName();
            
            assertNotNull(fileName);
            assertTrue(fileName.startsWith("activity_"));
            assertTrue(fileName.endsWith(".csv"));
        }
    }
    
    @Nested
    class UpdateEntry {
        
        @Test
        @DisplayName("should update entry and persist changes")
        void shouldUpdateEntryAndPersistChanges() {
            ActivityEntry original = entryEditorService.createEntry(
                    "DEV", "Old task", "TODO", "Old comment", 0.5
            );
            entryEditorService.saveEntry(original);
            
            ActivityEntry updated = original.withUpdated(
                    "DEV", "Updated task", "DOING", "Updated comment", 1.0
            );
            
            boolean success = entryEditorService.updateEntry(original, updated);
            
            assertTrue(success);
            
            List<ActivityEntry> saved = csvStorage.readEntries("test_activity.csv");
            assertEquals(1, saved.size());
            assertEquals("Updated task", saved.get(0).description());
            assertEquals("DOING", saved.get(0).status());
            assertEquals("Updated comment", saved.get(0).comment());
            assertEquals(1.0, saved.get(0).timeSpent());
        }
        
        @Test
        @DisplayName("should validate updated entry")
        void shouldValidateUpdatedEntry() {
            ActivityEntry original = entryEditorService.createEntry(
                    "DEV", "Old task", "TODO", "Old comment", 0.5
            );
            entryEditorService.saveEntry(original);
            
            ActivityEntry invalid = original.withUpdated("DEV", "", "DOING", "", 1.0);
            
            assertThrows(IllegalArgumentException.class, () -> {
                entryEditorService.updateEntry(original, invalid);
            });
        }
    }
    
    @Nested
    class DeleteEntry {
        
        @Test
        @DisplayName("should delete entry and persist changes")
        void shouldDeleteEntryAndPersistChanges() {
            ActivityEntry entry = entryEditorService.createEntry(
                    "DEV", "Task to delete", "TODO", "Comment", 0.5
            );
            entryEditorService.saveEntry(entry);
            
            boolean success = entryEditorService.deleteEntry(entry);
            
            assertTrue(success);
            
            List<ActivityEntry> saved = csvStorage.readEntries("test_activity.csv");
            assertTrue(saved.isEmpty());
        }
    }
    
    @Nested
    class HistoryReuse {
        
        @Test
        @DisplayName("should get last N distinct descriptions")
        void shouldGetLastNDistinctDescriptions() {
            ActivityEntry e1 = new ActivityEntry("DEV", "Task A", "TODO", "", "01/01/2026 09:00", 0.5);
            ActivityEntry e2 = new ActivityEntry("DEV", "Task B", "TODO", "", "01/01/2026 10:00", 0.5);
            ActivityEntry e3 = new ActivityEntry("DEV", "Task C", "TODO", "", "01/01/2026 11:00", 0.5);
            ActivityEntry e4 = new ActivityEntry("DEV", "Task A", "TODO", "", "01/01/2026 12:00", 0.5);
            
            historyService.addEntry(e1);
            historyService.addEntry(e2);
            historyService.addEntry(e3);
            historyService.addEntry(e4);
            
            List<String> reuse = entryEditorService.getHistoryReuseDescriptions(3);
            
            assertEquals(3, reuse.size());
            assertEquals("Task A", reuse.get(0)); // Most recent
            assertEquals("Task C", reuse.get(1));
            assertEquals("Task B", reuse.get(2));
        }
    }
}
