package com.memo.service;

import com.memo.model.ActivityEntry;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating time summaries.
 * UC-005: Daily summary, UC-006: Weekly summary
 */
public class SummaryService {
    
    private final TimeCalculationService timeCalcService;
    
    public SummaryService() {
        this.timeCalcService = new TimeCalculationService();
    }
    
    /**
     * UC-005: Calculate daily summary for a specific date.
     * Returns map of description -> total hours.
     */
    public Map<String, Double> getDailySummary(List<ActivityEntry> entries, LocalDate date) {
        return timeCalcService.calculateDailySummary(entries, date);
    }
    
    /**
     * UC-006: Calculate weekly summary for a specific week.
     * Returns map of description -> total hours.
     * Week format: "YYYY-WNN"
     */
    public Map<String, Double> getWeeklySummary(List<ActivityEntry> entries, String weekKey) {
        return timeCalcService.calculateWeeklySummary(entries, weekKey);
    }
    
    /**
     * Get all dates with entries, sorted newest first.
     */
    public List<LocalDate> getDatesWithEntries(List<ActivityEntry> entries) {
        return entries.stream()
                .map(e -> {
                    try {
                        return LocalDate.parse(e.timestamp().split(" ")[0],
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }
    
    /**
     * Get all week keys with entries, sorted newest first.
     */
    public List<String> getWeeksWithEntries(List<ActivityEntry> entries) {
        return entries.stream()
                .map(e -> {
                    try {
                        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(
                                e.timestamp(),
                                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                        int year = dt.getYear();
                        int weekNum = (dt.getDayOfYear() - 1) / 7 + 1;
                        return year + "-W" + String.format("%02d", weekNum);
                    } catch (Exception ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toList());
    }
    
    /**
     * Format a map as a string for display.
     */
    public String formatSummary(Map<String, Double> summary) {
        if (summary.isEmpty()) {
            return "No entries found.";
        }
        
        StringBuilder sb = new StringBuilder();
        summary.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    sb.append(String.format("  %s: %.2f hours%n", entry.getKey(), entry.getValue()));
                });
        
        double total = summary.values().stream().mapToDouble(Double::doubleValue).sum();
        sb.append(String.format("---%nTotal: %.2f hours", total));
        
        return sb.toString();
    }
    
    /**
     * Get total hours for a day.
     */
    public double getDailyTotal(List<ActivityEntry> entries, LocalDate date) {
        Map<String, Double> summary = getDailySummary(entries, date);
        return summary.values().stream().mapToDouble(Double::doubleValue).sum();
    }
    
    /**
     * Get total hours for a week.
     */
    public double getWeeklyTotal(List<ActivityEntry> entries, String weekKey) {
        Map<String, Double> summary = getWeeklySummary(entries, weekKey);
        return summary.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
