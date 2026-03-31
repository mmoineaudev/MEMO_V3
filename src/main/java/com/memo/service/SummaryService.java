package com.memo.service;

import com.memo.model.ActivityEntry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating summaries and time calculations.
 */
public class SummaryService {
    
    private final HistoryService historyService;
    
    public SummaryService(HistoryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Gets a summary for a specific date.
     * 
     * @param date The date to summarize
     * @return Summary object with time statistics
     */
    public Summary getSummary(LocalDate date) {
        List<ActivityEntry> entries = historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .toList();
        
        return new Summary(
                date,
                entries.stream().mapToInt(ActivityEntry::timeSpent).sum(),
                entries.size()
        );
    }
    
    /**
     * Gets a daily summary for today.
     * 
     * @return Daily summary for current date
     */
    public Summary getDailySummary() {
        return getSummary(LocalDate.now());
    }
    
    /**
     * Gets a weekly summary for the last 7 days.
     * 
     * @return Weekly summary with total time and activity count
     */
    public Summary getWeeklySummary() {
        LocalDate today = LocalDate.now();
        
        List<ActivityEntry> entries = historyService.getAll().stream()
                .filter(entry -> !entry.timestamp().toLocalDate().isBefore(today.minusDays(6)))
                .toList();
        
        return new Summary(
                today,
                entries.stream().mapToInt(ActivityEntry::timeSpent).sum(),
                entries.size()
        );
    }
    
    /**
     * Gets total time spent on a specific date for a given status.
     * 
     * @param date The date to check
     * @param status The status filter (e.g., "DONE", "DOING")
     * @return Total time in minutes
     */
    public int getTimeByStatus(LocalDate date, String status) {
        return historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .filter(entry -> entry.status().equalsIgnoreCase(status))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Gets total time spent on a specific date for a given activity type.
     * 
     * @param date The date to check
     * @param activityType The activity type to filter by
     * @return Total time in minutes
     */
    public int getTimeByActivityType(LocalDate date, String activityType) {
        return historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .filter(entry -> entry.activityType().equalsIgnoreCase(activityType))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Gets total time spent on a specific date.
     * 
     * @param date The date to check
     * @return Total time in minutes
     */
    public int getTotalTimeSpentOnDate(LocalDate date) {
        return historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Gets the number of activities on a specific date.
     * 
     * @param date The date to count
     * @return Number of activities
     */
    public int getActivityCount(LocalDate date) {
        return (int) historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .count();
    }
    
    /**
     * Gets a detailed summary with breakdown by status for a specific date.
     * 
     * @param date The date to summarize
     * @return DetailedSummary with breakdown information
     */
    public DetailedSummary getDetailedSummary(LocalDate date) {
        List<ActivityEntry> entries = historyService.getAll().stream()
                .filter(entry -> entry.timestamp().toLocalDate().equals(date))
                .toList();
        
        Map<String, Integer> byStatus = new HashMap<>();
        Map<String, Integer> byActivityType = new HashMap<>();
        
        for (ActivityEntry entry : entries) {
            String status = entry.status();
            String activityType = entry.activityType();
            
            byStatus.put(status, byStatus.getOrDefault(status, 0) + entry.timeSpent());
            byActivityType.put(activityType, byActivityType.getOrDefault(activityType, 0) + entry.timeSpent());
        }
        
        return new DetailedSummary(
                date,
                entries.stream().mapToInt(ActivityEntry::timeSpent).sum(),
                entries.size(),
                byStatus,
                byActivityType
        );
    }
    
    /**
     * Gets a summary object for storing time statistics.
     */
    public static class Summary {
        private final LocalDate date;
        private final int totalTime;
        private final int activityCount;
        
        public Summary(LocalDate date, int totalTime, int activityCount) {
            this.date = date;
            this.totalTime = totalTime;
            this.activityCount = activityCount;
        }
        
        public LocalDate getDate() { return date; }
        public int getTotalTime() { return totalTime; }
        public int getActivityCount() { return activityCount; }
    }
    
    /**
     * Extended summary with breakdown by status and activity type.
     */
    public static class DetailedSummary extends Summary {
        private final Map<String, Integer> byStatus;
        private final Map<String, Integer> byActivityType;
        
        public DetailedSummary(LocalDate date, int totalTime, int activityCount, 
                Map<String, Integer> byStatus, Map<String, Integer> byActivityType) {
            super(date, totalTime, activityCount);
            this.byStatus = byStatus;
            this.byActivityType = byActivityType;
        }
        
        public Map<String, Integer> getByStatus() { return byStatus; }
        public Map<String, Integer> getByActivityType() { return byActivityType; }
    }
}