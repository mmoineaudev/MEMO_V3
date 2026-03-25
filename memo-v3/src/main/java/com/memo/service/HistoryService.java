package com.memo.service;

import com.memo.model.ActivityEntry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing activity history.
 * Loads all entries from CSV files on startup.
 * 
 * Note: The list returned by getHistory() should not be modified directly.
 * Use the provided methods (addEntry, removeEntry, updateEntry) instead.
 */
public class HistoryService {
    
    private final CsvStorageService csvStorage;
    private final List<ActivityEntry> history;
    
    /**
     * Create a history service with default storage directory.
     */
    public HistoryService() {
        this(new CsvStorageService());
    }
    
    /**
     * Create a history service with custom CsvStorageService.
     */
    public HistoryService(CsvStorageService csvStorage) {
        this.csvStorage = csvStorage;
        this.history = new ArrayList<>();
    }
    
    /**
     * Ensure storage directory exists.
     */
    public boolean ensureStorageDirectory() {
        return csvStorage.ensureDirectoryExists();
    }
    
    /**
     * UC-011: Load all history from CSV files on startup.
     * Reads all CSV files matching "activity" pattern and combines entries.
     */
    public List<ActivityEntry> loadAllHistory() {
        history.clear();
        
        List<String> csvFiles = csvStorage.listFilesWithPattern("activity");
        
        for (String fileName : csvFiles) {
            List<String> lines = csvStorage.readFile(fileName);
            for (String line : lines) {
                ActivityEntry entry = ActivityEntry.fromCsv(line);
                // Filter out entries with no meaningful data
                if (entry != null && hasMeaningfulData(entry)) {
                    history.add(entry);
                }
            }
        }
        
        // Sort by timestamp (newest first)
        history.sort(Comparator.comparing(ActivityEntry::timestamp).reversed());
        
        return new ArrayList<>(history);
    }
    
    /**
     * Check if an entry has meaningful data.
     */
    private boolean hasMeaningfulData(ActivityEntry entry) {
        return !entry.activityType().isBlank() ||
               !entry.description().isBlank() ||
               !entry.timestamp().isBlank() ||
               entry.timeSpent() > 0.0;
    }
    
    /**
     * Get current history entries.
     */
    public List<ActivityEntry> getHistory() {
        return new ArrayList<>(history);
    }
    
    /**
     * Add an entry to history.
     */
    public void addEntry(ActivityEntry entry) {
        history.add(0, entry); // Add to beginning (newest first)
    }
    
    /**
     * Remove an entry from history.
     */
    public void removeEntry(ActivityEntry entry) {
        history.removeIf(e -> e.equals(entry));
    }
    
    /**
     * Update an entry in history.
     */
    public void updateEntry(ActivityEntry oldEntry, ActivityEntry newEntry) {
        int index = history.indexOf(oldEntry);
        if (index >= 0) {
            history.set(index, newEntry);
        }
    }
    
    /**
     * Save all history to CSV file.
     */
    public boolean saveAllHistory(String fileName) {
        return csvStorage.writeAllEntries(fileName, history);
    }
    
    /**
     * Get unique descriptions from history.
     */
    public List<String> getUniqueDescriptions() {
        return history.stream()
                .map(ActivityEntry::description)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Get last N distinct descriptions for history reuse.
     */
    public List<String> getLastNDistinctDescriptions(int n) {
        return history.stream()
                .map(ActivityEntry::description)
                .distinct()
                .limit(n)
                .collect(Collectors.toList());
    }
    
    /**
     * Clear all history.
     */
    public void clearHistory() {
        history.clear();
    }
}
