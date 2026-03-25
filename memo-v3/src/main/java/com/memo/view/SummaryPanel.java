package com.memo.view;

import com.memo.model.ActivityEntry;
import com.memo.service.HistoryService;
import com.memo.service.SummaryService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel for displaying time summaries.
 * UC-007: Display weekly summary popup
 */
public class SummaryPanel extends JPanel {
    
    private final SummaryService summaryService;
    private final HistoryService historyService;
    
    private JComboBox<String> weekCombo;
    private JComboBox<LocalDate> dateCombo;
    private JTextArea summaryArea;
    
    public SummaryPanel(SummaryService summaryService, HistoryService historyService) {
        this.summaryService = summaryService;
        this.historyService = historyService;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controls.setBorder(BorderFactory.createTitledBorder("Time Summary"));
        
        controls.add(new JLabel("View:"));
        JRadioButton weeklyRadio = new JRadioButton("Weekly");
        JRadioButton dailyRadio = new JRadioButton("Daily");
        weeklyRadio.setSelected(true);
        
        ButtonGroup group = new ButtonGroup();
        group.add(weeklyRadio);
        group.add(dailyRadio);
        
        controls.add(weeklyRadio);
        controls.add(dailyRadio);
        
        weekCombo = new JComboBox<>();
        dateCombo = new JComboBox<>();
        
        controls.add(new JLabel("Period:"));
        controls.add(weekCombo);
        controls.add(dateCombo);
        
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshSummary(weeklyRadio.isSelected()));
        controls.add(refreshButton);
        
        add(controls, BorderLayout.NORTH);
        
        // Summary text area
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        add(scrollPane, BorderLayout.CENTER);
        
        // Load data
        updatePeriodSelectors(weeklyRadio.isSelected());
    }
    
    private void updatePeriodSelectors(boolean weekly) {
        List<ActivityEntry> entries = historyService.getHistory();
        
        if (weekly) {
            List<String> weeks = summaryService.getWeeksWithEntries(entries);
            weekCombo.removeAllItems();
            for (String week : weeks) {
                weekCombo.addItem(week);
            }
            dateCombo.removeAllItems();
            dateCombo.setEnabled(false);
        } else {
            List<LocalDate> dates = summaryService.getDatesWithEntries(entries);
            dateCombo.removeAllItems();
            for (LocalDate date : dates) {
                dateCombo.addItem(date);
            }
            weekCombo.removeAllItems();
            weekCombo.setEnabled(false);
        }
        
        if (weekCombo.getItemCount() > 0) {
            weekCombo.setSelectedIndex(0);
        }
        if (dateCombo.getItemCount() > 0) {
            dateCombo.setSelectedIndex(0);
        }
    }
    
    private void refreshSummary(boolean weekly) {
        List<ActivityEntry> entries = historyService.getHistory();
        
        if (weekly) {
            String weekKey = (String) weekCombo.getSelectedItem();
            var summary = summaryService.getWeeklySummary(entries, weekKey);
            summaryArea.setText(summaryService.formatSummary(summary));
        } else {
            LocalDate date = (LocalDate) dateCombo.getSelectedItem();
            var summary = summaryService.getDailySummary(entries, date);
            summaryArea.setText(summaryService.formatSummary(summary));
        }
    }
    
    /**
     * Refresh summary using current view selection.
     */
    public void refreshSummary() {
        // Determine which view is selected by checking which combo is enabled
        boolean weekly = weekCombo.isEnabled();
        refreshSummary(weekly);
    }
}
