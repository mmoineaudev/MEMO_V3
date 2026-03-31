package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SummaryService.
 */
class SummaryServiceTest {
    
    private HistoryService historyService;
    private SummaryService summaryService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
        summaryService = new SummaryService(historyService);
    }
    
    @Test
    void testGetSummaryForSpecificDate() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // Add entries for today and yesterday
        historyService.add(createEntry("Task1", "Today's task", 60, "DOING", LocalDateTime.of(today, java.time.LocalTime.MIN)));
        historyService.add(createEntry("Task2", "Yesterday's task", 45, "DONE", LocalDateTime.of(yesterday, java.time.LocalTime.MIN)));
        
        SummaryService.Summary summary = summaryService.getSummary(today);
        
        assertNotNull(summary);
        assertEquals(60, summary.getTotalTime()); // Only today should count
    }
    
    @Test
    void testGetSummaryForCurrentDate() {
        LocalDateTime now = LocalDateTime.now();
        
        historyService.add(createEntry("Task1", "Today's task", 60, "DOING", now));
        
        SummaryService.Summary summary = summaryService.getDailySummary();
        
        assertNotNull(summary);
        assertTrue(summary.getTotalTime() >= 60);
    }
    
    @Test
    void testGetDailySummaryForPreviousDay() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        historyService.add(createEntry("Task1", "Yesterday's task", 60, "DOING", yesterday));
        
        SummaryService.Summary summary = summaryService.getDailySummary();
        
        assertNotNull(summary);
        assertEquals(0, summary.getTotalTime());
    }
    
    @Test
    void testGetWeeklySummary() {
        LocalDateTime now = LocalDateTime.now();
        
        // Add entries from this week
        for (int i = 0; i < 5; i++) {
            historyService.add(createEntry("Task" + i, "Weekly task", 30, "DOING", 
                    now.minusDays(i)));
        }
        
        SummaryService.Summary summary = summaryService.getWeeklySummary();
        
        assertNotNull(summary);
        assertTrue(summary.getTotalTime() >= 150); // 5 tasks * 30 min
    }
    
    @Test
    void testGetWeeklySummaryForPreviousWeek() {
        LocalDateTime lastWeek = LocalDateTime.now().minusWeeks(1);
        
        historyService.add(createEntry("Task1", "Last week's task", 60, "DOING", lastWeek));
        
        SummaryService.Summary summary = summaryService.getWeeklySummary();
        
        assertNotNull(summary);
        assertEquals(0, summary.getTotalTime());
    }
    
    @Test
    void testGetSummaryByStatusForDate() {
        LocalDateTime now = LocalDateTime.now();
        
        historyService.add(createEntry("Task1", "Todo task", 30, "DOING", now));
        historyService.add(createEntry("Task2", "Done task", 45, "DONE", now));
        
        int doingTime = summaryService.getTimeByStatus(now.toLocalDate(), "DOING");
        
        assertEquals(30, doingTime);
    }
    
    @Test
    void testGetSummaryByActivityTypeForDate() {
        LocalDateTime now = LocalDateTime.now();
        
        historyService.add(createEntry("Development", "Dev task 1", 60, "DOING", now));
        historyService.add(createEntry("Meeting", "Meeting task", 30, "DONE", now));
        
        int devTime = summaryService.getTimeByActivityType(now.toLocalDate(), "Development");
        
        assertEquals(60, devTime);
    }
    
    @Test
    void testGetTotalTimeSpentOnDate() {
        LocalDateTime now = LocalDateTime.now();
        
        historyService.add(createEntry("Task1", "First task", 30, "DOING", now));
        historyService.add(createEntry("Task2", "Second task", 45, "DONE", now));
        historyService.add(createEntry("Task3", "Third task", 15, "DOING", now));
        
        int totalTime = summaryService.getTotalTimeSpentOnDate(now.toLocalDate());
        
        assertEquals(90, totalTime); // 30 + 45 + 15
    }
    
    @Test
    void testGetSummaryEmptyHistory() {
        SummaryService.Summary daily = summaryService.getDailySummary();
        SummaryService.Summary weekly = summaryService.getWeeklySummary();
        
        assertNotNull(daily);
        assertNotNull(weekly);
        assertEquals(0, daily.getTotalTime());
        assertEquals(0, weekly.getTotalTime());
    }
    
    @Test
    void testGetActivityCountForDate() {
        LocalDateTime now = LocalDateTime.now();
        
        historyService.add(createEntry("Task1", "First", 30, "DOING", now));
        historyService.add(createEntry("Task2", "Second", 45, "DONE", now));
        historyService.add(createEntry("Task3", "Third", 15, "DOING", now));
        
        int count = summaryService.getActivityCount(now.toLocalDate());
        
        assertEquals(3, count);
    }
    
    @Test
    void testGetDetailedSummaryForDate() {
        LocalDateTime now = LocalDateTime.now();
        
        historyService.add(createEntry("Task1", "First task", 30, "DOING", now));
        historyService.add(createEntry("Task2", "Second task", 45, "DONE", now));
        
        SummaryService.DetailedSummary summary = summaryService.getDetailedSummary(now.toLocalDate());
        
        assertNotNull(summary);
        assertTrue(summary.getTotalTime() >= 75);
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