package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CsvStorageService.
 * Tests CSV serialization, deserialization, and sanitization functionality.
 */
class CsvStorageServiceTest {
    
    @TempDir
    Path tempDir;
    
    private CsvStorageService storageService;
    
    @BeforeEach
    void setUp() {
        storageService = new CsvStorageService(tempDir.toString());
    }
    
    @Test
    void testWriteEntryToCsvFile() throws Exception {
        ActivityEntry entry = new ActivityEntry("Development", "Fix bug in login module", "TODO", "Check auth logic", 30);
        
        Path expectedFile = tempDir.resolve("2026-03-31.csv");
        storageService.save(entry);
        
        assertTrue(Files.exists(expectedFile));
    }
    
    @Test
    void testWriteMultipleEntriesSeparateFiles() throws Exception {
        ActivityEntry entry1 = new ActivityEntry("Development", "First task", "DONE", "", 60);
        ActivityEntry entry2 = new ActivityEntry("Review", "Second task", "DOING", "", 45);
        
        storageService.save(entry1);
        storageService.save(entry2);
        
        Path file1 = tempDir.resolve("2026-03-31.csv");
        Path file2 = tempDir.resolve("2026-04-01.csv");
        
        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));
        assertEquals(1, Files.lines(file1).count());
        assertEquals(1, Files.lines(file2).count());
    }
    
    @Test
    void testReadAllEntriesFromMultipleFiles() throws Exception {
        ActivityEntry entry1 = new ActivityEntry("Development", "Task 1", "DONE", "", 60);
        ActivityEntry entry2 = new ActivityEntry("Review", "Task 2", "DOING", "", 45);
        
        storageService.save(entry1);
        storageService.save(entry2);
        
        List<ActivityEntry> entries = storageService.loadAll();
        
        assertEquals(2, entries.size());
        
        ActivityEntry loaded1 = entries.get(0);
        ActivityEntry loaded2 = entries.get(1);
        
        assertEquals("Development", loaded1.activityType());
        assertEquals("Task 1", loaded1.description());
        assertEquals("DONE", loaded1.status());
        assertEquals(60, loaded1.timeSpent());
        
        assertEquals("Review", loaded2.activityType());
        assertEquals("Task 2", loaded2.description());
        assertEquals("DOING", loaded2.status());
        assertEquals(45, loaded2.timeSpent());
    }
    
    @Test
    void testMultiLineCommentEncoding() throws Exception {
        String multilineComment = "First line\nSecond line\nThird line";
        
        ActivityEntry entry = new ActivityEntry("Note", "Important note", "NOTE", multilineComment, 0);
        
        storageService.save(entry);
        List<ActivityEntry> entries = storageService.loadAll();
        
        assertEquals(1, entries.size());
        assertEquals(multilineComment, entries.get(0).comment());
    }
    
    @Test
    void testSemicolonSanitization() throws Exception {
        ActivityEntry entry = new ActivityEntry("Development", "Task with;semicolon", "TODO", "Comments with;semicolons too", 30);
        
        storageService.save(entry);
        List<ActivityEntry> entries = storageService.loadAll();
        
        assertEquals(1, entries.size());
    }
    
    @Test
    void testHandleEmptyComment() throws Exception {
        ActivityEntry entry = new ActivityEntry("Development", "Task with no comment", "DONE", "", 45);
        
        storageService.save(entry);
        List<ActivityEntry> entries = storageService.loadAll();
        
        assertEquals(1, entries.size());
        assertEquals("", entries.get(0).comment());
    }
    
    @Test
    void testHandleSpecialCharacters() throws Exception {
        ActivityEntry entry = new ActivityEntry("Development", "Task with special chars: @#$%^&*()", "TODO", "More special chars: \\t \\n \"\\'", 60);
        
        storageService.save(entry);
        List<ActivityEntry> entries = storageService.loadAll();
        
        assertEquals(1, entries.size());
        assertTrue(entries.get(0).isValid());
    }
    
    @Test
    void testStorageDirectoryCreation() throws Exception {
        Path newDir = tempDir.resolve("created_dir");
        CsvStorageService service = new CsvStorageService(newDir.toString());
        
        assertFalse(Files.exists(newDir));
        
        ActivityEntry entry = new ActivityEntry("Development", "Test task", "TODO", "", 30);
        service.save(entry);
        
        assertTrue(Files.exists(newDir));
    }
    
    @Test
    void testReadFromNonexistentDirectory() throws Exception {
        Path nonExistentDir = tempDir.resolve("nonexistent");
        CsvStorageService service = new CsvStorageService(nonExistentDir.toString());
        
        assertDoesNotThrow(() -> service.loadAll());
        assertTrue(service.loadAll().isEmpty());
    }
    
    @Test
    void testRoundTripPreservesData() throws Exception {
        ActivityEntry original = new ActivityEntry("Development", "Round trip task", "DOING", "Original comment", 120);
        
        storageService.save(original);
        List<ActivityEntry> loaded = storageService.loadAll();
        
        assertEquals(1, loaded.size());
        ActivityEntry restored = loaded.get(0);
        
        assertEquals(original.activityType(), restored.activityType());
        assertEquals(original.description(), restored.description());
        assertEquals(original.status(), restored.status());
        assertEquals(original.comment(), restored.comment());
        assertEquals(original.timeSpent(), restored.timeSpent());
    }
    
    @Test
    void testWriteAllAndReadAllPreservingOrder() throws Exception {
        ActivityEntry entry1 = new ActivityEntry("Task", "First entry", "DONE", "", 60);
        ActivityEntry entry2 = new ActivityEntry("Task", "Second entry", "DOING", "", 90);
        ActivityEntry entry3 = new ActivityEntry("Task", "Third entry", "TODO", "", 120);
        
        storageService.save(entry1);
        storageService.save(entry2);
        storageService.save(entry3);
        
        List<ActivityEntry> entries = storageService.loadAll();
        
        assertEquals(3, entries.size());
        assertEquals("First entry", entries.get(0).description());
        assertEquals("Second entry", entries.get(1).description());
        assertEquals("Third entry", entries.get(2).description());
    }
}