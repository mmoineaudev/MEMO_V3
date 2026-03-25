package com.memo.view;

import com.memo.service.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame.
 * UC-008: Resize application components using GridBagLayout
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private EntryPanel entryPanel;
    private HistoryPanel historyPanel;
    
    /**
     * Create the main frame with the given services.
     */
    public MainFrame(EntryEditorService entryEditorService, HistoryService historyService) {
        initComponents(entryEditorService, historyService);
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents(EntryEditorService entryEditorService, HistoryService historyService) {
        setTitle("MEMO_V2 - Activity Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationByPlatform(true);
        
        // Create layout with GridBagLayout for resizable components
        setLayout(new BorderLayout());
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create panels
        entryPanel = new EntryPanel(entryEditorService, historyService);
        historyPanel = new HistoryPanel(historyService);
        
        tabbedPane.addTab("New Entry", entryPanel);
        tabbedPane.addTab("History", historyPanel);
        
        // Add to frame
        add(tabbedPane, BorderLayout.CENTER);
        
        // Set minimum size for better UX
        setMinimumSize(new Dimension(700, 500));
        
        // Show frame
        setVisible(true);
    }
    
    /**
     * Refresh the history view.
     */
    public void refreshHistory() {
        historyPanel.refreshHistory();
    }
}
