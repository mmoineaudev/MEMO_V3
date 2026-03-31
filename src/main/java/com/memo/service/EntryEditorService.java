package com.memo.service;

import com.memo.model.ActivityEntry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for creating and updating ActivityEntries with intelligent defaults.
 */
public class EntryEditorService {
    
    private final HistoryService historyService;
    private String lastDescription;
    
    public EntryEditorService(HistoryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Creates a new ActivityEntry with auto-timestamping and optional description reuse.
     * 
     * @param activityType The type of activity (e.g., "Development", "Meeting")
     * @param description Optional description. If null or empty, reuses last description.
     * @param timeSpent Time spent in minutes
     * @return The created ActivityEntry
     */
public ActivityEntry create(String activityType, String description, int timeSpent) {
        return create(activityType, description, timeSpent, LocalDateTime.now());
    }
    
    /**
     * Creates a new ActivityEntry with custom timestamp.
     * 
     * @param activityType The type of activity
     * @param description Optional description. If null or empty, reuses last description or uses "Task" if no history.
     * @param timeSpent Time spent in minutes
     * @param timestamp Custom timestamp (null for auto-timestamp)
     * @return The created ActivityEntry
     */
    public ActivityEntry create(String activityType, String description, int timeSpent, LocalDateTime timestamp) {
        if (description == null || description.trim().isEmpty()) {
            // Try to use lastDescription; if not available, use most recent description from history
            if (lastDescription != null && !lastDescription.trim().isEmpty()) {
                description = lastDescription;
            } else {
                List<ActivityEntry> allEntries = historyService.getAll();
                if (!allEntries.isEmpty()) {
                    description = allEntries.get(0).description();
                } else {
                    description = "Task";
                }
            }
        }
        
        return new ActivityEntry(activityType, description, "DOING", "", timestamp != null ? timestamp : LocalDateTime.now(), timeSpent);
    }
    
    /**
     * Updates an existing ActivityEntry.
     * 
     * @param activityType The type of activity to update
     * @param description Optional description. If null, reuses last description or keeps existing.
     * @param status New status (e.g., "DONE", "DOING")
     * @param timeSpent New time spent in minutes
     * @return The updated ActivityEntry
     */
    public ActivityEntry update(String activityType, String description, String status, int timeSpent) {
        return update(activityType, description, status, timeSpent, null);
    }
    
    /**
     * Updates an existing ActivityEntry with custom timestamp.
     * 
     * @param activityType The type of activity to update
     * @param description Optional description. If null, reuses last description or keeps existing.
     * @param status New status
     * @param timeSpent New time spent in minutes
     * @param timestamp Custom timestamp (null for current time)
     * @return The updated ActivityEntry
     */
    public ActivityEntry update(String activityType, String description, String status, int timeSpent, LocalDateTime timestamp) {
        ActivityEntry entry = historyService.get(activityType);
        
        if (entry == null) {
            throw new IllegalArgumentException("Activity type '" + activityType + "' not found");
        }
        
        // Determine the new description
        String newDescription;
        if (description != null && !description.trim().isEmpty()) {
            newDescription = description;
        } else if (lastDescription != null && !lastDescription.trim().isEmpty()) {
            newDescription = lastDescription;
        } else {
            newDescription = entry.description();
        }
        
        LocalDateTime newTimestamp = timestamp != null ? timestamp : LocalDateTime.now();
        
        ActivityEntry updated = new ActivityEntry(
                activityType,
                newDescription,
                status,
                entry.comment(),
                newTimestamp,
                timeSpent
        );
        
        historyService.update(updated);
        return updated;
    }
    
    /**
     * Returns a list of recent descriptions for auto-suggest functionality.
     * 
     * @param limit Maximum number of descriptions to return
     * @return List of unique recent descriptions (newest first)
     */
    public List<String> getRecentDescriptions(int limit) {
        List<ActivityEntry> allEntries = historyService.getAll();
        
        if (allEntries.isEmpty()) {
            return new ArrayList<>();
        }
        
        return allEntries.stream()
                .map(ActivityEntry::description)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Sets the last description for reuse.
     * 
     * @param description The description to remember
     */
    public void setLastDescription(String description) {
        this.lastDescription = description;
    }
}