package com.memo.service;

import com.memo.model.ActivityEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for creating and managing activity entries.
 * Handles validation, file naming, and persistence.
 */
public class EntryEditorService {
    
    private final HistoryService historyService;
    private final String defaultFileName;
    
    /**
     * Create an entry editor service with default storage.
     */
    public EntryEditorService() {
        this(new HistoryService(), "activity_" + java.time.LocalDate.now() + ".csv");
    }
    
    /**
     * Create an entry editor service with custom history service.
     */
    public EntryEditorService(HistoryService historyService, String defaultFileName) {
        this.historyService = historyService;
        this.defaultFileName = defaultFileName;
    }
    
    /**
     * UC-001: Create a new activity entry.
     * Validates and creates a new entry with current timestamp.
     */
    public ActivityEntry createEntry(String activityType, String description, 
            String status, String comment, Double timeSpent) {
        // Validate required fields
        validateEntry(activityType, description, status);
        
        return ActivityEntry.createNew(activityType, description, status, comment, timeSpent);
    }
    
    /**
     * Validate entry fields.
     */
    private void validateEntry(String activityType, String description, String status) {
        if (activityType == null || activityType.isBlank()) {
            throw new IllegalArgumentException("Activity type is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
    }
    
    /**
     * Save an entry to the CSV file.
     */
    public boolean saveEntry(ActivityEntry entry) {
        if (historyService.getHistory().isEmpty()) {
            // First entry, create new file
            return saveToNewFile(entry);
        } else {
            // Append to existing file
            return appendToFile(entry);
        }
    }
    
    /**
     * Save entry to a new file.
     */
    private boolean saveToNewFile(ActivityEntry entry) {
        String fileName = defaultFileName;
        historyService.addEntry(entry);
        return historyService.saveAllHistory(fileName);
    }
    
    /**
     * Append entry to existing file.
     */
    private boolean appendToFile(ActivityEntry entry) {
        List<String> csvFiles = historyService.getHistory().isEmpty() ? 
                new ArrayList<>() : historyService.getHistory().stream()
                .map(e -> e.timestamp())
                .filter(ts -> ts.contains("20"))
                .findFirst()
                .map(ts -> {
                    String date = ts.split(" ")[0];
                    return "activity_" + date.replace("/", "_") + ".csv";
                })
                .map(f -> List.of(f))
                .orElse(List.of());
        
        // For now, just save all history to default file
        return historyService.saveAllHistory(defaultFileName);
    }
    
    /**
     * Generate a unique filename based on current date.
     */
    public String generateFileName() {
        return "activity_" + java.time.LocalDate.now() + ".csv";
    }
    
    /**
     * UC-013: Update an entry and persist changes.
     */
    public boolean updateEntry(ActivityEntry oldEntry, ActivityEntry newEntry) {
        // Validate the new entry
        validateEntry(newEntry.activityType(), newEntry.description(), newEntry.status());
        
        // Update in memory
        historyService.updateEntry(oldEntry, newEntry);
        
        // Persist to file
        return historyService.saveAllHistory(defaultFileName);
    }
    
    /**
     * Delete an entry and persist changes.
     */
    public boolean deleteEntry(ActivityEntry entry) {
        historyService.removeEntry(entry);
        return historyService.saveAllHistory(defaultFileName);
    }
    
    /**
     * Get unique descriptions for history reuse.
     */
    public List<String> getHistoryReuseDescriptions(int maxCount) {
        return historyService.getLastNDistinctDescriptions(maxCount);
    }
}
