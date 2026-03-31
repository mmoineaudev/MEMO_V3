package com.memo.service;

import com.memo.model.ActivityEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Service for Kanban board organization and status management.
 */
public class KanbanService {
    
    private static final List<String> STATUS_FLOW = List.of("TODO", "DOING", "DONE");
    
    private final HistoryService historyService;
    
    public KanbanService(HistoryService historyService) {
        this.historyService = historyService;
    }
    
    /**
     * Returns all possible statuses in flow order.
     * 
     * @return List of status strings
     */
    public List<String> getAllStatuses() {
        return new ArrayList<>(STATUS_FLOW);
    }
    
    /**
     * Gets entries filtered by a specific status.
     * 
     * @param status The status to filter by
     * @return List of entries with matching status
     */
    public List<ActivityEntry> getEntriesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerStatus = status.toUpperCase();
        
        return historyService.getAll().stream()
                .filter(entry -> entry.status().equals(lowerStatus))
                .toList();
    }
    
    /**
     * Gets all entries grouped by status for Kanban board display.
     * 
     * @return KanbanBoard with entries organized by status
     */
    public KanbanBoard getKanbanBoard() {
        Map<String, List<ActivityEntry>> board = new HashMap<>();
        
        for (String status : STATUS_FLOW) {
            board.put(status, getEntriesByStatus(status));
        }
        
        return new KanbanBoard(board);
    }
    
    /**
     * Moves an entry to the next status in the flow.
     * 
     * @param activityType The type of activity to move
     * @return True if moved successfully, false otherwise
     */
    public boolean moveToNextStatus(String activityType) {
        ActivityEntry entry = historyService.get(activityType);
        
        if (entry == null) {
            return false;
        }
        
        String currentStatus = entry.status();
        int currentIndex = STATUS_FLOW.indexOf(currentStatus);
        
        if (currentIndex >= 0 && currentIndex < STATUS_FLOW.size() - 1) {
            String nextStatus = STATUS_FLOW.get(currentIndex + 1);
            ActivityEntry updated = new ActivityEntry(
                    activityType,
                    entry.description(),
                    nextStatus,
                    entry.comment(),
                    entry.timestamp(),
                    entry.timeSpent()
            );
            
            historyService.update(updated);
            return true;
        }
        
        return false;
    }
    
    /**
     * Moves an entry to the previous status in the flow.
     * 
     * @param activityType The type of activity to move
     * @return True if moved successfully, false otherwise
     */
    public boolean moveToPreviousStatus(String activityType) {
        ActivityEntry entry = historyService.get(activityType);
        
        if (entry == null) {
            return false;
        }
        
        String currentStatus = entry.status();
        int currentIndex = STATUS_FLOW.indexOf(currentStatus);
        
        if (currentIndex > 0) {
            String prevStatus = STATUS_FLOW.get(currentIndex - 1);
            ActivityEntry updated = new ActivityEntry(
                    activityType,
                    entry.description(),
                    prevStatus,
                    entry.comment(),
                    entry.timestamp(),
                    entry.timeSpent()
            );
            
            historyService.update(updated);
            return true;
        }
        
        return false;
    }
    
    /**
     * Gets the count of tasks for a specific status.
     * 
     * @param status The status to count
     * @return Number of tasks with that status
     */
    public int getTaskCountByStatus(String status) {
        return (int) historyService.getAll().stream()
                .filter(entry -> entry.status().equals(status))
                .count();
    }
    
    /**
     * Represents the Kanban board state.
     */
    public static class KanbanBoard {
        private final Map<String, List<ActivityEntry>> entriesByStatus;
        
        public KanbanBoard(Map<String, List<ActivityEntry>> entriesByStatus) {
            this.entriesByStatus = entriesByStatus;
        }
        
        public List<ActivityEntry> getEntriesByStatus(String status) {
            return new ArrayList<>(entriesByStatus.getOrDefault(status, new ArrayList<>()));
        }
        
        public Map<String, List<ActivityEntry>> getAllEntries() {
            return new HashMap<>(entriesByStatus);
        }
    }
}