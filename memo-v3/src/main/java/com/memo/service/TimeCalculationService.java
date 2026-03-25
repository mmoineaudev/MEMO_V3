package com.memo.service;

import com.memo.model.ActivityEntry;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating time sums from activity entries.
 * Supports daily and weekly aggregations by description.
 */
public class TimeCalculationService {
    
    /**
     * Calculate daily time summary for a specific date.
     * Returns map of description -> total time spent.
     */
    public Map<String, Double> calculateDailySummary(List<ActivityEntry> entries, LocalDate date) {
        return entries.stream()
                .filter(entry -> isEntryOnDate(entry, date))
                .collect(Collectors.groupingBy(
                        ActivityEntry::description,
                        Collectors.summingDouble(ActivityEntry::timeSpent)
                ));
    }
    
    /**
     * Calculate weekly time summary for a specific week.
     * Returns map of description -> total time spent.
     * Uses manual week calculation compatible with older JDKs.
     */
    public Map<String, Double> calculateWeeklySummary(List<ActivityEntry> entries, String weekKey) {
        return entries.stream()
                .filter(entry -> isEntryInWeek(entry, weekKey))
                .collect(Collectors.groupingBy(
                        ActivityEntry::description,
                        Collectors.summingDouble(ActivityEntry::timeSpent)
                ));
    }
    
    /**
     * Calculate time summary for all entries (no date filter).
     */
    public Map<String, Double> calculateAllTimeSummary(List<ActivityEntry> entries) {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        ActivityEntry::description,
                        Collectors.summingDouble(ActivityEntry::timeSpent)
                ));
    }
    
    /**
     * Check if an entry is on a specific date.
     */
    private boolean isEntryOnDate(ActivityEntry entry, LocalDate date) {
        try {
            java.time.LocalDateTime entryTime = java.time.LocalDateTime.parse(
                    entry.timestamp(),
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            return entryTime.toLocalDate().equals(date);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if an entry is in a specific week.
     * Week format: "2026-W12" (year-week number)
     */
    private boolean isEntryInWeek(ActivityEntry entry, String weekKey) {
        try {
            java.time.LocalDateTime entryTime = java.time.LocalDateTime.parse(
                    entry.timestamp(),
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            int year = entryTime.getYear();
            int weekNum = (entryTime.getDayOfYear() - 1) / 7 + 1;
            String entryWeekKey = year + "-W" + String.format("%02d", weekNum);
            return weekKey.equals(entryWeekKey);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get entries grouped by date.
     */
    public Map<LocalDate, List<ActivityEntry>> groupEntriesByDate(List<ActivityEntry> entries) {
        return entries.stream()
                .filter(e -> isValidTimestamp(e.timestamp()))
                .collect(Collectors.groupingBy(
                        e -> LocalDate.parse(e.timestamp(), 
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                        Collectors.toList()
                ));
    }
    
    /**
     * Get entries grouped by week (key format: "YYYY-WNN").
     */
    public Map<String, List<ActivityEntry>> groupEntriesByWeek(List<ActivityEntry> entries) {
        return entries.stream()
                .filter(e -> isValidTimestamp(e.timestamp()))
                .collect(Collectors.groupingBy(this::getWeekKey, Collectors.toList()));
    }
    
    /**
     * Get week key for an entry (format: "YYYY-WNN").
     */
    private String getWeekKey(ActivityEntry entry) {
        try {
            java.time.LocalDateTime entryTime = java.time.LocalDateTime.parse(
                    entry.timestamp(),
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            );
            int year = entryTime.getYear();
            int weekNum = (entryTime.getDayOfYear() - 1) / 7 + 1;
            return year + "-W" + String.format("%02d", weekNum);
        } catch (Exception e) {
            return "0-W00";
        }
    }
    
    /**
     * Check if timestamp string is valid.
     */
    private boolean isValidTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return false;
        }
        try {
            java.time.LocalDateTime.parse(timestamp,
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Calculate total time spent across all entries.
     */
    public double calculateTotalTime(List<ActivityEntry> entries) {
        return entries.stream()
                .mapToDouble(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Calculate total time for a specific activity type.
     */
    public double calculateTimeByActivityType(List<ActivityEntry> entries, String activityType) {
        return entries.stream()
                .filter(e -> e.activityType().equals(activityType))
                .mapToDouble(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Get unique descriptions from entries.
     */
    public Set<String> getUniqueDescriptions(List<ActivityEntry> entries) {
        return entries.stream()
                .map(ActivityEntry::description)
                .distinct()
                .collect(Collectors.toSet());
    }
    
    /**
     * Get unique activity types from entries.
     */
    public Set<String> getUniqueActivityTypes(List<ActivityEntry> entries) {
        return entries.stream()
                .map(ActivityEntry::activityType)
                .distinct()
                .collect(Collectors.toSet());
    }
}
