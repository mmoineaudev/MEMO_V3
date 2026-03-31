package com.memo.service;

import com.memo.model.ActivityEntry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for time calculations and formatting.
 */
public class TimeCalculationService {
    
    private final HistoryService historyService;
    
    public TimeCalculationService(HistoryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Formats minutes into a human-readable format (e.g., "2h 30m").
     * 
     * @param minutes Total minutes to format
     * @return Formatted time string
     */
    public String formatTime(int minutes) {
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        
        return hours + "h " + remainingMinutes + "m";
    }
    
    /**
     * Calculates total time spent across all entries.
     * 
     * @return Total time in minutes
     */
    public int calculateTotalTimeSpent() {
        List<ActivityEntry> entries = historyService.getAll();
        
        return entries.stream()
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Calculates average time per entry.
     * 
     * @return Average time in minutes, or 0 if no entries
     */
    public double calculateAverageTimePerEntry() {
        List<ActivityEntry> entries = historyService.getAll();
        
        if (entries.isEmpty()) {
            return 0.0;
        }
        
        return entries.stream()
                .mapToInt(ActivityEntry::timeSpent)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Gets total time spent for a specific activity type.
     * 
     * @param activityType The activity type to calculate
     * @return Total time in minutes
     */
    public int getTimeByActivityType(String activityType) {
        if (activityType == null || activityType.trim().isEmpty()) {
            return 0;
        }
        
        String lowerType = activityType.toLowerCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.activityType().toLowerCase().equals(lowerType))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Gets total time spent for a specific status.
     * 
     * @param status The status to calculate (e.g., "DONE", "DOING")
     * @return Total time in minutes
     */
    public int getTimeByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return 0;
        }
        
        String lowerStatus = status.toUpperCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.status().equals(lowerStatus))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Gets total time spent within a date range.
     * 
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Total time in minutes
     */
    public int getTimeByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        
        return historyService.getAll().stream()
                .filter(entry -> !entry.timestamp().isBefore(startDate) && !entry.timestamp().isAfter(endDate))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Gets total time spent on a specific date.
     * 
     * @param date The date to calculate
     * @return Total time in minutes
     */
    public int getTimeByDate(LocalDate date) {
        if (date == null) {
            return 0;
        }
        
        return historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
}