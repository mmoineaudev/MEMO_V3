package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;
import com.memo.service.HistoryService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.*;

/**
 * Panel for creating new activity entries.
 * UC-001: Create new activity entry with history reuse
 */
public class EntryPanel extends JPanel {
    
    private final EntryEditorService entryEditorService;
    private final HistoryService historyService;
    private final Runnable refreshCallback;
    
    private JComboBox<String> activityTypeCombo;
    private JComboBox<String> descriptionCombo;
    private JComboBox<String> statusCombo;
    private JTextArea commentArea;
    private JSpinner timeSpinner;
    private JButton saveButton;
    
    private static final String[] ACTIVITY_TYPES = {"DEV", "CEREMONY", "SUPPORT", "LEARNING", "ADMIN"};
    private static final String[] STATUSES = {"TODO", "DOING", "DONE", "NOTE"};
    
    /**
     * Create an entry panel with the given services and refresh callback.
     */
    public EntryPanel(EntryEditorService entryEditorService, HistoryService historyService, Runnable refreshCallback) {
        this.entryEditorService = entryEditorService;
        this.historyService = historyService;
        this.refreshCallback = refreshCallback;
        initComponents();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        setLayout(new GridLayout(6, 2, 10, 5));
        setBorder(BorderFactory.createTitledBorder("New Activity Entry"));
        
        // Activity Type
        add(new JLabel("Activity Type:"));
        activityTypeCombo = new JComboBox<>(ACTIVITY_TYPES);
        activityTypeCombo.setEditable(true);
        add(activityTypeCombo);
        
        // Description with history reuse
        add(new JLabel("Description:"));
        descriptionCombo = new JComboBox<>();
        descriptionCombo.setEditable(true);
        descriptionCombo.setMaximumRowCount(5);
        updateHistoryReuse();
        add(descriptionCombo);
        
        // Status
        add(new JLabel("Status:"));
        statusCombo = new JComboBox<>(STATUSES);
        add(statusCombo);
        
        // Comment
        add(new JLabel("Comment:"));
        commentArea = new JTextArea(3, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane commentScrollPane = new JScrollPane(commentArea);
        add(commentScrollPane);
        
        // Time Spent
        add(new JLabel("Time (hours):"));
        timeSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        add(timeSpinner);
        
        // Save Button
        saveButton = new JButton("Save Entry");
        saveButton.addActionListener(e -> saveEntry());
        add(saveButton);
    }
    
    /**
     * Update history reuse suggestions.
     */
    private void updateHistoryReuse() {
        List<String> descriptions = entryEditorService.getHistoryReuseDescriptions(10);
        
        // Clear and rebuild combo
        descriptionCombo.removeAllItems();
        for (String desc : descriptions) {
            descriptionCombo.addItem(desc);
        }
    }
    
    /**
     * Save the entry.
     */
    private void saveEntry() {
        String activityType = (String) activityTypeCombo.getSelectedItem();
        String description = (String) descriptionCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();
        String comment = commentArea.getText();
        Double timeSpent = (Double) timeSpinner.getValue();
        
        try {
            ActivityEntry entry = entryEditorService.createEntry(
                    activityType, description, status, comment, timeSpent
            );
            
            boolean saved = entryEditorService.saveEntry(entry);
            
            if (saved) {
                JOptionPane.showMessageDialog(this,
                        "Entry saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                
                // Clear comment area, keep other fields
                commentArea.setText("");
                updateHistoryReuse();
                
                // Refresh all panels to show the new entry
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } else {
                showError("Failed to save entry");
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    
    /**
     * Show error dialog.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
