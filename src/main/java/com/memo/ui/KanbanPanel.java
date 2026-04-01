package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;
import com.memo.service.KanbanService;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying activities in a Kanban board layout.
 */
public class KanbanPanel extends JPanel {
    
    private KanbanService kanbanService;
    private EntryEditorService editorService;
    
    public KanbanPanel(KanbanService kanbanService, EntryEditorService editorService) {
        this.kanbanService = kanbanService;
        this.editorService = editorService;
        
        setLayout(new BorderLayout());
        
        createKanbanBoard();
    }
    
    private void createKanbanBoard() {
        JPanel boardPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        boardPanel.setBorder(BorderFactory.createTitledBorder("Kanban Board"));
        
        // TODO column
        JPanel todoColumn = createKanbanColumn("TODO", "TODO");
        boardPanel.add(todoColumn);
        
        // DOING column
        JPanel doingColumn = createKanbanColumn("DOING", "DOING");
        boardPanel.add(doingColumn);
        
        // DONE column
        JPanel doneColumn = createKanbanColumn("DONE", "DONE");
        boardPanel.add(doneColumn);
        
        // NOTE column
        JPanel noteColumn = createKanbanColumn("NOTE", "NOTE");
        boardPanel.add(noteColumn);
        
        add(boardPanel, BorderLayout.CENTER);
    }
    
    private JPanel createKanbanColumn(String title, String status) {
        JPanel column = new JPanel(new BorderLayout());
        column.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        // Column header with count
        int count = kanbanService.getTaskCountByStatus(status);
        JLabel headerLabel = new JLabel(status + " (" + count + ")");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        if (status.equals("TODO")) {
            headerLabel.setForeground(Color.BLUE);
        } else if (status.equals("DOING")) {
            headerLabel.setForeground(Color.ORANGE);
        } else if (status.equals("DONE")) {
            headerLabel.setForeground(Color.GREEN);
        } else if (status.equals("NOTE")) {
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
        
        // Activity type as title
        JLabel titleLabel = new JLabel(entry.activityType());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        card.add(titleLabel, BorderLayout.NORTH);
        
        // Description
        JTextArea descArea = new JTextArea(entry.description());
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setPreferredSize(new Dimension(0, 50));
        card.add(descArea, BorderLayout.CENTER);
        
        // Time spent at bottom
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel timeLabel = new JLabel(entry.timeSpent() + " min");
        footerPanel.add(timeLabel);
        
        // Move buttons
        JButton moveNext = new JButton("→");
        JButton movePrev = new JButton("←");
        
        if (!entry.status().equals("TODO")) {
            movePrev.addActionListener(e -> kanbanService.moveToPreviousStatus(entry.activityType()));
        }
        
        if (!entry.status().equals("DONE")) {
            moveNext.addActionListener(e -> kanbanService.moveToNextStatus(entry.activityType()));
        }
        
        footerPanel.add(movePrev);
        footerPanel.add(moveNext);
        
        card.add(footerPanel, BorderLayout.SOUTH);
        
        return card;
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