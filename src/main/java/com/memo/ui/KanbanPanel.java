package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.model.Status;
import com.memo.service.EntryEditorService;
import com.memo.service.HistoryService;
import com.memo.service.KanbanService;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Panel for displaying activities in a Kanban board layout.
 * Supports status changes and entry editing.
 */
public class KanbanPanel extends JPanel {
    
    private KanbanService kanbanService;
    private EntryEditorService editorService;
    private HistoryService historyService;
    
    public KanbanPanel(KanbanService kanbanService, EntryEditorService editorService) {
        this.kanbanService = kanbanService;
        this.editorService = editorService;
        this.historyService = editorService.getHistoryService();
        
        setLayout(new BorderLayout());
        
        createKanbanBoard();
    }
    
    private void createKanbanBoard() {
        JPanel boardPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        boardPanel.setBorder(BorderFactory.createTitledBorder("Kanban Board"));
        
        // TODO column
        JPanel todoColumn = createKanbanColumn(Status.TODO);
        boardPanel.add(todoColumn);
        
        // DOING column
        JPanel doingColumn = createKanbanColumn(Status.DOING);
        boardPanel.add(doingColumn);
        
        // DONE column
        JPanel doneColumn = createKanbanColumn(Status.DONE);
        boardPanel.add(doneColumn);
        
        // NOTE column
        JPanel noteColumn = createKanbanColumn(Status.NOTE);
        boardPanel.add(noteColumn);
        
        add(boardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createKanbanColumn(String status) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(Status.getDisplayName(status)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // Column header with count
        int count = kanbanService.getTaskCountByStatus(status);
        JLabel headerLabel = new JLabel(Status.getDisplayName(status) + " (" + count + ")");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        if (status.equals(Status.TODO)) {
            headerLabel.setForeground(Color.BLUE);
        } else if (status.equals(Status.DOING)) {
            headerLabel.setForeground(Color.ORANGE);
        } else if (status.equals(Status.DONE)) {
            headerLabel.setForeground(Color.GREEN);
        } else if (status.equals(Status.NOTE)) {
            headerLabel.setForeground(new Color(128, 0, 128)); // Magenta for NOTE
        }
        
        column.add(headerLabel, BorderLayout.NORTH);
        
        // Cards panel
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        // Load entries for this status
        var entries = kanbanService.getEntriesByStatus(status);
        
        for (ActivityEntry entry : entries) {
            JPanel card = createKanbanCard(entry);
            cardsPanel.add(card);
            cardsPanel.add(Box.createVerticalStrut(5));
        }
        
        column.add(cardsPanel, BorderLayout.CENTER);
        
        // Add button at bottom
        JButton addButton = new JButton("+ Add");
        column.add(addButton, BorderLayout.SOUTH);
        
        return column;
    }
    
    private JPanel createKanbanCard(ActivityEntry entry) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        card.setBackground(Color.WHITE);
        
        // Activity type as title with edit button
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(entry.activityType());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton editButton = new JButton("✎");
        editButton.setToolTipText("Edit entry");
        editButton.setPreferredSize(new Dimension(24, 20));
        editButton.addActionListener(e -> openEditDialog(entry));
        headerPanel.add(editButton, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Description
        JTextArea descArea = new JTextArea(entry.description());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setPreferredSize(new Dimension(0, 40));
        card.add(descArea, BorderLayout.CENTER);
        
        // Comment (if present)
        if (entry.comment() != null && !entry.comment().isEmpty()) {
            JPanel commentPanel = new JPanel(new BorderLayout());
            commentPanel.setBackground(new Color(245, 245, 245));
            JLabel commentLabel = new JLabel("💬 " + entry.comment());
            commentLabel.setFont(new Font("Arial", Font.ITALIC, 10));
            commentLabel.setForeground(Color.GRAY);
            commentPanel.add(commentLabel, BorderLayout.CENTER);
            card.add(commentPanel, BorderLayout.SOUTH);
        }
        
        // Footer with time and action buttons
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel timeLabel = new JLabel(entry.timeSpent() + " min");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 10));
        footerPanel.add(timeLabel);
        
        // Separator
        footerPanel.add(Box.createHorizontalStrut(5));
        footerPanel.add(new JLabel("|"));
        footerPanel.add(Box.createHorizontalStrut(5));
        
        // Move buttons
        JButton movePrev = new JButton("←");
        movePrev.setToolTipText("Move to previous status");
        movePrev.setPreferredSize(new Dimension(28, 20));
        
        JButton moveNext = new JButton("→");
        moveNext.setToolTipText("Move to next status");
        moveNext.setPreferredSize(new Dimension(28, 20));
        
        String currentStatus = entry.status();
        ActivityEntry originalEntry = entry; // Capture for lambda
        
        if (!currentStatus.equals(Status.TODO)) {
            movePrev.setEnabled(true);
            movePrev.addActionListener(e -> changeEntryStatus(originalEntry, getPreviousStatus(currentStatus)));
        } else {
            movePrev.setEnabled(false);
        }
        
        if (currentStatus.equals(Status.TODO)) {
            moveNext.setEnabled(true);
            moveNext.addActionListener(e -> changeEntryStatus(originalEntry, Status.DOING));
        } else if (currentStatus.equals(Status.DOING)) {
            moveNext.setEnabled(true);
            moveNext.addActionListener(e -> changeEntryStatus(originalEntry, Status.DONE));
        } else {
            moveNext.setEnabled(false);
        }
        
        footerPanel.add(movePrev);
        footerPanel.add(moveNext);
        
        card.add(footerPanel, BorderLayout.SOUTH);
        
        // Double-click to edit
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openEditDialog(entry);
                }
            }
        });
        
        return card;
    }
    
    private String getPreviousStatus(String current) {
        if (current.equals(Status.DOING)) return Status.TODO;
        if (current.equals(Status.DONE)) return Status.DOING;
        if (current.equals(Status.NOTE)) return Status.DONE;
        return Status.TODO;
    }
    
    private void changeEntryStatus(ActivityEntry entry, String newStatus) {
        // Get the current entries and update the status
        var allEntries = historyService.getAll();
        
        ActivityEntry updated = null;
        for (ActivityEntry e : allEntries) {
            if (e.activityType().equals(entry.activityType()) && 
                e.timestamp().equals(entry.timestamp())) {
                updated = new ActivityEntry(
                        e.activityType(),
                        e.description(),
                        newStatus,
                        e.comment(),
                        e.timestamp(),
                        e.timeSpent()
                );
                break;
            }
        }
        
        if (updated != null) {
            historyService.update(updated);
            refreshKanban();
        }
    }
    
    private void openEditDialog(ActivityEntry entry) {
        // Create a simple edit dialog for status and comment
        JDialog dialog = new JDialog((Frame)null, "Edit Entry", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 300);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Edit: " + entry.activityType());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        dialog.add(titleLabel, gbc);
        
        // Description (read-only)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        dialog.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        JTextField descField = new JTextField(entry.description());
        descField.setEditable(false);
        dialog.add(descField, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        dialog.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        String[] statuses = {Status.TODO, Status.DOING, Status.DONE, Status.NOTE};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setSelectedItem(entry.status());
        dialog.add(statusCombo, gbc);
        
        // Comment
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        dialog.add(new JLabel("Comment:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        JTextArea commentArea = new JTextArea(entry.comment());
        commentArea.setRows(4);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        dialog.add(scrollPane, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            String newComment = commentArea.getText().trim();
            
            ActivityEntry updated = new ActivityEntry(
                    entry.activityType(),
                    entry.description(),
                    newStatus,
                    newComment.isEmpty() ? null : newComment,
                    entry.timestamp(),
                    entry.timeSpent()
            );
            
            historyService.update(updated);
            dialog.dispose();
            refreshKanban();
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }
    
    /**
     * Refreshes the Kanban board display.
     */
    public void refreshKanban() {
        // Remove old components and recreate
        removeAll();
        createKanbanBoard();
        revalidate();
        repaint();
    }
}