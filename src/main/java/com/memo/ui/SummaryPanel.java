package com.memo.ui;

import com.memo.service.SummaryService;
import com.memo.service.TimeCalculationService;
import javax.swing.*;
import java.awt.*;

/**
 * Panel for displaying daily and weekly summaries.
 */
public class SummaryPanel extends JPanel {
    
    private SummaryService summaryService;
    private TimeCalculationService timeCalcService;
    
    public SummaryPanel(SummaryService summaryService, TimeCalculationService timeCalcService) {
        this.summaryService = summaryService;
        this.timeCalcService = timeCalcService;
        
        setLayout(new BorderLayout());
        
        createSummaryDisplay();
    }
    
    private void createSummaryDisplay() {
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Daily Summary
        JPanel dailyPanel = createSummaryCard("Daily Summary", "Today's Statistics");
        mainPanel.add(dailyPanel);
        
        // Weekly Summary
        JPanel weeklyPanel = createSummaryCard("Weekly Summary", "Last 7 Days Statistics");
        mainPanel.add(weeklyPanel);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSummaryCard(String title, String subtitle) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createTitledBorder(title));
        
        // Title and subtitle
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLUE);
        
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        headerPanel.add(titleLabel);
        headerPanel.add(subLabel);
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Stats panel
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 0));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel timeLabel = new JLabel("Total Time: -");
        JLabel countLabel = new JLabel("Activities: -");
        JLabel avgLabel = new JLabel("Average per activity: -");
        
        statsPanel.add(timeLabel);
        statsPanel.add(countLabel);
        statsPanel.add(avgLabel);
        
        card.add(statsPanel, BorderLayout.CENTER);
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        card.add(refreshButton, BorderLayout.SOUTH);
        
        refreshButton.addActionListener(e -> updateSummaryStats(statsPanel));
        
        return card;
    }
    
    private void updateSummaryStats(JPanel statsPanel) {
        // Daily summary
        SummaryService.Summary daily = summaryService.getDailySummary();
        
        // Weekly summary
        SummaryService.Summary weekly = summaryService.getWeeklySummary();
        
        // Update labels
        for (Component comp : statsPanel.getComponents()) {
            if (comp instanceof JLabel label) {
                String text = label.getText();
                
                if (text.startsWith("Total Time:")) {
                    String formattedTime = timeCalcService.formatTime(daily.getTotalTime());
                    label.setText("Total Time: " + formattedTime);
                } else if (text.startsWith("Activities:")) {
                    label.setText("Activities: " + daily.getActivityCount());
                } else if (text.startsWith("Average per activity:")) {
                    double avg = timeCalcService.calculateAverageTimePerEntry();
                    label.setText("Average per activity: " + String.format("%.1f min", avg));
                }
            }
        }
    }
}