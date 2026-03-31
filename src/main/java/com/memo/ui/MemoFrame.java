package com.memo.ui;

import com.memo.service.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * Main application window for the Memo application.
 */
public class MemoFrame extends JFrame {
    
    private HistoryService historyService;
    private CsvStorageService storageService;
    private EntryEditorService editorService;
    private SearchService searchService;
    private SummaryService summaryService;
    private KanbanService kanbanService;
    private TimeCalculationService timeCalcService;
    private SettingsService settingsService;
    
    private HistoryPanel historyPanel;
    private SearchPanel searchPanel;
    private SummaryPanel summaryPanel;
    private KanbanPanel kanbanPanel;
    
    public MemoFrame() {
        setTitle("Memo - Time Tracking");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize services
        historyService = new HistoryService();
        storageService = new CsvStorageService("./log");
        editorService = new EntryEditorService(historyService);
        searchService = new SearchService(historyService);
        summaryService = new SummaryService(historyService);
        kanbanService = new KanbanService(historyService);
        timeCalcService = new TimeCalculationService(historyService);
        settingsService = new SettingsService("./settings");
        
        // Load history from storage
        loadHistory();
        
        // Create UI components
        createUI();
    }
    
    private void createUI() {
        setLayout(new BorderLayout());
        
        // Top panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // History panel
        historyPanel = new HistoryPanel(historyService, editorService, kanbanService);
        tabbedPane.addTab("History", historyPanel);
        
        // Search panel
        searchPanel = new SearchPanel(searchService, timeCalcService);
        tabbedPane.addTab("Search", searchPanel);
        
        // Summary panel
        summaryPanel = new SummaryPanel(summaryService, timeCalcService);
        tabbedPane.addTab("Summary", summaryPanel);
        
        // Kanban panel
        kanbanPanel = new KanbanPanel(kanbanService, editorService);
        tabbedPane.addTab("Kanban", kanbanPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar at bottom
        JLabel statusBar = new JLabel("Ready");
        statusBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private void loadHistory() {
        try {
            var entries = storageService.loadAll();
            for (var entry : entries) {
                historyService.add(entry);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error loading history: " + e.getMessage(), 
                    "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public HistoryService getHistoryService() { return historyService; }
    public CsvStorageService getStorageService() { return storageService; }
    public EntryEditorService getEditorService() { return editorService; }
    public SearchService getSearchService() { return searchService; }
    public SummaryService getSummaryService() { return summaryService; }
    public KanbanService getKanbanService() { return kanbanService; }
    public TimeCalculationService getTimeCalcService() { return timeCalcService; }
    public SettingsService getSettingsService() { return settingsService; }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel if system one fails
            }
            
            MemoFrame frame = new MemoFrame();
            frame.setVisible(true);
        });
    }
}