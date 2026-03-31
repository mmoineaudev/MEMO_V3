package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HistoryService.
 */
class HistoryServiceTest {
    
    private HistoryService historyService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
    }
    
    @Test
    void testAddEntry() {
        ActivityEntry entry = createTestEntry("Development", "Test task", 30);
        
        assertDoesNotThrow(() -> historyService.add(entry));
    }
    
    @Test
    void testGetAllReturnsEmptyListInitially() {
        List<ActivityEntry> entries = historyService.getAll();
        
        assertTrue(entries.isEmpty());
    }
    
    @Test
    void testAddAndGetMultipleEntries() {
        ActivityEntry entry1 = createTestEntry("Development", "First task", 30);
        ActivityEntry entry2 = createTestEntry("Review", "Second task", 45);
        
        historyService.add(entry1);
        historyService.add(entry2);
        
        List<ActivityEntry> entries = historyService.getAll();
        
        assertEquals(2, entries.size());
        assertEquals("First task", entries.get(0).description());
        assertEquals("Second task", entries.get(1).description());
    }
    
    @Test
    void testGetEntryById() {
        ActivityEntry entry = createTestEntry("Development", "Target task", 60);
        
        historyService.add(entry);
        String id = entry.activityType(); // Using activityType as ID for simplicity
        
        ActivityEntry retrieved = historyService.get(id);
        
        assertNotNull(retrieved);
        assertEquals("Target task", retrieved.description());
    }
    
    @Test
    void testGetNonExistingEntryReturnsNull() {
        ActivityEntry retrieved = historyService.get("NonExistent");
        
        assertNull(retrieved);
    }
    
    @Test
    void testUpdateEntry() {
        ActivityEntry entry = createTestEntry("Development", "Original task", 30);
        
        historyService.add(entry);
        
        ActivityEntry updated = new ActivityEntry(
                "Development",
                "Updated task",
                "DONE",
                "Comment",
                LocalDateTime.now(),
                45
        );
        
        assertDoesNotThrow(() -> historyService.update(updated));
        
        ActivityEntry retrieved = historyService.get("Development");
        assertNotNull(retrieved);
        assertEquals("Updated task", retrieved.description());
    }
    
    @Test
    void testDeleteEntry() {
        ActivityEntry entry = createTestEntry("Development", "Task to delete", 30);
        
        historyService.add(entry);
        assertTrue(historyService.getAll().size() == 1);
        
        assertDoesNotThrow(() -> historyService.delete(entry.activityType()));
        
        List<ActivityEntry> remaining = historyService.getAll();
        assertEquals(0, remaining.size());
    }
    
    @Test
    void testClearAllEntries() {
        historyService.add(createTestEntry("Task1", "First", 30));
        historyService.add(createTestEntry("Task2", "Second", 45));
        
        assertDoesNotThrow(() -> historyService.clear());
        
        assertTrue(historyService.getAll().isEmpty());
    }
    
    private ActivityEntry createTestEntry(String activityType, String description, int timeSpent) {
        return new ActivityEntry(activityType, description, "DOING", "", LocalDateTime.now(), timeSpent);
    }
}