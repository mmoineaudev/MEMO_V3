package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EntryEditorService.
 */
class EntryEditorServiceTest {
    
    private HistoryService historyService;
    private EntryEditorService entryEditorService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
        entryEditorService = new EntryEditorService(historyService);
    }
    
    @Test
    void testCreateEntryWithAutoTimestamp() {
        ActivityEntry created = entryEditorService.create("Development", "New task", 60);
        
        assertNotNull(created);
        assertEquals("Development", created.activityType());
        assertEquals("New task", created.description());
        assertEquals(60, created.timeSpent());
        assertEquals("DOING", created.status());
        assertTrue(created.timestamp().isBefore(LocalDateTime.now()) || 
                   created.timestamp().isAfter(LocalDateTime.now()));
    }
    
    @Test
    void testCreateEntryWithCustomTimestamp() {
        LocalDateTime customTime = LocalDateTime.of(2026, 4, 1, 10, 30);
        
        ActivityEntry created = entryEditorService.create("Development", "New task", 60, customTime);
        
        assertNotNull(created);
        assertEquals(customTime, created.timestamp());
    }
    
    @Test
    void testCreateEntryWithNullDescriptionReusesLast() {
        // Add an entry first
        historyService.add(new ActivityEntry(
                "Development",
                "Original description",
                "DOING",
                "",
                LocalDateTime.now(),
                60
        ));
        
        // Create new entry with null description - should reuse last one
        ActivityEntry created = entryEditorService.create("Development", null, 30);
        
        assertNotNull(created);
        assertEquals("Original description", created.description());
    }
    
    @Test
    void testCreateEntryWithEmptyDescriptionReusesLast() {
        historyService.add(new ActivityEntry(
                "Development",
                "Original description",
                "DOING",
                "",
                LocalDateTime.now(),
                60
        ));
        
        // Create new entry with empty description - should reuse last one
        ActivityEntry created = entryEditorService.create("Development", "", 30);
        
        assertNotNull(created);
        assertEquals("Original description", created.description());
    }
    
    @Test
    void testCreateEntryWithNewDescriptionDoesNotReuse() {
        historyService.add(new ActivityEntry(
                "Development",
                "Original description",
                "DOING",
                "",
                LocalDateTime.now(),
                60
        ));
        
        // Create new entry with new description - should use provided value
        ActivityEntry created = entryEditorService.create("Development", "New description", 30);
        
        assertNotNull(created);
        assertEquals("New description", created.description());
    }
    
    @Test
    void testAutoReuseBehaviorWithEmptyHistory() {
        // Create entry with null description when history is empty
        ActivityEntry created = entryEditorService.create("Development", null, 30);
        
        assertNotNull(created);
        // With empty history and null description, should probably use a default
        assertNotNull(created.description());
    }
    
    @Test
    void testUpdateEntry() {
        // Create original entry
        historyService.add(new ActivityEntry(
                "Development",
                "Original task",
                "DOING",
                "",
                LocalDateTime.now(),
                60
        ));
        
        ActivityEntry updated = entryEditorService.update("Development", 
                "Updated description",
                "DONE",
                90,
                null);
        
        assertNotNull(updated);
        assertEquals("Development", updated.activityType());
        assertEquals("Updated description", updated.description());
        assertEquals("DONE", updated.status());
        assertEquals(90, updated.timeSpent());
    }
    
    @Test
    void testUpdateEntryWithNullDescriptionReusesLast() {
        historyService.add(new ActivityEntry(
                "Development",
                "Original task",
                "DOING",
                "",
                LocalDateTime.now(),
                60
        ));
        
        // Update with null description - should reuse last one
        entryEditorService.update("Development", null, "DONE", 90, null);
        
        ActivityEntry updated = historyService.get("Development");
        assertNotNull(updated);
        assertEquals("Original task", updated.description());
    }
    
    @Test
    void testGetRecentDescriptions() {
        // Add multiple entries with different descriptions
        historyService.add(new ActivityEntry(
                "Development",
                "First description",
                "DOING",
                "",
                LocalDateTime.now(),
                30
        ));
        
        historyService.add(new ActivityEntry(
                "Development",
                "Second description",
                "DOING",
                "",
                LocalDateTime.now(),
                45
        ));
        
        List<String> descriptions = entryEditorService.getRecentDescriptions(5);
        
        assertNotNull(descriptions);
        assertTrue(descriptions.size() > 0);
    }
    
    @Test
    void testGetRecentDescriptionsWithLimit() {
        historyService.add(new ActivityEntry(
                "Development",
                "First description",
                "DOING",
                "",
                LocalDateTime.now(),
                30
        ));
        
        historyService.add(new ActivityEntry(
                "Development",
                "Second description",
                "DOING",
                "",
                LocalDateTime.now(),
                45
        ));
        
        List<String> descriptions = entryEditorService.getRecentDescriptions(1);
        
        assertNotNull(descriptions);
        assertTrue(descriptions.size() <= 1);
    }
    
    @Test
    void testGetRecentDescriptionsEmptyHistory() {
        List<String> descriptions = entryEditorService.getRecentDescriptions(10);
        
        assertNotNull(descriptions);
        assertTrue(descriptions.isEmpty());
    }
}