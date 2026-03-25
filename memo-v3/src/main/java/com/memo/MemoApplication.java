package com.memo;

import com.memo.service.*;
import com.memo.view.MainFrame;

import javax.swing.*;

/**
 * Main application entry point.
 */
public class MemoApplication {
    
    /**
     * Application main method.
     */
    public static void main(String[] args) {
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.out.println("Could not set look and feel: " + e.getMessage());
            }
            
            // Initialize settings service
            SettingsService settingsService = new SettingsService();
            
            // Initialize storage with configured directory
            String storageDir = settingsService.getStorageDirectory();
            CsvStorageService csvStorage = new CsvStorageService(storageDir);
            
            // Ensure storage directory exists (UC-010)
            if (!csvStorage.ensureDirectoryExists()) {
                showError("Failed to create storage directory. Please check permissions.");
                return;
            }
            
            HistoryService historyService = new HistoryService(csvStorage);
            EntryEditorService entryEditorService = new EntryEditorService(
                    historyService, 
                    "activity_" + java.time.LocalDate.now() + ".csv"
            );
            
            // Load all history on startup (UC-011)
            historyService.loadAllHistory();
            
            // Create and show main frame
            MainFrame mainFrame = new MainFrame(entryEditorService, historyService, settingsService);
            
            // Reload history after loading from disk
            mainFrame.refreshHistory();
        });
    }
    
    /**
     * Show error dialog.
     */
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
