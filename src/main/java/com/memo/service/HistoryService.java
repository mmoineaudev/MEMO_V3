package com.memo.service;

import com.memo.model.ActivityEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * In-memory service for managing ActivityEntries.
 * Provides CRUD operations with automatic sorting by timestamp.
 */
public class HistoryService {
    
    private final List<ActivityEntry> entries;
    private static final Comparator<ActivityEntry> TIMESTAMP_COMPARATOR = 
            Comparator.comparing(ActivityEntry::timestamp).reversed();
    
    public HistoryService() {
        this.entries = new ArrayList<>();
    }
    
    /**
     * Adds an ActivityEntry to the history.
     * 
     * @param entry The entry to add
     */
    public void add(ActivityEntry entry) {
        entries.add(entry);
    }
    
    /**
     * Retrieves all ActivityEntries sorted by timestamp (newest first).
     * 
     * @return List of all entries
     */
    public List<ActivityEntry> getAll() {
        return new ArrayList<>(entries);
    }
    
    /**
     * Retrieves an ActivityEntry by its identifier.
     * 
     * @param id The identifier (uses activityType as key)
     * @return The found entry, or null if not found
     */
    public ActivityEntry get(String id) {
        return entries.stream()
                .filter(entry -> entry.activityType().equals(id))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Updates an existing ActivityEntry.
     * 
     * @param updatedEntry The updated entry with matching activityType
     */
    public void update(ActivityEntry updatedEntry) {
        String id = updatedEntry.activityType();
        
        entries.replaceAll(entry -> {
            if (entry.activityType().equals(id)) {
                return updatedEntry;
            }
            return entry;
        });
    }
    
    /**
     * Deletes an ActivityEntry by its identifier.
     * 
     * @param id The identifier to delete
     */
    public void delete(String id) {
        entries.removeIf(entry -> entry.activityType().equals(id));
    }
    
    /**
     * Clears all entries from the history.
     */
    public void clear() {
        entries.clear();
    }
    
    /**
     * Returns the total number of entries.
     * 
     * @return Count of entries
     */
    public int size() {
        return entries.size();
    }
}