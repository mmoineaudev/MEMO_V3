package com.memo.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record representing an activity entry.
 * Each activity entry captures activity details, status, comments, and timing information.
 */
public record ActivityEntry(
        String activityType,
        String description,
        String status,
        String comment,
        LocalDateTime timestamp,
        int timeSpent
) {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

      /**
     * Creates a new ActivityEntry with auto-generated timestamp.
     * 
     * @param activityType The type of activity
     * @param description Brief description of the activity
     * @param status Current status: TODO, DOING, DONE, NOTE
     * @param comment Additional comments or notes (may contain line breaks)
     * @param timeSpent Time spent in minutes
     */
    public ActivityEntry(String activityType, String description, String status, 
                          String comment, int timeSpent) {
        this(activityType, description, status, comment, LocalDateTime.now(), timeSpent);
    }
    
    /**
     * Factory method to create an ActivityEntry with a specific date.
     * 
     * @param activityType The type of activity
     * @param description Brief description of the activity
     * @param status Current status: TODO, DOING, DONE, NOTE
     * @param comment Additional comments or notes (may contain line breaks)
     * @param date The date for this entry
     * @param timeSpent Time spent in minutes
     * @return A new ActivityEntry with the specified date
     */
    public static ActivityEntry ofDate(String activityType, String description, String status, 
                                        String comment, java.time.LocalDate date, int timeSpent) {
        return new ActivityEntry(activityType, description, status, comment, 
                                  date.atStartOfDay(), timeSpent);
    }

    /**
     * Returns the timestamp formatted as a string.
     * 
     * @return Formatted timestamp string
     */
    public String getFormattedTimestamp() {
        return timestamp.format(TIMESTAMP_FORMATTER);
    }

    /**
     * Validates that all required fields are non-null and has valid values.
     * 
     * @return true if entry is valid, false otherwise
     */
    public boolean isValid() {
        return activityType != null && !activityType.isBlank()
                && description != null && !description.isBlank()
                && status != null && !status.isBlank()
                && timeSpent >= 0;
    }
}