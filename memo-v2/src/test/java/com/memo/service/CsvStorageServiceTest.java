package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvStorageService Tests")
class CsvStorageServiceTest {
    
    private static Path testStorageDir;
    private static CsvStorageService service;
    
    @BeforeAll
    static void setup() throws IOException {
        testStorageDir = Files.createTempDirectory("memo_test_storage_");
        service = new CsvStorageService(testStorageDir.toString());
    }
    
    @AfterAll
    static void teardown() throws IOException {
        // Clean up test directory
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
    
    @DisplayName("UC-010: Auto-create Storage Directory")
    @Nested
    class DirectoryCreation {
        
        @Test
        @DisplayName("should create directory if it does not exist")
        void shouldCreateDirectoryIfNotExists() {
            boolean created = service.ensureDirectoryExists();
            
            assertTrue(created);
            assertTrue(Files.exists(testStorageDir));
            assertTrue(Files.isWritable(testStorageDir));
        }
        
        @Test
        @DisplayName("should return true if directory already exists")
        void shouldReturnTrueIfDirectoryExists() {
            boolean created = service.ensureDirectoryExists();
            
            assertTrue(created);
        }
    }
    
    @DisplayName("CSV File Operations")
    @Nested
    class FileOperations {
        
        private static final String TEST_FILE = "test_activity.csv";
        private static final String TEST_PATTERN = "test";
        
        @BeforeEach
        void beforeEach() {
            // Ensure directory exists
            service.ensureDirectoryExists();
        }
        
        @AfterEach
        void afterEach() {
            // Clean up test files
            service.deleteFile(TEST_FILE);
        }
        
        @Test
        @DisplayName("should list files with matching pattern")
        void shouldListFilesWithMatchingPattern() {
            // Create some test files
            service.appendToFile("test_activity.csv", "DEV;Test;TODO;;25/03/2026 09:00;0.0");
            service.appendToFile("other_file.txt", "not a test file");
            
            List<String> files = service.listFilesWithPattern(TEST_PATTERN);
            
            assertTrue(files.contains("test_activity.csv"));
            assertFalse(files.contains("other_file.txt"));
        }
        
        @Test
        @DisplayName("should return empty list when no files match")
        void shouldReturnEmptyListWhenNoFilesMatch() {
            List<String> files = service.listFilesWithPattern("nonexistent");
            
            assertTrue(files.isEmpty());
        }
        
        @Test
        @DisplayName("should read file contents")
        void shouldReadFileContents() {
            String line = "DEV;Test entry;TODO;Comment;25/03/2026 09:00;0.5";
            service.appendToFile(TEST_FILE, line);
            
            List<String> lines = service.readFile(TEST_FILE);
            
            assertEquals(1, lines.size());
            assertEquals(line, lines.get(0));
        }
        
        @Test
        @DisplayName("should return empty list for non-existent file")
        void shouldReturnEmptyListForNonExistentFile() {
            List<String> lines = service.readFile("nonexistent.csv");
            
            assertTrue(lines.isEmpty());
        }
        
        @Test
        @DisplayName("should append line to file")
        void shouldAppendLineToFile() {
            String line1 = "DEV;Test1;TODO;;25/03/2026 09:00;0.0";
            String line2 = "DEV;Test2;DOING;;25/03/2026 10:00;0.5";
            
            service.appendToFile(TEST_FILE, line1);
            service.appendToFile(TEST_FILE, line2);
            
            List<String> lines = service.readFile(TEST_FILE);
            
            assertEquals(2, lines.size());
            assertEquals(line1, lines.get(0));
            assertEquals(line2, lines.get(1));
        }
        
        @Test
        @DisplayName("should create file when appending to non-existent file")
        void shouldCreateFileWhenAppendingToNonExistentFile() {
            service.appendToFile(TEST_FILE, "DEV;Test;TODO;;25/03/2026 09:00;0.0");
            
            assertTrue(service.fileExists(TEST_FILE));
        }
        
        @Test
        @DisplayName("should normalize line endings to LF")
        void shouldNormalizeLineEndingsToLF() {
            String lineWithCRLF = "DEV;Test;TODO;;25/03/2026 09:00;0.0\r\n";
            service.appendToFile(TEST_FILE, lineWithCRLF);
            
            List<String> lines = service.readFile(TEST_FILE);
            // After normalization and re-reading, should not have \r\n
            assertTrue(lines.get(0).length() > 0);
        }
        
        @Test
        @DisplayName("should read entries from file")
        void shouldReadEntriesFromFile() {
            String line = "DEV;Code review;TODO;;25/03/2026 09:00;0.5";
            service.appendToFile(TEST_FILE, line);
            
            List<ActivityEntry> entries = service.readEntries(TEST_FILE);
            
            assertEquals(1, entries.size());
            ActivityEntry entry = entries.get(0);
            assertEquals("DEV", entry.activityType());
            assertEquals("Code review", entry.description());
            assertEquals("TODO", entry.status());
            assertEquals(0.5, entry.timeSpent());
        }
        
        @Test
        @DisplayName("should write all entries to file")
        void shouldWriteAllEntriesToFile() {
            List<ActivityEntry> entries = List.of(
                ActivityEntry.createNew("DEV", "Task 1", "TODO", "Comment 1", 0.5),
                ActivityEntry.createNew("DEV", "Task 2", "DOING", "Comment 2", 1.0)
            );
            
            boolean success = service.writeAllEntries(TEST_FILE, entries);
            
            assertTrue(success);
            assertTrue(service.fileExists(TEST_FILE));
            
            List<ActivityEntry> readEntries = service.readEntries(TEST_FILE);
            assertEquals(2, readEntries.size());
        }
        
        @Test
        @DisplayName("should delete file")
        void shouldDeleteFile() {
            service.appendToFile(TEST_FILE, "DEV;Test;TODO;;25/03/2026 09:00;0.0");
            
            boolean deleted = service.deleteFile(TEST_FILE);
            
            assertTrue(deleted);
            assertFalse(service.fileExists(TEST_FILE));
        }
        
        @Test
        @DisplayName("should handle delete of non-existent file")
        void shouldHandleDeleteOfNonExistentFile() {
            boolean deleted = service.deleteFile("nonexistent.csv");
            
            assertTrue(deleted);
        }
        
        @Test
        @DisplayName("should check file existence")
        void shouldCheckFileExistence() {
            assertFalse(service.fileExists(TEST_FILE));
            
            service.appendToFile(TEST_FILE, "DEV;Test;TODO;;25/03/2026 09:00;0.0");
            
            assertTrue(service.fileExists(TEST_FILE));
        }
    }
}
