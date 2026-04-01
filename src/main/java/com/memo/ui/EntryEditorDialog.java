package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;
import com.memo.service.CsvStorageService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Dialog for creating and editing activity entries.
 */
public class EntryEditorDialog extends JDialog {
    
    private EntryEditorService editorService;
    private CsvStorageService storageService;
    
    private JTextField activityTypeField;
    private JTextArea descriptionTextArea;
    private JComboBox<String> statusComboBox;
    private JTextField timeSpentField;
    private JTextArea commentTextArea;
    private JButton saveButton;
    private JButton cancelButton;
    
    public EntryEditorDialog(JFrame parent, EntryEditorService editorService, CsvStorageService storageService) {
        super(parent, "New Activity Entry", true);
        this.editorService = editorService;
        this.storageService = storageService;
        
        setLayout(new BorderLayout());
        setSize(450, 600);
        setLocationRelativeTo(parent);
        
        createContent();
    }
    
    private void createContent() {
        JPanel mainPanel = new JPanel(new GridLayout(7, 2, 10, 8));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Activity Type with auto-suggest
        mainPanel.add(new JLabel("Activity Type:"));
        activityTypeField = new JTextField();
        setupAutoComplete(activityTypeField, editorService.getRecentDescriptions(10));
        mainPanel.add(activityTypeField);
        
        // Description (large text area)
        mainPanel.add(new JLabel("Description:"));
        descriptionTextArea = new JTextArea(5, 25);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionTextArea);
        mainPanel.add(descScrollPane);
        
        // Comment (large text area)
        mainPanel.add(new JLabel("Comment:"));
        commentTextArea = new JTextArea(4, 25);
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        JScrollPane commentScrollPane = new JScrollPane(commentTextArea);
        mainPanel.add(commentScrollPane);
        
        // Status
        mainPanel.add(new JLabel("Status:"));
        String[] statuses = {"TODO", "DOING", "DONE", "NOTE"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setSelectedIndex(1); // DOING by default
        mainPanel.add(statusComboBox);
        
        // Time Spent (minutes)
        mainPanel.add(new JLabel("Time Spent (min):"));
        timeSpentField = new JTextField("30");
        mainPanel.add(timeSpentField);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        saveButton = new JButton("Save Entry");
        saveButton.setPreferredSize(new Dimension(120, 30));
        cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(120, 30));
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Action listeners
        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> onCancel());
        
        // Enter key on time field to save
        timeSpentField.addActionListener(e -> onSave());
    }
    
    private void setupAutoComplete(JTextField field, List<String> suggestions) {
        if (suggestions == null || suggestions.isEmpty()) return;
        
        // Simple approach: just set the first suggestion as default or placeholder
        // For a proper autocomplete, we'd need a custom component or third-party library
        // For now, we'll just leave it as a simple text field
    }
    
    private void onSave() {
        String activityType = activityTypeField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        String comment = commentTextArea.getText().trim();
        String status = (String) statusComboBox.getSelectedItem();
        
        try {
            int timeSpent = Integer.parseInt(timeSpentField.getText());
            
            if (activityType.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "Please enter an activity type", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                activityTypeField.requestFocus();
                return;
            }
            
            // Create new entry with current timestamp
            ActivityEntry entry = editorService.create(activityType, description, timeSpent);
            
            // Update with status and comment
            ActivityEntry updated = new ActivityEntry(
                    entry.activityType(),
                    entry.description(),
                    status,
                    comment,
                    entry.timestamp(),
                    entry.timeSpent()
            );
            
            // Add to history service (in-memory)
            editorService.addToHistory(updated);
            
            // Save to CSV file
            saveEntryToStorage(updated);
            
            // Store the last description for reuse
            editorService.setLastDescription(description);
            
            JOptionPane.showMessageDialog(this, 
                    "Entry saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid number for time spent", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error saving entry: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void saveEntryToStorage(ActivityEntry entry) throws Exception {
        // Ensure storage directory exists
        if (!storageService.storageDirectoryExists()) {
            storageService.createStorageDirectory();
        }
        
        // Get or create the list for today's date
        List<ActivityEntry> entries = storageService.loadAll();
        entries.add(entry);
        
        // Save all entries (will be organized by date in CsvStorageService)
        storageService.saveAll(entries);
    }
    
    private void onCancel() {
        dispose();
    }
    
    public String getNewActivityType() {
        return activityTypeField.getText().trim();
    }
    
    public String getNewDescription() {
        return descriptionTextArea.getText().trim();
    }
    
    public int getNewTimeSpent() {
        try {
            return Integer.parseInt(timeSpentField.getText());
        } catch (NumberFormatException e) {
            return 30;
        }
    }
}