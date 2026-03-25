package com.memo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Immutable model representing a single activity entry.
 * Fields: activityType, description, status, comment, timestamp, timeSpent
 */
public record ActivityEntry(
        String activityType,
        String description,
        String status,
        String comment,
        String timestamp,
        Double timeSpent
) {
    
    public static final String SEPARATOR = ";";
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Parse a CSV line into an ActivityEntry.
     * Format: ACTIVITY_TYPE;DESCRIPTION;STATUS;COMMENT;TIMESTAMP;TIME_SPENT
     */
    public static ActivityEntry fromCsv(String line) {
        if (line == null || line.trim().isBlank()) {
            return null;
        }
        
        String[] parts = line.split(SEPARATOR, -1);
        
        String activityType = parts.length > 0 ? parts[0] : "";
        String description = parts.length > 1 ? parts[1] : "";
        String status = parts.length > 2 ? parts[2] : "";
        String comment = parts.length > 3 ? parts[3] : "";
        String timestamp = parts.length > 4 ? parts[4] : "";
        Double timeSpent = parts.length > 5 && !parts[5].isBlank() 
                ? Double.parseDouble(parts[5]) : 0.0;
        
        return new ActivityEntry(
                activityType, description, status, comment, timestamp, timeSpent
        );
    }
    
    /**
     * Convert this entry to CSV line format.
     */
    public String toCsv() {
        return String.join(SEPARATOR, 
                activityType, description, status, comment, timestamp, 
                timeSpent != null ? String.valueOf(timeSpent) : "0.0");
    }
    
    /**
     * Create a new entry with current timestamp.
     */
    public static ActivityEntry createNew(String activityType, String description, 
            String status, String comment, Double timeSpent) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return new ActivityEntry(
                activityType, description, status, comment, timestamp, 
                timeSpent != null ? timeSpent : 0.0
        );
    }
    
    /**
     * Create a copy with updated fields.
     */
    public ActivityEntry withUpdated(String activityType, String description, 
            String status, String comment, Double timeSpent) {
        return new ActivityEntry(
                activityType != null ? activityType : this.activityType,
                description != null ? description : this.description,
                status != null ? status : this.status,
                comment != null ? comment : this.comment,
                this.timestamp,
                timeSpent != null ? timeSpent : this.timeSpent
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityEntry that = (ActivityEntry) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(activityType, that.activityType) &&
               Objects.equals(description, that.description);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(timestamp, activityType, description);
    }
}
