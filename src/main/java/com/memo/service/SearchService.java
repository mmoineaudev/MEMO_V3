package com.memo.service;

import com.memo.model.ActivityEntry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for searching and filtering ActivityEntries.
 * Provides efficient search capabilities with multiple filter criteria.
 */
public class SearchService {
    
    private final HistoryService historyService;
    
    public SearchService(HistoryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Searches entries by description (case-insensitive).
     * 
     * @param searchTerm The search term to match in descriptions
     * @return List of matching entries
     */
    public List<ActivityEntry> searchByDescription(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.description().toLowerCase().contains(lowerSearch))
                .collect(Collectors.toList());
    }
    
    /**
     * Searches entries by activity type.
     * 
     * @param activityType The activity type to match
     * @return List of matching entries
     */
    public List<ActivityEntry> searchByActivityType(String activityType) {
        if (activityType == null || activityType.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerType = activityType.toLowerCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.activityType().toLowerCase().equals(lowerType))
                .collect(Collectors.toList());
    }
    
    /**
     * Searches entries by status.
     * 
     * @param status The status to match
     * @return List of matching entries
     */
    public List<ActivityEntry> searchByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerStatus = status.toLowerCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.status().toLowerCase().equals(lowerStatus))
                .collect(Collectors.toList());
    }
    
    /**
     * Combines description and activity type searches.
     * 
     * @param descriptionTerm The search term for descriptions
     * @param activityTypeTerm The search term for activity types
     * @return List of matching entries
     */
    public List<ActivityEntry> searchByDescriptionAndActivityType(String descriptionTerm, String activityTypeTerm) {
        if (descriptionTerm == null && activityTypeTerm == null) {
            return new ArrayList<>();
        }
        
        String lowerDesc = descriptionTerm != null ? descriptionTerm.toLowerCase() : "";
        String lowerType = activityTypeTerm != null ? activityTypeTerm.toLowerCase() : "";
        
        return historyService.getAll().stream()
                .filter(entry -> 
                    (lowerDesc.isEmpty() || entry.description().toLowerCase().contains(lowerDesc)) &&
                    (lowerType.isEmpty() || entry.activityType().toLowerCase().equals(lowerType))
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Filters entries by date range.
     * 
     * @param startDate Inclusive start date
     * @param endDate Inclusive end date
     * @return List of matching entries
     */
    public List<ActivityEntry> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return new ArrayList<>();
        }
        
        return historyService.getAll().stream()
                .filter(entry -> 
                    !entry.timestamp().isBefore(startDate) && 
                    !entry.timestamp().isAfter(endDate)
                )
                .collect(Collectors.toList());
    }
    
    /**
     * Performs an advanced search with multiple criteria.
     * 
     * @param descriptionTerm Optional description search term
     * @param activityTypeTerm Optional activity type search term
     * @param status Optional status filter
     * @param startDate Optional start date for range filtering
     * @param endDate Optional end date for range filtering
     * @return List of matching entries
     */
    public List<ActivityEntry> advancedSearch(
            String descriptionTerm,
            String activityTypeTerm,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        List<ActivityEntry> results = historyService.getAll();
        
        if (descriptionTerm != null && !descriptionTerm.trim().isEmpty()) {
            String lowerDesc = descriptionTerm.toLowerCase();
            results = results.stream()
                    .filter(entry -> entry.description().toLowerCase().contains(lowerDesc))
                    .collect(Collectors.toList());
        }
        
        if (activityTypeTerm != null && !activityTypeTerm.trim().isEmpty()) {
            String lowerType = activityTypeTerm.toLowerCase();
            results = results.stream()
                    .filter(entry -> entry.activityType().toLowerCase().equals(lowerType))
                    .collect(Collectors.toList());
        }
        
        if (status != null && !status.trim().isEmpty()) {
            String lowerStatus = status.toLowerCase();
            results = results.stream()
                    .filter(entry -> entry.status().toLowerCase().equals(lowerStatus))
                    .collect(Collectors.toList());
        }
        
        if (startDate != null) {
            results = results.stream()
                    .filter(entry -> !entry.timestamp().isBefore(startDate))
                    .collect(Collectors.toList());
        }
        
        if (endDate != null) {
            results = results.stream()
                    .filter(entry -> !entry.timestamp().isAfter(endDate))
                    .collect(Collectors.toList());
        }
        
        return results;
    }
    
    /**
     * Calculates total time spent for a list of entries.
     * 
     * @param entries The list of entries
     * @return Total time in minutes
     */
    public int getTotalTimeSpent(List<ActivityEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return 0;
        }
        
        return entries.stream()
                .mapToInt(ActivityEntry::timeSpent)
                .sum();
    }
    
    /**
     * Searches entries by comment (case-insensitive).
     * 
     * @param searchTerm The search term to match in comments
     * @return List of matching entries
     */
    public List<ActivityEntry> searchByComment(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerSearch = searchTerm.toLowerCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.comment().toLowerCase().contains(lowerSearch))
                .collect(Collectors.toList());
    }
    
    /**
     * Returns all entries from history.
     * 
     * @return List of all entries
     */
    public List<ActivityEntry> getAllEntries() {
        return historyService.getAll();
    }
    
    /**
     * Returns entries filtered by status.
     * 
     * @param status The status to filter by
     * @return List of entries with matching status
     */
    public List<ActivityEntry> getEntriesByStatus(String status) {
        return historyService.getAll().stream()
                .filter(entry -> entry.status().equals(status))
                .toList();
    }
}