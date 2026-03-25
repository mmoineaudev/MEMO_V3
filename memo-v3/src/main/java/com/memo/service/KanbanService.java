package com.memo.service;

import com.memo.model.ActivityEntry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Kanban board data.
 * UC-015: Display Kanban view
 */
public class KanbanService {
    
    /**
     * Get entries grouped by description, excluding DONE status.
     * Entries within each group are sorted by timestamp (oldest first).
     */
    public Map<String, List<ActivityEntry>> getEntriesByDescription(List<ActivityEntry> entries) {
        return entries.stream()
                .filter(e -> !"DONE".equals(e.status()))
                .sorted(Comparator.comparing(ActivityEntry::timestamp))
                .collect(Collectors.groupingBy(
                        ActivityEntry::description,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
    
    /**
     * Get unique descriptions for Kanban columns.
     */
    public List<String> getColumnDescriptions(List<ActivityEntry> entries) {
        return getEntriesByDescription(entries).keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Check if entry should appear in Kanban (not DONE).
     */
    public boolean isInKanban(ActivityEntry entry) {
        return !"DONE".equals(entry.status());
    }
    
    /**
     * Count entries per description.
     */
    public Map<String, Integer> getCountsByDescription(List<ActivityEntry> entries) {
        return getEntriesByDescription(entries).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().size()
                ));
    }
}
