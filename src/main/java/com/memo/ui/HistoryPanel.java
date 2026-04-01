package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;
import com.memo.service.HistoryService;
import com.memo.service.KanbanService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Panel for displaying and managing activity history in a table.
 */
public class HistoryPanel extends JPanel {
    
    private HistoryService historyService;
    private EntryEditorService editorService;
    private KanbanService kanbanService;
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    
    public HistoryPanel(HistoryService historyService, 
                       EntryEditorService editorService,
                       KanbanService kanbanService) {
        this.historyService = historyService;
        this.editorService = editorService;
        this.kanbanService = kanbanService;
        
        setLayout(new BorderLayout());
        
        createTable();
        createScrollPane();
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void createTable() {
        String[] columnNames = {"Type", "Description", "Status", "Time (min)", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        
        // Set renderers
        table.setDefaultRenderer(Object.class, new CellRenderer());
    }
    
    private void createScrollPane() {
        scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Activity History"));
    }
    
    public void refreshTable() {
        tableModel.setRowCount(0);
        
        // Get all entries and sort by timestamp (newest first)
        List<ActivityEntry> sortedEntries = historyService.getAll().stream()
                .sorted((e1, e2) -> e2.timestamp().compareTo(e1.timestamp()))
                .toList();
        
        for (ActivityEntry entry : sortedEntries) {
            Object[] row = {
                    entry.activityType(),
                    entry.description(),
                    entry.status(),
                    String.valueOf(entry.timeSpent()),
                    formatTimestamp(entry.timestamp())
            };
            tableModel.addRow(row);
        }
    }
    
    private String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public void loadEntries() {
        refreshTable();
    }
    
    /**
     * Custom cell renderer for better appearance.
     */
    private class CellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            super.setValue(value);
            
            String status = "";
            if (value != null) {
                status = value.toString();
            }
            
            // Color coding based on status
            switch (status.toUpperCase()) {
                case "DONE":
                    setForeground(Color.GREEN);
                    break;
                case "DOING":
                    setForeground(Color.ORANGE);
                    break;
                case "TODO":
                    setForeground(Color.BLUE);
                    break;
                default:
                    setForeground(Color.BLACK);
            }
        }
    }
}