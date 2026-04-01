package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Unified panel combining History, Search, and Summary views.
 */
public class ActivitiesPanel extends JPanel {
    
    private HistoryService historyService;
    private EntryEditorService editorService;
    private SearchService searchService;
    private SummaryService summaryService;
    private TimeCalculationService timeCalcService;
    
    // Search components
    private JTextField searchDescriptionField;
    private JTextField searchTypeField;
    private JComboBox<String> statusComboBox;
    
    // Table components
    private JTable historyTable;
    private DefaultTableModel tableModel;
    
    // Summary components
    private JLabel dailyTimeLabel;
    private JLabel dailyCountLabel;
    private JLabel weeklyTimeLabel;
    private JLabel weeklyCountLabel;
    
    public ActivitiesPanel(HistoryService historyService, 
                          EntryEditorService editorService,
                          SearchService searchService,
                          SummaryService summaryService,
                          TimeCalculationService timeCalcService) {
        this.historyService = historyService;
        this.editorService = editorService;
        this.searchService = searchService;
        this.summaryService = summaryService;
        this.timeCalcService = timeCalcService;
        
        setLayout(new BorderLayout());
        
        createSearchPanel();
        createHistoryTable();
        createSummaryPanel();
        
        refreshAll();
    }
    
    private void createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setBorder(BorderFactory.createTitledBorder("Filters"));
        searchPanel.setOpaque(true);
        searchPanel.setBackground(Color.LIGHT_GRAY);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Description filter
        gbc.gridx = 0;
        gbc.gridy = 0;
        searchPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        searchDescriptionField = new JTextField(15);
        searchDescriptionField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });
        searchPanel.add(searchDescriptionField, gbc);
        
        // Type filter
        gbc.gridx = 2;
        searchPanel.add(new JLabel("Type:"), gbc);
        
        gbc.gridx = 3;
        searchTypeField = new JTextField(10);
        searchTypeField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });
        searchPanel.add(searchTypeField, gbc);
        
        // Status filter
        gbc.gridx = 4;
        searchPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 5;
        String[] statuses = {"ALL", "TODO", "DOING", "DONE", "NOTE"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.addActionListener(e -> applyFilters());
        searchPanel.add(statusComboBox, gbc);
        
        // Clear button
        gbc.gridx = 6;
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearFilters());
        searchPanel.add(clearButton, gbc);
        
        add(searchPanel, BorderLayout.NORTH);
    }
    
    private void createHistoryTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        
        // Table header info
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.add(new JLabel("<b>Activity History</b>"));
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(new JLabel("Entries: "));
        infoPanel.add(new JLabel("0")); // Will be updated
        tablePanel.add(infoPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"Type", "Description", "Status", "Time (min)", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(25);
        historyTable.setAutoCreateRowSorter(true);
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(250);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        
        // Add color coding for status
        historyTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(tablePanel, BorderLayout.CENTER);
    }
    
    private void createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Quick Summary"));
        summaryPanel.setOpaque(true);
        summaryPanel.setBackground(Color.LIGHT_GRAY);
        
        // Daily stats
        JPanel dailyPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        dailyPanel.setBorder(BorderFactory.createTitledBorder("Today"));
        dailyTimeLabel = new JLabel("Total: 0h 0m");
        dailyCountLabel = new JLabel("Activities: 0");
        dailyPanel.add(dailyTimeLabel);
        dailyPanel.add(dailyCountLabel);
        
        // Weekly stats
        JPanel weeklyPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        weeklyPanel.setBorder(BorderFactory.createTitledBorder("This Week"));
        weeklyTimeLabel = new JLabel("Total: 0h 0m");
        weeklyCountLabel = new JLabel("Activities: 0");
        weeklyPanel.add(weeklyTimeLabel);
        weeklyPanel.add(weeklyCountLabel);
        
        summaryPanel.add(dailyPanel);
        summaryPanel.add(weeklyPanel);
        
        add(summaryPanel, BorderLayout.SOUTH);
    }
    
    private void applyFilters() {
        String descFilter = searchDescriptionField.getText().trim();
        String typeFilter = searchTypeField.getText().trim();
        String statusFilter = (String) statusComboBox.getSelectedItem();
        
        List<ActivityEntry> results;
        
        if (descFilter.isEmpty() && typeFilter.isEmpty()) {
            // Just filter by status
            results = statusFilter.equals("ALL") ? 
                    searchService.getAllEntries() : 
                    searchService.getEntriesByStatus(statusFilter);
        } else {
            // Combined search
            results = searchService.searchByDescriptionAndActivityType(descFilter, typeFilter);
            
            if (!statusFilter.equals("ALL")) {
                results = results.stream()
                        .filter(entry -> entry.status().equals(statusFilter))
                        .toList();
            }
        }
        
        populateTable(results);
    }
    
    private void clearFilters() {
        searchDescriptionField.setText("");
        searchTypeField.setText("");
        statusComboBox.setSelectedIndex(0);
        applyFilters();
    }
    
    private void populateTable(List<ActivityEntry> entries) {
        tableModel.setRowCount(0);
        
        // Sort by timestamp DESC (newest first)
        List<ActivityEntry> sortedEntries = entries.stream()
                .sorted((e1, e2) -> e2.timestamp().compareTo(e1.timestamp()))
                .toList();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (ActivityEntry entry : sortedEntries) {
            Object[] row = {
                entry.activityType(),
                entry.description(),
                entry.status(),
                String.valueOf(entry.timeSpent()),
                entry.timestamp().format(formatter)
            };
            tableModel.addRow(row);
        }
    }
    
    public void refreshAll() {
        // Refresh table with all entries
        List<ActivityEntry> allEntries = historyService.getAll();
        populateTable(allEntries);
        
        // Refresh summary
        SummaryService.Summary daily = summaryService.getDailySummary();
        dailyTimeLabel.setText("Total: " + timeCalcService.formatTime(daily.getTotalTime()));
        dailyCountLabel.setText("Activities: " + daily.getActivityCount());
        
        SummaryService.Summary weekly = summaryService.getWeeklySummary();
        weeklyTimeLabel.setText("Total: " + timeCalcService.formatTime(weekly.getTotalTime()));
        weeklyCountLabel.setText("Activities: " + weekly.getActivityCount());
    }
    
    /**
     * Custom cell renderer for status color coding.
     */
    private class StatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            super.setValue(value);
            
            if (value == null) return;
            
            String status = value.toString().toUpperCase();
            switch (status) {
                case "DONE":
                    setForeground(Color.GREEN);
                    break;
                case "DOING":
                    setForeground(Color.ORANGE);
                    break;
                case "TODO":
                    setForeground(Color.BLUE);
                    break;
                case "NOTE":
                    setForeground(new Color(128, 0, 128)); // Magenta
                    break;
                default:
                    setForeground(Color.BLACK);
            }
        }
    }
}
