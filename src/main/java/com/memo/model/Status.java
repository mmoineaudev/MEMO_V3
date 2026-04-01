package com.memo.model;

/**
 * Constants for activity entry statuses.
 * Defines the valid status values and their display names.
 */
public final class Status {
    
    /** Task not yet started */
    public static final String TODO = "TODO";
    
    /** Task currently in progress */
    public static final String DOING = "DOING";
    
    /** Task completed */
    public static final String DONE = "DONE";
    
    /** Note or reference entry */
    public static final String NOTE = "NOTE";
    
    /** Kanban flow statuses (excludes NOTE) */
    public static final java.util.List<String> KANBAN_FLOW = 
        java.util.List.of(TODO, DOING, DONE);
    
    /** All valid statuses as a list */
    public static final java.util.List<String> ALL_STATUSES = 
        java.util.List.of(TODO, DOING, DONE, NOTE);
    
    /** Status display names for UI */
    private static final java.util.Map<String, String> DISPLAY_NAMES = 
        new java.util.HashMap<>();
    
    static {
        DISPLAY_NAMES.put(TODO, "To Do");
        DISPLAY_NAMES.put(DOING, "In Progress");
        DISPLAY_NAMES.put(DONE, "Done");
        DISPLAY_NAMES.put(NOTE, "Note");
    }
    
    /**
     * Returns the display name for a status.
     * 
     * @param status The status code
     * @return The human-readable display name
     */
    public static String getDisplayName(String status) {
        return DISPLAY_NAMES.getOrDefault(status, status);
    }
    
    /**
     * Returns true if the given status is valid.
     * 
     * @param status The status to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String status) {
        return ALL_STATUSES.contains(status);
    }
    
    // Prevent instantiation
    private Status() {
        throw new UnsupportedOperationException("Status is a utility class");
    }
}
