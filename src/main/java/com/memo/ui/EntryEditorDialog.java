package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

/**
 * Dialog for creating and editing activity entries.
 */
public class EntryEditorDialog extends JDialog {
    
    private EntryEditorService editorService;
    
    private JTextField activityTypeField;
    private JTextArea descriptionTextArea;
    private JComboBox<String> statusComboBox;
    private JTextField timeSpentField;
    private JButton saveButton;
    private JButton cancelButton;
    
    public EntryEditorDialog(JFrame parent, EntryEditorService editorService) {
        super(parent, "New Activity Entry", true);
        this.editorService = editorService;
        
        setLayout(new BorderLayout());
        setSize(400, 500);
        setLocationRelativeTo(parent);
        
        createContent();
    }
    
    private void createContent() {
        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 10, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Activity Type
        mainPanel.add(new JLabel("Activity Type:"));
        activityTypeField = new JTextField();
        mainPanel.add(activityTypeField);
        
        // Description
        mainPanel.add(new JLabel("Description:"));
        descriptionTextArea = new JTextArea(4, 20);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionTextArea);
        mainPanel.add(descScrollPane);
        
        // Status
        mainPanel.add(new JLabel("Status:"));
        String[] statuses = {"TODO", "DOING", "DONE"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setSelectedIndex(1); // DOING by default
        mainPanel.add(statusComboBox);
        
        // Time Spent (minutes)
        mainPanel.add(new JLabel("Time Spent (min):"));
        timeSpentField = new JTextField("30");
        mainPanel.add(timeSpentField);
        
        // Button Panel
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");
        
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
    
    private void onSave() {
        String activityType = activityTypeField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        String status = (String) statusComboBox.getSelectedItem();
        
        try {
            int timeSpent = Integer.parseInt(timeSpentField.getText());
            
            if (activityType.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "Please enter an activity type", 
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create entry using editor service
            ActivityEntry entry = editorService.create(activityType, description, timeSpent);
            
            // Set status from combo box
            ActivityEntry updated = new ActivityEntry(
                    entry.activityType(),
                    entry.description(),
                    status,
                    entry.comment(),
                    entry.timestamp(),
                    entry.timeSpent()
            );
            
            editorService.update(activityType, updated.description(), status, timeSpent);
            
            // Store the last description for reuse
            editorService.setLastDescription(description);
            
            setVisible(false);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                    "Please enter a valid number for time spent", 
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onCancel() {
        setVisible(false);
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