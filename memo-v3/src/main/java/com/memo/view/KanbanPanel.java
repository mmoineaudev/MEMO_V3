package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.HistoryService;
import com.memo.service.KanbanService;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * Kanban board panel.
 * UC-015: Display Kanban view
 * UC-016: Interact with Kanban entries
 */
public class KanbanPanel extends JPanel {
    
    private final KanbanService kanbanService;
    private final HistoryService historyService;
    
    private JPanel columnsPanel;
    
    public KanbanPanel(KanbanService kanbanService, HistoryService historyService) {
        this.kanbanService = kanbanService;
        this.historyService = historyService;
        initComponents();
        refreshKanban();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        columnsPanel = new JPanel(new GridLayout(1, 1, 10, 10));
        columnsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        columnsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(columnsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void refreshKanban() {
        columnsPanel.removeAll();
        
        // Use getKanbanBoard() to group entries by status (TODO/DOING/DONE/NOTE)
        Map<String, java.util.List<ActivityEntry>> grouped = kanbanService.getKanbanBoard(
                historyService.getHistory()
        );
        
        if (grouped.isEmpty()) {
            JLabel empty = new JLabel("No entries found.");
            empty.setFont(empty.getFont().deriveFont(16f));
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            columnsPanel.add(empty);
        } else {
            // Define column order for Kanban board
            String[] columnOrder = {"TODO", "DOING", "DONE", "NOTE"};
            
            for (String status : columnOrder) {
                java.util.List<ActivityEntry> entries = grouped.get(status);
                if (entries != null && !entries.isEmpty()) {
                    createColumn(status, entries);
                }
            }
        }
        
        columnsPanel.revalidate();
        columnsPanel.repaint();
    }
    
    private void createColumn(String description, java.util.List<ActivityEntry> entries) {
        // Column header
        JPanel column = new JPanel(new BorderLayout(0, 5));
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(200, 0));
        column.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Header with count
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setOpaque(false);
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(descLabel.getFont().deriveFont(Font.BOLD));
        header.add(descLabel);
        
        JLabel countBadge = new JLabel(String.format("(%d)", entries.size()));
        countBadge.setFont(countBadge.getFont().deriveFont(10f));
        countBadge.setForeground(Color.GRAY);
        header.add(countBadge);
        
        column.add(header, BorderLayout.NORTH);
        
        // Cards container
        JPanel cardsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        cardsPanel.setOpaque(false);
        
        for (ActivityEntry entry : entries) {
            cardsPanel.add(createCard(entry));
        }
        
        column.add(cardsPanel, BorderLayout.CENTER);
        columnsPanel.add(column);
    }
    
    private JPanel createCard(ActivityEntry entry) {
        JPanel card = new JPanel(new BorderLayout(3, 3));
        card.setOpaque(true);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        card.setPreferredSize(new Dimension(180, 80));
        
        // Timestamp
        JLabel timeLabel = new JLabel(entry.timestamp());
        timeLabel.setFont(timeLabel.getFont().deriveFont(10f));
        timeLabel.setForeground(Color.GRAY);
        card.add(timeLabel, BorderLayout.NORTH);
        
        // Status with color
        JLabel statusLabel = new JLabel(entry.status());
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        setStatusColor(statusLabel, entry.status());
        card.add(statusLabel, BorderLayout.CENTER);
        
        // Time
        JLabel timeLabel2 = new JLabel(String.format("%.2f h", entry.timeSpent()));
        timeLabel2.setFont(timeLabel2.getFont().deriveFont(11f));
        card.add(timeLabel2, BorderLayout.SOUTH);
        
        // Click to edit
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showEntryDetails(entry);
            }
        });
        
        return card;
    }
    
    private void setStatusColor(JLabel label, String status) {
        if (status == null) {
            label.setForeground(Color.WHITE);
        } else {
            switch (status.toUpperCase()) {
                case "TODO":
                    label.setForeground(new Color(255, 215, 0));
                    break;
                case "DOING":
                    label.setForeground(new Color(30, 144, 255));
                    break;
                case "DONE":
                    label.setForeground(new Color(50, 205, 50));
                    break;
                case "NOTE":
                    label.setForeground(new Color(128, 128, 128));
                    break;
                default:
                    label.setForeground(Color.WHITE);
            }
        }
    }
    
    private void showEntryDetails(ActivityEntry entry) {
        String details = String.format(
                "Activity: %s%nDescription: %s%nStatus: %s%nComment: %s%nTime: %.2f hours%nTimestamp: %s",
                entry.activityType(),
                entry.description(),
                entry.status(),
                entry.comment(),
                entry.timeSpent(),
                entry.timestamp()
        );
        
        JOptionPane.showMessageDialog(this, details, "Entry Details", 
                JOptionPane.INFORMATION_MESSAGE);
    }
}
