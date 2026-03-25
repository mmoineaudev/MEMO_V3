package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.HistoryService;
import com.memo.view.components.ColoredStatusLabel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel for displaying all activity history.
 * UC-002: Display history of all entries
 */
public class HistoryPanel extends JPanel {
    
    private final HistoryService historyService;
    private JTable historyTable;
    private DefaultTableModel tableModel;
    
    /**
     * Create a history panel with the given history service.
     */
    public HistoryPanel(HistoryService historyService) {
        this.historyService = historyService;
        initComponents();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Create table model
        String[] columns = {"Activity Type", "Description", "Status", "Comment", "Timestamp", "Time (h)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        
        // Create table
        historyTable = new JTable(tableModel);
        historyTable.setAutoCreateRowSorter(true);
        historyTable.setRowHeight(25);
        
        // Render status column with colors
        historyTable.getColumnModel().getColumn(2).setCellRenderer(new ColoredStatusRenderer());
        
        // Wrap table in scroll pane
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Refresh history
        refreshHistory();
    }
    
    /**
     * Refresh the history table.
     */
    public void refreshHistory() {
        tableModel.setRowCount(0);
        
        java.util.List<ActivityEntry> entries = historyService.getHistory();
        
        for (ActivityEntry entry : entries) {
            Object[] row = {
                    entry.activityType(),
                    entry.description(),
                    entry.status(),
                    entry.comment(),
                    entry.timestamp(),
                    String.format("%.2f", entry.timeSpent())
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Custom renderer for status column with color coding.
     */
    private static class ColoredStatusRenderer extends DefaultTableCellRenderer {
        
        private final ColoredStatusLabel label = new ColoredStatusLabel("");
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (isSelected) {
                label.setForeground(table.getSelectionForeground());
                label.setBackground(table.getSelectionBackground());
            } else {
                label.setForeground(table.getForeground());
                label.setBackground(table.getBackground());
            }
            
            label.setStatus(value != null ? value.toString() : "");
            return label;
        }
    }
}
