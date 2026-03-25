package com.memo.view;

import com.memo.service.*;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame.
 * UC-008: Resize application components
 */
public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private EntryPanel entryPanel;
    private HistoryPanel historyPanel;
    private SearchPanel searchPanel;
    private SummaryPanel summaryPanel;
    private KanbanPanel kanbanPanel;
    private SettingsPanel settingsPanel;
    
    private SettingsService settingsService;
    
    public MainFrame(EntryEditorService entryEditorService, HistoryService historyService, SettingsService settingsService) {
        this.settingsService = settingsService;
        initComponents(entryEditorService, historyService);
    }
    
    private void initComponents(EntryEditorService entryEditorService, HistoryService historyService) {
        setTitle("MEMO_V3 - Activity Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationByPlatform(true);
        
        setLayout(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        entryPanel = new EntryPanel(entryEditorService, historyService);
        historyPanel = new HistoryPanel(historyService, entryEditorService);
        searchPanel = new SearchPanel(new SearchService(), historyService);
        summaryPanel = new SummaryPanel(new SummaryService(), historyService);
        kanbanPanel = new KanbanPanel(new KanbanService(), historyService);
        settingsPanel = new SettingsPanel(settingsService);
        
        tabbedPane.addTab("New Entry", entryPanel);
        tabbedPane.addTab("History", historyPanel);
        tabbedPane.addTab("Search", searchPanel);
        tabbedPane.addTab("Summary", summaryPanel);
        tabbedPane.addTab("Kanban", kanbanPanel);
        tabbedPane.addTab("Settings", settingsPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        setMinimumSize(new Dimension(800, 500));
        setVisible(true);
    }
    
    public void refreshHistory() {
        historyPanel.refreshHistory();
    }
}
