package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.HistoryService;
import com.memo.service.SearchService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for searching activity entries.
 * UC-003: Search activity entries
 * UC-004: Display search results with time sum
 */
public class SearchPanel extends JPanel {
    
    private final SearchService searchService;
    private final HistoryService historyService;
    
    private JTextField searchField;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    
    private static final String[] ALL_TYPES = {"ALL", "DEV", "CEREMONY", "SUPPORT", "LEARNING", "ADMIN"};
    private static final String[] ALL_STATUSES = {"ALL", "TODO", "DOING", "DONE", "NOTE"};
    
    public SearchPanel(SearchService searchService, HistoryService historyService) {
        this.searchService = searchService;
        this.historyService = historyService;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Search controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controls.setBorder(BorderFactory.createTitledBorder("Search"));
        
        controls.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        controls.add(searchField);
        
        controls.add(new JLabel("Type:"));
        typeFilterCombo = new JComboBox<>(ALL_TYPES);
        typeFilterCombo.setSelectedIndex(0);
        controls.add(typeFilterCombo);
        
        controls.add(new JLabel("Status:"));
        statusFilterCombo = new JComboBox<>(ALL_STATUSES);
        statusFilterCombo.setSelectedIndex(0);
        controls.add(statusFilterCombo);
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        controls.add(searchButton);
        
        add(controls, BorderLayout.NORTH);
        
        // Results table
        String[] columns = {"Type", "Description", "Status", "Comment", "Timestamp", "Time (h)"};
        tableModel = new DefaultTableModel(columns, 0);
        resultsTable = new JTable(tableModel);
        resultsTable.setAutoCreateRowSorter(true);
        resultsTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Summary panel
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BorderLayout());
        JLabel summaryLabel = new JLabel("Enter search criteria and click Search");
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.BOLD));
        summaryPanel.add(summaryLabel, BorderLayout.CENTER);
        add(summaryPanel, BorderLayout.SOUTH);
    }
    
    private void performSearch() {
        String query = searchField.getText().trim().toLowerCase();
        String typeFilter = (String) typeFilterCombo.getSelectedItem();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        
        List<ActivityEntry> results = searchService.search(
                historyService.getHistory(), query, typeFilter, statusFilter
        );
        
        // Update table
        tableModel.setRowCount(0);
        for (ActivityEntry entry : results) {
            tableModel.addRow(new Object[]{
                    entry.activityType(),
                    entry.description(),
                    entry.status(),
                    entry.comment(),
                    entry.timestamp(),
                    String.format("%.2f", entry.timeSpent())
            });
        }
        
        // Update summary
        updateSummary(results);
    }
    
    private void updateSummary(List<ActivityEntry> results) {
        JPanel summaryPanel = (JPanel) getComponents()[2];
        summaryPanel.removeAll();
        
        if (results.isEmpty()) {
            JLabel emptyLabel = new JLabel("No results found.");
            emptyLabel.setFont(emptyLabel.getFont().deriveFont(Font.BOLD));
            summaryPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            JPanel content = new JPanel(new BorderLayout());
            
            // Table summary
            JLabel countLabel = new JLabel(String.format("%d entries found", results.size()));
            countLabel.setFont(countLabel.getFont().deriveFont(Font.BOLD));
            content.add(countLabel, BorderLayout.NORTH);
            
            // Total time
            double totalTime = results.stream()
                    .mapToDouble(ActivityEntry::timeSpent)
                    .sum();
            JLabel timeLabel = new JLabel(String.format("Total time: %.2f hours", totalTime));
            content.add(timeLabel, BorderLayout.CENTER);
            
            summaryPanel.add(content, BorderLayout.CENTER);
        }
        
        summaryPanel.revalidate();
        summaryPanel.repaint();
    }
}
