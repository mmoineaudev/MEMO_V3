package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.EntryEditorService;
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
        this(historyService, null);
    }
    
    /**
     * Create a history panel with history service and entry editor for editing.
     */
    public HistoryPanel(HistoryService historyService, EntryEditorService entryEditorService) {
        this.historyService = historyService;
        this.entryEditorService = entryEditorService;
        initComponents();
    }
    
    private EntryEditorService entryEditorService;
    
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
        
        // Double-click to edit
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (entryEditorService != null && e.getClickCount() == 2) {
                    int row = historyTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        ActivityEntry entry = (ActivityEntry) historyTable.getValueAt(row, 0);
                        showEditDialog(entry);
                    }
                }
            }
        });
        
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
                    String.format("%.2f", entry.timeSpent()),
                    entry  // Store entry object for editing
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * Show edit dialog for an entry.
     */
    private void showEditDialog(ActivityEntry entry) {
        EditEntryDialog dialog = new EditEntryDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                entry,
                entryEditorService
        );
        dialog.setVisible(true);
        
        if (dialog.getEditedEntry() != null) {
            refreshHistory();
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
