package com.memo.view.components;

import javax.swing.*;
import java.awt.*;

/**
 * Label component that displays status text with color coding.
 * UC-014: TODO=gold, DOING=blue, DONE=green, NOTE=gray, Unknown=white
 */
public class ColoredStatusLabel extends JLabel {
    
    private static final String TODO_COLOR = "#FFD700";
    private static final String DOING_COLOR = "#1E90FF";
    private static final String DONE_COLOR = "#32CD32";
    private static final String NOTE_COLOR = "#808080";
    private static final String UNKNOWN_COLOR = "#FFFFFF";
    
    /**
     * Create a colored status label with the given status.
     */
    public ColoredStatusLabel(String status) {
        super(createHtmlLabel(status, getStatusColor(status)));
    }
    
    /**
     * Create HTML label with color.
     */
    private static String createHtmlLabel(String status, String color) {
        return "<html><font color='" + color + "'><b>" + escapeHtml(status) + "</b></font></html>";
    }
    
    /**
     * Get the color for a status.
     */
    private static String getStatusColor(String status) {
        if (status == null) {
            return UNKNOWN_COLOR;
        }
        switch (status.toUpperCase()) {
            case "TODO":
                return TODO_COLOR;
            case "DOING":
                return DOING_COLOR;
            case "DONE":
                return DONE_COLOR;
            case "NOTE":
                return NOTE_COLOR;
            default:
                return UNKNOWN_COLOR;
        }
    }
    
    /**
     * Escape HTML special characters in the status text.
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
    
    /**
     * Update the status and color.
     */
    public void setStatus(String status) {
        setText(createHtmlLabel(status, getStatusColor(status)));
    }
}
