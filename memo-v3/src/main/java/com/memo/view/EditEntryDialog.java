package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog for editing an existing entry.
 * UC-012: Edit activity entry
 */
public class EditEntryDialog extends JDialog {
    
    private final EntryEditorService entryEditorService;
    
    private final JComboBox<String> activityTypeCombo;
    private final JComboBox<String> descriptionCombo;
    private final JComboBox<String> statusCombo;
    private final JTextArea commentArea;
    private final JSpinner timeSpinner;
    
    private ActivityEntry editedEntry;
    
    private static final String[] ACTIVITY_TYPES = {"DEV", "CEREMONY", "SUPPORT", "LEARNING", "ADMIN"};
    private static final String[] STATUSES = {"TODO", "DOING", "DONE", "NOTE"};
    
    public EditEntryDialog(Frame owner, ActivityEntry entryToEdit, EntryEditorService entryEditorService) {
        super(owner, "Edit Entry", true);
        this.entryEditorService = entryEditorService;
        
        activityTypeCombo = new JComboBox<>(ACTIVITY_TYPES);
        descriptionCombo = new JComboBox<>();
        descriptionCombo.setEditable(true);
        statusCombo = new JComboBox<>(STATUSES);
        commentArea = new JTextArea(3, 20);
        timeSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        
        initComponents(entryToEdit);
    }
    
    private void initComponents(ActivityEntry entryToEdit) {
        setLayout(new GridLayout(6, 2, 10, 5));
        setTitle("Edit Activity Entry");
        
        add(new JLabel("Activity Type:"));
        activityTypeCombo.addItem(entryToEdit.activityType());
        activityTypeCombo.setSelectedItem(entryToEdit.activityType());
        add(activityTypeCombo);
        
        add(new JLabel("Description:"));
        descriptionCombo.addItem(entryToEdit.description());
        descriptionCombo.setSelectedItem(entryToEdit.description());
        add(descriptionCombo);
        
        add(new JLabel("Status:"));
        statusCombo.addItem(entryToEdit.status());
        statusCombo.setSelectedItem(entryToEdit.status());
        add(statusCombo);
        
        add(new JLabel("Comment:"));
        commentArea.setText(entryToEdit.comment());
        JScrollPane commentScrollPane = new JScrollPane(commentArea);
        add(commentScrollPane);
        
        add(new JLabel("Time (hours):"));
        timeSpinner.setValue(entryToEdit.timeSpent());
        add(timeSpinner);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveAndClose());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);
        add(buttonPanel);
        
        pack();
        setLocationRelativeTo(getOwner());
    }
    
    private void saveAndClose() {
        String activityType = (String) activityTypeCombo.getSelectedItem();
        String description = (String) descriptionCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();
        String comment = commentArea.getText();
        Double timeSpent = (Double) timeSpinner.getValue();
        
        try {
            ActivityEntry newEntry = new ActivityEntry(
                    activityType, description, status, comment,
                    editedEntry.timestamp(), timeSpent
            );
            
            boolean saved = entryEditorService.updateEntry(editedEntry, newEntry);
            
            if (saved) {
                editedEntry = newEntry;
                JOptionPane.showMessageDialog(this, "Entry updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save changes.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public ActivityEntry getEditedEntry() {
        return editedEntry;
    }
}
