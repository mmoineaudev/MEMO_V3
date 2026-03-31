package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SearchService.
 */
class SearchServiceTest {
    
    private HistoryService historyService;
    private SearchService searchService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
        searchService = new SearchService(historyService);
    }
    
    @Test
    void testSearchByDescription() {
        historyService.add(createEntry("Development", "Implement feature X", 60));
        historyService.add(createEntry("Review", "Code review", 30));
        
        List<ActivityEntry> results = searchService.searchByDescription("feature");
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.get(0).description().contains("feature"));
    }
    
    @Test
    void testSearchByDescriptionCaseInsensitive() {
        historyService.add(createEntry("Development", "Implement feature X", 60));
        
        List<ActivityEntry> results = searchService.searchByDescription("FEATURE");
        
        assertNotNull(results);
        assertEquals(1, results.size());
    }
    
    @Test
    void testSearchByActivityType() {
        historyService.add(createEntry("Development", "Task 1", 30));
        historyService.add(createEntry("Meeting", "Team standup", 60));
        
        List<ActivityEntry> results = searchService.searchByActivityType("Development");
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Development", results.get(0).activityType());
    }
    
    @Test
    void testSearchByStatus() {
        historyService.add(createEntry("Task1", "First task", 30, "DONE"));
        historyService.add(createEntry("Task2", "Second task", 45, "DOING"));
        
        List<ActivityEntry> results = searchService.searchByStatus("DONE");
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("DONE", results.get(0).status());
    }
    
    @Test
    void testSearchMultipleCriteriaCombined() {
        historyService.add(createEntry("Development", "Implement feature X", 60, "DOING"));
        historyService.add(createEntry("Meeting", "Discuss project Y", 30, "DONE"));
        
        List<ActivityEntry> results = searchService.searchByDescriptionAndActivityType("feature", "Development");
        
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Development", results.get(0).activityType());
    }
    
    @Test
    void testSearchNoResults() {
        historyService.add(createEntry("Task1", "First task", 30));
        
        List<ActivityEntry> results = searchService.searchByDescription("nonexistent");
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testSearchEmptyHistoryReturnsEmptyList() {
        List<ActivityEntry> results = searchService.searchByDescription("anything");
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testFilterByTimeRangeWithinRange() {
        LocalDateTime now = LocalDateTime.now();
        historyService.add(createEntry("Task1", "First task", 30, "DOING", now.minusDays(1)));
        historyService.add(createEntry("Task2", "Second task", 45, "DOING", now.plusDays(1)));
        
        List<ActivityEntry> results = searchService.searchByDateRange(
                now.minusDays(3),
                now.plusDays(3)
        );
        
        assertNotNull(results);
        assertEquals(2, results.size());
    }
    
    @Test
    void testFilterByTimeRangeBeforeRange() {
        LocalDateTime now = LocalDateTime.now();
        historyService.add(createEntry("Task1", "First task", 30, "DOING", now.minusDays(5)));
        
        List<ActivityEntry> results = searchService.searchByDateRange(
                now,
                now.plusDays(3)
        );
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testFilterByTimeRangeAfterRange() {
        LocalDateTime now = LocalDateTime.now();
        historyService.add(createEntry("Task1", "First task", 30, "DOING", now.plusDays(5)));
        
        List<ActivityEntry> results = searchService.searchByDateRange(
                now.minusDays(3),
                now
        );
        
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testGetTotalTimeSpentForEntries() {
        LocalDateTime now = LocalDateTime.now();
        historyService.add(createEntry("Task1", "First task", 30, "DOING", now));
        historyService.add(createEntry("Task2", "Second task", 45, "DOING", now));
        
        List<ActivityEntry> entries = historyService.getAll();
        int total = searchService.getTotalTimeSpent(List.of(entries.get(0), entries.get(1)));
        
        assertEquals(75, total);
    }
    
    @Test
    void testGetTotalTimeSpentForEmptyList() {
        List<ActivityEntry> emptyList = List.of();
        int total = searchService.getTotalTimeSpent(emptyList);
        
        assertEquals(0, total);
    }
    
    @Test
    void testAdvancedSearchWithAllFilters() {
        LocalDateTime now = LocalDateTime.now();
        historyService.add(createEntry("Development", "Implement feature X", 60, "DOING", now.minusHours(1)));
        historyService.add(createEntry("Review", "Code review document", 30, "DONE", now.plusHours(1)));
        
        List<ActivityEntry> results = searchService.advancedSearch(
                "feature",
                "Development",
                "DOING",
                now.minusDays(3),
                now.plusDays(3)
        );
        
        assertNotNull(results);
        assertEquals(1, results.size());
    }
    
    private ActivityEntry createEntry(String activityType, String description, int timeSpent) {
        return new ActivityEntry(activityType, description, "DOING", "", LocalDateTime.now(), timeSpent);
    }
    
    private ActivityEntry createEntry(String activityType, String description, int timeSpent, String status) {
        return new ActivityEntry(activityType, description, status, "", LocalDateTime.now(), timeSpent);
    }
    
    private ActivityEntry createEntry(String activityType, String description, int timeSpent, String status, LocalDateTime timestamp) {
        return new ActivityEntry(activityType, description, status, "", timestamp, timeSpent);
    }
}