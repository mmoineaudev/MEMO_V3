package com.memo.service;

import com.memo.model.ActivityEntry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for searching activity entries.
 */
public class SearchService {
    
    /**
     * Search entries by query string.
     */
    public List<ActivityEntry> search(List<ActivityEntry> entries, String query) {
        return search(entries, query, null, null);
    }
    
    /**
     * Search entries by query string and filters.
     */
    public List<ActivityEntry> search(List<ActivityEntry> entries, String query, 
            String typeFilter, String statusFilter) {
        
        return entries.stream()
                .filter(e -> matchesQuery(e, query))
                .filter(e -> matchesType(e, typeFilter))
                .filter(e -> matchesStatus(e, statusFilter))
                .collect(Collectors.toList());
    }
    
    /**
     * Check if entry matches the query string.
     */
    private boolean matchesQuery(ActivityEntry entry, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        
        return entry.activityType().toLowerCase().contains(query) ||
               entry.description().toLowerCase().contains(query) ||
               entry.comment().toLowerCase().contains(query) ||
               entry.status().toLowerCase().contains(query) ||
               entry.timestamp().toLowerCase().contains(query);
    }
    
    /**
     * Check if entry matches the type filter.
     */
    private boolean matchesType(ActivityEntry entry, String typeFilter) {
        if ("ALL".equals(typeFilter)) {
            return true;
        }
        return typeFilter.equals(entry.activityType());
    }
    
    /**
     * Check if entry matches the status filter.
     */
    private boolean matchesStatus(ActivityEntry entry, String statusFilter) {
        if ("ALL".equals(statusFilter)) {
            return true;
        }
        return statusFilter.equals(entry.status());
    }
}
