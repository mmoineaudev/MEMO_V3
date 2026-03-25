package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HistoryService Tests")
class HistoryServiceTest {
    
    private static Path testStorageDir;
    private static CsvStorageService csvStorage;
    private static HistoryService historyService;
    
    @BeforeAll
    static void setup() throws IOException {
        testStorageDir = Files.createTempDirectory("memo_history_test_");
        csvStorage = new CsvStorageService(testStorageDir.toString());
        historyService = new HistoryService(csvStorage);
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
        csvStorage.deleteFile("activity_test.csv");
    }
    
    @DisplayName("UC-011: Load All History on Startup")
    @Nested
    class LoadHistory {
        
        @Test
        @DisplayName("should load entries from CSV files")
        void shouldLoadEntriesFromCsvFiles() {
            // Create test data with explicit timestamps
            ActivityEntry e1 = new ActivityEntry("DEV", "Task 1", "TODO", "Comment", "01/01/2026 09:00", 0.5);
            ActivityEntry e2 = new ActivityEntry("DEV", "Task 2", "DOING", "Comment", "25/03/2026 10:00", 1.0);
            
            csvStorage.appendToFile("activity_test.csv", e1.toCsv());
            csvStorage.appendToFile("activity_test.csv", e2.toCsv());
            
            // Load history
            List<ActivityEntry> loaded = historyService.loadAllHistory();
            
            assertEquals(2, loaded.size());
            assertEquals("Task 2", loaded.get(0).description()); // Newest first
            assertEquals("Task 1", loaded.get(1).description());
        }
        
        @Test
        @DisplayName("should return empty list when no CSV files exist")
        void shouldReturnEmptyListWhenNoCsvFilesExist() {
            List<ActivityEntry> loaded = historyService.loadAllHistory();
            
            assertTrue(loaded.isEmpty());
        }
        
        @Test
        @DisplayName("should sort entries by timestamp newest first")
        void shouldSortEntriesByTimestampNewestFirst() {
            // Manually create entries with different timestamps
            ActivityEntry e1 = new ActivityEntry("DEV", "Old", "TODO", "", "01/01/2026 09:00", 0.5);
            ActivityEntry e2 = new ActivityEntry("DEV", "New", "TODO", "", "25/03/2026 10:00", 1.0);
            
            csvStorage.appendToFile("activity_test.csv", e1.toCsv());
            csvStorage.appendToFile("activity_test.csv", e2.toCsv());
            
            List<ActivityEntry> loaded = historyService.loadAllHistory();
            
            assertEquals("New", loaded.get(0).description());
            assertEquals("Old", loaded.get(1).description());
        }
        
        @Test
        @DisplayName("should ignore invalid CSV lines")
        void shouldIgnoreInvalidCsvLines() {
            csvStorage.appendToFile("activity_test.csv", ""); // Empty line
            csvStorage.appendToFile("activity_test.csv", "DEV;Valid;TODO;;25/03/2026 09:00;0.5");
            
            // Should only load valid entries (1 valid, 1 empty/invalid)
            List<ActivityEntry> loaded = historyService.loadAllHistory();
            
            assertEquals(1, loaded.size());
            assertEquals("Valid", loaded.get(0).description());
        }
    }
    
    @Nested
    class HistoryManagement {
        
        @Test
        @DisplayName("should add entry to history")
        void shouldAddEntryToHistory() {
            ActivityEntry entry = ActivityEntry.createNew("DEV", "New Task", "TODO", "Comment", 0.5);
            
            historyService.addEntry(entry);
            
            List<ActivityEntry> history = historyService.getHistory();
            assertEquals(1, history.size());
            assertEquals("New Task", history.get(0).description());
        }
        
        @Test
        @DisplayName("should remove entry from history")
        void shouldRemoveEntryFromHistory() {
            ActivityEntry entry = ActivityEntry.createNew("DEV", "To Remove", "TODO", "", 0.5);
            historyService.addEntry(entry);
            
            historyService.removeEntry(entry);
            
            List<ActivityEntry> history = historyService.getHistory();
            assertTrue(history.isEmpty());
        }
        
        @Test
        @DisplayName("should update entry in history")
        void shouldUpdateEntryInHistory() {
            ActivityEntry oldEntry = ActivityEntry.createNew("DEV", "Old Desc", "TODO", "Old Comment", 0.5);
            historyService.addEntry(oldEntry);
            
            ActivityEntry newEntry = oldEntry.withUpdated("DEV", "New Desc", "DOING", "New Comment", 1.0);
            historyService.updateEntry(oldEntry, newEntry);
            
            List<ActivityEntry> history = historyService.getHistory();
            assertEquals(1, history.size());
            assertEquals("New Desc", history.get(0).description());
            assertEquals("DOING", history.get(0).status());
            assertEquals("New Comment", history.get(0).comment());
            assertEquals(1.0, history.get(0).timeSpent());
        }
        
        @Test
        @DisplayName("should save all history to CSV file")
        void shouldSaveAllHistoryToCsvFile() {
            ActivityEntry e1 = ActivityEntry.createNew("DEV", "Task 1", "TODO", "Comment 1", 0.5);
            ActivityEntry e2 = ActivityEntry.createNew("DEV", "Task 2", "DOING", "Comment 2", 1.0);
            
            historyService.addEntry(e1);
            historyService.addEntry(e2);
            
            boolean success = historyService.saveAllHistory("activity_test.csv");
            
            assertTrue(success);
            assertTrue(csvStorage.fileExists("activity_test.csv"));
            
            List<ActivityEntry> saved = csvStorage.readEntries("activity_test.csv");
            assertEquals(2, saved.size());
        }
        
        @Test
        @DisplayName("should get unique descriptions")
        void shouldGetUniqueDescriptions() {
            ActivityEntry e1 = ActivityEntry.createNew("DEV", "Task 1", "TODO", "", 0.5);
            ActivityEntry e2 = ActivityEntry.createNew("DEV", "Task 2", "DOING", "", 1.0);
            ActivityEntry e3 = ActivityEntry.createNew("DEV", "Task 1", "DONE", "", 0.5);
            
            historyService.addEntry(e1);
            historyService.addEntry(e2);
            historyService.addEntry(e3);
            
            List<String> descriptions = historyService.getUniqueDescriptions();
            
            assertEquals(2, descriptions.size());
            assertTrue(descriptions.contains("Task 1"));
            assertTrue(descriptions.contains("Task 2"));
        }
        
        @Test
        @DisplayName("should get last N distinct descriptions")
        void shouldGetLastNDistinctDescriptions() {
            ActivityEntry e1 = ActivityEntry.createNew("DEV", "Task A", "TODO", "", 0.5);
            ActivityEntry e2 = ActivityEntry.createNew("DEV", "Task B", "TODO", "", 0.5);
            ActivityEntry e3 = ActivityEntry.createNew("DEV", "Task C", "TODO", "", 0.5);
            ActivityEntry e4 = ActivityEntry.createNew("DEV", "Task A", "TODO", "", 0.5); // Duplicate
            
            historyService.addEntry(e1);
            historyService.addEntry(e2);
            historyService.addEntry(e3);
            historyService.addEntry(e4);
            
            List<String> lastDistinct = historyService.getLastNDistinctDescriptions(2);
            
            assertEquals(2, lastDistinct.size());
            assertEquals("Task A", lastDistinct.get(0)); // Most recent
            assertEquals("Task C", lastDistinct.get(1));
        }
        
        @Test
        @DisplayName("should clear all history")
        void shouldClearAllHistory() {
            historyService.addEntry(ActivityEntry.createNew("DEV", "Task 1", "TODO", "", 0.5));
            historyService.addEntry(ActivityEntry.createNew("DEV", "Task 2", "TODO", "", 0.5));
            
            historyService.clearHistory();
            
            assertTrue(historyService.getHistory().isEmpty());
        }
    }
}
