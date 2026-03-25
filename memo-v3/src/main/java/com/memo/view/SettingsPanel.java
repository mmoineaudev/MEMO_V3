package com.memo.view;

import com.memo.service.SettingsService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.*;

/**
 * Panel for configuring application settings.
 * UC-009: Configure storage directory
 */
public class SettingsPanel extends JPanel {
    
    private final SettingsService settingsService;
    
    private JTextField storageDirField;
    private JButton browseButton;
    private JButton resetButton;
    private JButton applyButton;
    
    public SettingsPanel(SettingsService settingsService) {
        this.settingsService = settingsService;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Storage directory label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        add(new JLabel("Storage Directory:"), gbc);
        
        // Storage directory field
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        storageDirField = new JTextField(30);
        storageDirField.setText(settingsService.getStorageDirectory());
        add(storageDirField, gbc);
        
        // Browse button
        gbc.gridx = 2;
        gbc.weightx = 0;
        browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> browseDirectory());
        add(browseButton, gbc);
        
        // Reset button
        gbc.gridx = 3;
        resetButton = new JButton("Reset to Default");
        resetButton.setEnabled(settingsService.isCustomStorageConfigured());
        resetButton.addActionListener(e -> resetToDefault());
        add(resetButton, gbc);
        
        // Apply button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        applyButton = new JButton("Apply Settings");
        applyButton.addActionListener(e -> applySettings());
        add(applyButton, gbc);
        
        // Info label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        JLabel infoLabel = new JLabel(
                "All CSV files will be stored in the directory above. " +
                "This setting is persisted across application restarts."
        );
        infoLabel.setFont(infoLabel.getFont().deriveFont(10f));
        infoLabel.setForeground(Color.GRAY);
        add(infoLabel, gbc);
    }
    
    private void browseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select Storage Directory");
        
        // Start in current directory
        String currentDir = storageDirField.getText();
        if (Files.exists(Paths.get(currentDir))) {
            chooser.setCurrentDirectory(new File(currentDir));
        }
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            storageDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void resetToDefault() {
        String defaultDir = System.getProperty("user.home") + File.separator + ".MEMO";
        storageDirField.setText(defaultDir);
        settingsService.resetToDefault();
        resetButton.setEnabled(false);
    }
    
    /**
     * Apply settings.
     */
    public void applySettings() {
        settingsService.setStorageDirectory(storageDirField.getText());
        JOptionPane.showMessageDialog(this,
                "Settings saved successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
