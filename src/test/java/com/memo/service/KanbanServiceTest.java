package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KanbanService.
 */
class KanbanServiceTest {
    
    private HistoryService historyService;
    private KanbanService kanbanService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
        kanbanService = new KanbanService(historyService);
    }
    
    @Test
    void testGetEntriesByStatusForAll() {
        List<String> statuses = kanbanService.getAllStatuses();
        
        assertNotNull(statuses);
        assertTrue(statuses.contains("TODO"));
        assertTrue(statuses.contains("DOING"));
        assertTrue(statuses.contains("DONE"));
    }
    
    @Test
    void testGetEntriesByStatus() {
        historyService.add(createEntry("Task1", "First task", 30, "TODO"));
        historyService.add(createEntry("Task2", "Second task", 45, "DOING"));
        
        List<ActivityEntry> todoEntries = kanbanService.getEntriesByStatus("TODO");
        
        assertNotNull(todoEntries);
        assertEquals(1, todoEntries.size());
        assertEquals("TODO", todoEntries.get(0).status());
    }
    
    @Test
    void testGetEntriesByStatusEmpty() {
        List<ActivityEntry> doneEntries = kanbanService.getEntriesByStatus("DONE");
        
        assertNotNull(doneEntries);
        assertTrue(doneEntries.isEmpty());
    }
    
    @Test
    void testMoveEntryToNextStatus() {
        historyService.add(createEntry("Task1", "First task", 30, "TODO"));
        
        assertDoesNotThrow(() -> kanbanService.moveToNextStatus("Task1"));
        
        ActivityEntry updated = historyService.get("Task1");
        assertNotNull(updated);
        assertEquals("DOING", updated.status());
    }
    
    @Test
    void testMoveEntryToPreviousStatus() {
        historyService.add(createEntry("Task1", "First task", 30, "DOING"));
        
        assertDoesNotThrow(() -> kanbanService.moveToPreviousStatus("Task1"));
        
        ActivityEntry updated = historyService.get("Task1");
        assertNotNull(updated);
        assertEquals("TODO", updated.status());
    }
    
    @Test
    void testMoveEntryFromDoneCannotGoFurther() {
        historyService.add(createEntry("Task1", "First task", 30, "DONE"));
        
        assertDoesNotThrow(() -> kanbanService.moveToNextStatus("Task1"));
        
        ActivityEntry updated = historyService.get("Task1");
        assertNotNull(updated);
        assertEquals("DONE", updated.status()); // Should stay at DONE
    }
    
    @Test
    void testMoveEntryToTodoCannotGoFurther() {
        historyService.add(createEntry("Task1", "First task", 30, "TODO"));
        
        assertDoesNotThrow(() -> kanbanService.moveToPreviousStatus("Task1"));
        
        ActivityEntry updated = historyService.get("Task1");
        assertNotNull(updated);
        assertEquals("TODO", updated.status()); // Should stay at TODO
    }
    
    @Test
    void testGetAllStatusesReturnsUnique() {
        historyService.add(createEntry("Task1", "First", 30, "TODO"));
        historyService.add(createEntry("Task2", "Second", 45, "DOING"));
        historyService.add(createEntry("Task3", "Third", 60, "DONE"));
        
        List<String> statuses = kanbanService.getAllStatuses();
        
        assertNotNull(statuses);
        assertEquals(3, statuses.size());
    }
    
    @Test
    void testGetKanbanBoard() {
        historyService.add(createEntry("Task1", "First task", 30, "TODO"));
        historyService.add(createEntry("Task2", "Second task", 45, "DOING"));
        historyService.add(createEntry("Task3", "Third task", 60, "DONE"));
        
        KanbanService.KanbanBoard board = kanbanService.getKanbanBoard();
        
        assertNotNull(board);
        assertEquals(1, board.getEntriesByStatus("TODO").size());
        assertEquals(1, board.getEntriesByStatus("DOING").size());
        assertEquals(1, board.getEntriesByStatus("DONE").size());
    }
    
    @Test
    void testGetKanbanBoardEmpty() {
        KanbanService.KanbanBoard board = kanbanService.getKanbanBoard();
        
        assertNotNull(board);
        assertTrue(board.getEntriesByStatus("TODO").isEmpty());
        assertTrue(board.getEntriesByStatus("DOING").isEmpty());
        assertTrue(board.getEntriesByStatus("DONE").isEmpty());
    }
    
    @Test
    void testGetTaskCountByStatus() {
        historyService.add(createEntry("Task1", "First", 30, "TODO"));
        historyService.add(createEntry("Task2", "Second", 45, "TODO"));
        historyService.add(createEntry("Task3", "Third", 60, "DOING"));
        
        int todoCount = kanbanService.getTaskCountByStatus("TODO");
        int doingCount = kanbanService.getTaskCountByStatus("DOING");
        
        assertEquals(2, todoCount);
        assertEquals(1, doingCount);
    }
    
    @Test
    void testGetTaskCountForNonExistentStatus() {
        int count = kanbanService.getTaskCountByStatus("NONEXISTENT");
        
        assertEquals(0, count);
    }
    
    private ActivityEntry createEntry(String activityType, String description, int timeSpent) {
        return new ActivityEntry(activityType, description, "DOING", "", LocalDateTime.now(), timeSpent);
    }
    
    private ActivityEntry createEntry(String activityType, String description, int timeSpent, String status) {
        return new ActivityEntry(activityType, description, status, "", LocalDateTime.now(), timeSpent);
    }
}