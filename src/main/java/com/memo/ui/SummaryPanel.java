package com.memo.ui;

import com.memo.service.SummaryService;
import com.memo.service.TimeCalculationService;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Panel for displaying daily and weekly summaries.
 */
public class SummaryPanel extends JPanel {
    
    private SummaryService summaryService;
    private TimeCalculationService timeCalcService;
    
    private JSpinner dateSpinner;
    private JLabel dailyTimeLabel;
    private JLabel dailyCountLabel;
    private JLabel dailyAvgLabel;
    private JLabel weeklyTimeLabel;
    private JLabel weeklyCountLabel;
    private JLabel weeklyAvgLabel;
    
    public SummaryPanel(SummaryService summaryService, TimeCalculationService timeCalcService) {
        this.summaryService = summaryService;
        this.timeCalcService = timeCalcService;
        
        setLayout(new BorderLayout());
        
        createSummaryDisplay();
        refreshSummaries();
    }
    
    private void createSummaryDisplay() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Date selector
        JPanel dateSelector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateSelector.setBorder(BorderFactory.createTitledBorder("Select Date"));
        dateSelector.add(new JLabel("Date: "));
        
        SpinnerDateModel dateModel = new SpinnerDateModel();
        dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSelector.add(dateSpinner);
        
        JButton selectButton = new JButton("Show Summary");
        selectButton.addActionListener(e -> refreshSummaries());
        dateSelector.add(selectButton);
        
        mainPanel.add(dateSelector);
        
        // Daily and Weekly Summary cards
        JPanel summariesPanel = new JPanel(new GridLayout(2, 1, 10, 0));
        
        // Daily Summary
        JPanel dailyPanel = createDailySummaryCard();
        summariesPanel.add(dailyPanel);
        
        // Weekly Summary  
        JPanel weeklyPanel = createWeeklySummaryCard();
        summariesPanel.add(weeklyPanel);
        
        mainPanel.add(summariesPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createDailySummaryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder("Daily Summary"));
        
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Daily Statistics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.BLUE);
        headerPanel.add(titleLabel);
        card.add(headerPanel, BorderLayout.NORTH);
        
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        dailyTimeLabel = new JLabel("Total Time: -");
        dailyCountLabel = new JLabel("Activities: -");
        dailyAvgLabel = new JLabel("Average per activity: -");
        
        statsPanel.add(dailyTimeLabel);
        statsPanel.add(dailyCountLabel);
        statsPanel.add(dailyAvgLabel);
        
        card.add(statsPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createWeeklySummaryCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder("Weekly Summary"));
        
        JPanel headerPanel = new JPanel();
        JLabel titleLabel = new JLabel("Last 7 Days Statistics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(Color.GREEN);
        headerPanel.add(titleLabel);
        card.add(headerPanel, BorderLayout.NORTH);
        
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        weeklyTimeLabel = new JLabel("Total Time: -");
        weeklyCountLabel = new JLabel("Activities: -");
        weeklyAvgLabel = new JLabel("Average per activity: -");
        
        statsPanel.add(weeklyTimeLabel);
        statsPanel.add(weeklyCountLabel);
        statsPanel.add(weeklyAvgLabel);
        
        card.add(statsPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void refreshSummaries() {
        // Get daily summary for selected date
        SummaryService.Summary daily = summaryService.getDailySummary();
        
        // Update daily labels
        String dailyTimeFormatted = timeCalcService.formatTime(daily.getTotalTime());
        dailyTimeLabel.setText("Total Time: " + dailyTimeFormatted);
        dailyCountLabel.setText("Activities: " + daily.getActivityCount());
        double dailyAvg = daily.getActivityCount() > 0 ? 
                (double) daily.getTotalTime() / daily.getActivityCount() : 0;
        dailyAvgLabel.setText("Average per activity: " + String.format("%.1f min", dailyAvg));
        
        // Get weekly summary
        SummaryService.Summary weekly = summaryService.getWeeklySummary();
        
        // Update weekly labels
        String weeklyTimeFormatted = timeCalcService.formatTime(weekly.getTotalTime());
        weeklyTimeLabel.setText("Total Time: " + weeklyTimeFormatted);
        weeklyCountLabel.setText("Activities: " + weekly.getActivityCount());
        double weeklyAvg = weekly.getActivityCount() > 0 ? 
                (double) weekly.getTotalTime() / weekly.getActivityCount() : 0;
        weeklyAvgLabel.setText("Average per activity: " + String.format("%.1f min", weeklyAvg));
    }
}