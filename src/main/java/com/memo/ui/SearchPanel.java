package com.memo.ui;

import com.memo.model.ActivityEntry;
import com.memo.service.SearchService;
import com.memo.service.TimeCalculationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel for searching and filtering activity entries.
 */
public class SearchPanel extends JPanel {
    
    private SearchService searchService;
    private TimeCalculationService timeCalcService;
    
    private JTextField searchDescriptionField;
    private JTextField searchTypeField;
    private JComboBox<String> statusComboBox;
    private JButton searchButton;
    private JLabel resultCountLabel;
    private JLabel totalTimeLabel;
    private DefaultTableModel tableModel;
    
    public SearchPanel(SearchService searchService, 
                      TimeCalculationService timeCalcService) {
        this.searchService = searchService;
        this.timeCalcService = timeCalcService;
        
        setLayout(new BorderLayout());
        
        createSearchControls();
        createResultsArea();
    }
    
    private void createSearchControls() {
        JPanel controlsPanel = new JPanel(new GridBagLayout());
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Search Filters"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Search by Description
        gbc.gridx = 0;
        gbc.gridy = 0;
        controlsPanel.add(new JLabel("Description:"), gbc);
        
        gbc.gridx = 1;
        searchDescriptionField = new JTextField(20);
        controlsPanel.add(searchDescriptionField, gbc);
        
        // Search by Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        controlsPanel.add(new JLabel("Activity Type:"), gbc);
        
        gbc.gridx = 1;
        searchTypeField = new JTextField(20);
        controlsPanel.add(searchTypeField, gbc);
        
        // Search by Status
        gbc.gridx = 0;
        gbc.gridy = 2;
        controlsPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        String[] statuses = {"ALL", "TODO", "DOING", "DONE"};
        statusComboBox = new JComboBox<>(statuses);
        statusComboBox.setSelectedIndex(0);
        controlsPanel.add(statusComboBox, gbc);
        
        // Search Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        searchButton = new JButton("Search");
        controlsPanel.add(searchButton, gbc);
        
        add(controlsPanel, BorderLayout.NORTH);
        
        // Action listener
        searchButton.addActionListener(e -> performSearch());
    }
    
    private void createResultsArea() {
        JPanel resultsArea = new JPanel(new BorderLayout());
        resultsArea.setBorder(BorderFactory.createTitledBorder("Search Results"));
        
        // Labels for results
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        resultCountLabel = new JLabel("Results: 0 entries");
        totalTimeLabel = new JLabel("Total time: 0h 0m");
        
        infoPanel.add(resultCountLabel);
        infoPanel.add(totalTimeLabel);
        
        resultsArea.add(infoPanel, BorderLayout.NORTH);
        
        // Table for results
        String[] columnNames = {"Type", "Description", "Status", "Time (min)", "Timestamp"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(120);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setPreferredWidth(70);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(table);
        resultsArea.add(scrollPane, BorderLayout.CENTER);
        
        add(resultsArea, BorderLayout.CENTER);
    }
    
    private void performSearch() {
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
        
        // Update labels
        resultCountLabel.setText("Results: " + results.size() + " entries");
        
        int totalTime = searchService.getTotalTimeSpent(results);
        String formattedTime = timeCalcService.formatTime(totalTime);
        totalTimeLabel.setText("Total time: " + formattedTime);
        
        // Populate table with results
        populateTable(results);
    }
    
    private void populateTable(List<ActivityEntry> entries) {
        tableModel.setRowCount(0); // Clear existing rows
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (ActivityEntry entry : entries) {
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
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public void performSearchByDescription(String desc) {
        searchDescriptionField.setText(desc);
        performSearch();
    }
}