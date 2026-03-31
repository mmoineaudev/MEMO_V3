package com.memo.service;

import com.memo.model.ActivityEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TimeCalculationService.
 */
class TimeCalculationServiceTest {
    
    private HistoryService historyService;
    private TimeCalculationService timeCalcService;
    
    @BeforeEach
    void setUp() {
        historyService = new HistoryService();
        timeCalcService = new TimeCalculationService(historyService);
    }
    
    @Test
    void testFormatMinutesToHoursAndMinutes() {
        String formatted = timeCalcService.formatTime(125);
        
        assertEquals("2h 5m", formatted);
    }
    
    @Test
    void testFormatMinutesToHoursOnly() {
        String formatted = timeCalcService.formatTime(180);
        
        assertEquals("3h 0m", formatted);
    }
    
    @Test
    void testFormatMinutesLessThanOneHour() {
        String formatted = timeCalcService.formatTime(45);
        
        assertEquals("0h 45m", formatted);
    }
    
    @Test
    void testFormatZeroMinutes() {
        String formatted = timeCalcService.formatTime(0);
        
        assertEquals("0h 0m", formatted);
    }
    
    @Test
    void testCalculateTotalTimeSpent() {
        historyService.add(createEntry("Task1", "First task", 30));
        historyService.add(createEntry("Task2", "Second task", 45));
        historyService.add(createEntry("Task3", "Third task", 60));
        
        int totalTime = timeCalcService.calculateTotalTimeSpent();
        
        assertEquals(135, totalTime);
    }
    
    @Test
    void testCalculateTotalTimeSpentEmpty() {
        int totalTime = timeCalcService.calculateTotalTimeSpent();
        
        assertEquals(0, totalTime);
    }
    
    @Test
    void testCalculateAverageTimePerEntry() {
        historyService.add(createEntry("Task1", "First task", 30));
        historyService.add(createEntry("Task2", "Second task", 60));
        
        double average = timeCalcService.calculateAverageTimePerEntry();
        
        assertEquals(45.0, average);
    }
    
    @Test
    void testCalculateAverageTimePerEntryEmpty() {
        double average = timeCalcService.calculateAverageTimePerEntry();
        
        assertEquals(0.0, average);
    }
    
    @Test
    void testGetTimeByActivityType() {
        historyService.add(createEntry("Development", "Dev task 1", 60));
        historyService.add(createEntry("Meeting", "Meeting task", 30));
        historyService.add(createEntry("Development", "Dev task 2", 45));
        
        int devTime = timeCalcService.getTimeByActivityType("Development");
        
        assertEquals(105, devTime);
    }
    
    @Test
    void testGetTimeByStatus() {
        historyService.add(createEntry("Task1", "First task", 30, "DONE"));
        historyService.add(createEntry("Task2", "Second task", 45, "DOING"));
        historyService.add(createEntry("Task3", "Third task", 60, "DONE"));
        
        int doneTime = timeCalcService.getTimeByStatus("DONE");
        
        assertEquals(90, doneTime);
    }
    
    @Test
    void testGetTimeByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        
        // Add entries with specific times
        historyService.add(createEntry("Task1", "First task", 30, "DOING", 
                LocalDateTime.of(now.toLocalDate().minusDays(1), java.time.LocalTime.MIN)));
        historyService.add(createEntry("Task2", "Second task", 45, "DONE", 
                LocalDateTime.of(now.toLocalDate(), java.time.LocalTime.MIN)));
        historyService.add(createEntry("Task3", "Third task", 60, "DOING", 
                LocalDateTime.of(now.toLocalDate().minusDays(2), java.time.LocalTime.MIN)));
        
        int totalTime = timeCalcService.getTimeByDateRange(
                LocalDateTime.of(now.toLocalDate(), java.time.LocalTime.MIN),
                LocalDateTime.of(now.toLocalDate().plusDays(1), java.time.LocalTime.MIN)
        );
        
        assertEquals(45, totalTime); // Only today's entry
    }
    
    @Test
    void testGetTimeByDateRangeEmpty() {
        LocalDateTime now = LocalDateTime.now();
        
        int totalTime = timeCalcService.getTimeByDateRange(now.minusDays(10), now.minusDays(8));
        
        assertEquals(0, totalTime);
    }
    
    @Test
    void testFormatTimeWithLargeValues() {
        String formatted = timeCalcService.formatTime(3665); // 61 hours 5 minutes
        
        assertEquals("61h 5m", formatted);
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