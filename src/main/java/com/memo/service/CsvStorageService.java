package com.memo.service;

import com.memo.model.ActivityEntry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for persisting ActivityEntries to CSV files.
 */
public class CsvStorageService {
    
    private static final String DEFAULT_STORAGE_DIR = "./log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final String storageDir;
    
    public CsvStorageService() {
        this(DEFAULT_STORAGE_DIR);
    }
    
    public CsvStorageService(String dirPath) {
        this.storageDir = dirPath;
    }
    
    public void save(ActivityEntry entry) throws IOException {
        Path dirPath = Paths.get(storageDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
        
        LocalDate date = entry.timestamp().toLocalDate();
        String dateStr = date.format(DATE_FORMATTER);
        Path filePath = Paths.get(storageDir, dateStr + ".csv");
        
        // Add header if file doesn't exist
        List<String> lines = new ArrayList<>();
        if (Files.exists(filePath)) {
            try (Stream<String> s = Files.lines(filePath)) {
                lines = s.collect(Collectors.toList());
            }
        } else {
            // Write header line
            lines.add("description;activityType;comment;status;timestamp;timeSpent");
        }
        
        String csvLine = convertEntryToCsv(entry);
        lines.add(csvLine);
        
        Files.write(filePath, lines);
    }
    
    public List<ActivityEntry> loadAll() throws IOException {
        Path dirPath = Paths.get(storageDir);
        
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            return new ArrayList<>();
        }
        
        try (Stream<Path> paths = Files.walk(dirPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".csv"))
                    .flatMap(path -> readCsvFile(path).stream())
                    .sorted(Comparator.comparing(ActivityEntry::timestamp).reversed())
                    .collect(Collectors.toList());
        }
    }
    
     private String convertEntryToCsv(ActivityEntry entry) {
        StringBuilder sb = new StringBuilder();
        
        // All fields that might contain semicolons must be quoted
        // Also encode newlines and carriage returns to avoid breaking CSV line parsing
        String description = encodeNewlines(entry.description());
        String activityType = encodeNewlines(entry.activityType());
        String comment = encodeNewlines(entry.comment());
        String status = encodeNewlines(entry.status());
        String timestamp = encodeNewlines(DATE_FORMATTER.format(entry.timestamp()));
        String timeSpent = encodeNewlines(String.valueOf(entry.timeSpent()));
        
        sb.append('"').append(escapeQuote(description)).append('"')
          .append(';')
          .append('"').append(escapeQuote(activityType)).append('"')
          .append(';')
          .append('"').append(escapeQuote(comment)).append('"')
          .append(';')
          .append('"').append(escapeQuote(status)).append('"')
          .append(';')
          .append('"').append(escapeQuote(timestamp)).append('"')
          .append(';')
          .append('"').append(escapeQuote(timeSpent)).append('"');
        
        return sb.toString();
    }
    
    /**
     * Encodes newlines and carriage returns to avoid breaking CSV line parsing.
     * \n becomes \\n, \r becomes \\r
     */
    private String encodeNewlines(String value) {
        if (value == null) return "";
        return value.replace("\n", "\\n").replace("\r", "\\r");
    }
    
    /**
     * Decodes escaped newlines and carriage returns.
     */
    private String decodeNewlines(String value) {
        if (value == null) return "";
        return value.replace("\\n", "\n").replace("\\r", "\r");
    }
    
    private String escapeQuote(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
    
    private List<ActivityEntry> readCsvFile(Path filePath) {
        List<ActivityEntry> entries = new ArrayList<>();
        
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.skip(1).filter(line -> !line.trim().isEmpty()).forEach(line -> {
                String[] fields = parseCsvLine(line);
                if (fields.length >= 6) {
                    try {
                        String description = unescapeQuote(fields[0]);
                        String activityType = unescapeQuote(fields[1]);
                        String comment = unescapeQuote(fields[2]);
                        String status = unescapeQuote(fields[3]);
                        String timestampStr = unescapeQuote(fields[4]);
                        int timeSpent = Integer.parseInt(unescapeQuote(fields[5]).trim());
                        
                        // Decode newlines that were encoded during write
                        description = decodeNewlines(description);
                        activityType = decodeNewlines(activityType);
                        comment = decodeNewlines(comment);
                        status = decodeNewlines(status);
                        timestampStr = decodeNewlines(timestampStr);
                        
                        LocalDate date = timestampStr.isEmpty() 
                            ? LocalDate.now() 
                            : LocalDate.parse(timestampStr, DATE_FORMATTER);
                        
                        LocalDateTime timestamp = date.atStartOfDay();
                        entries.add(new ActivityEntry(activityType, description, status, comment, timestamp, timeSpent));
                    } catch (Exception e) {
                        // Skip malformed lines
                    }
                }
            });
        } catch (IOException e) {
            // Silently skip unreadable files
        }
        
        return entries;
    }
    
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                if (inQuotes && !current.isEmpty() && current.charAt(current.length()-1) == '"') {
                    // Escaped quote - keep one and stay in quotes
                    current.append('"');
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ';' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
    
    private String unescapeQuote(String value) {
        if (value == null || value.isEmpty()) return "";
        
        // Handle quoted values by stripping outer quotes
        if ((value.startsWith("\"") && value.endsWith("\"")) || 
            (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        
        // Return as-is for non-quoted values
        return value;
    }
    
    /**
     * Checks if the storage directory exists.
     * 
     * @return true if the storage directory exists and is a directory
     */
    public boolean storageDirectoryExists() {
        Path dirPath = Paths.get(storageDir);
        return Files.exists(dirPath) && Files.isDirectory(dirPath);
    }
    
    /**
     * Creates the storage directory if it doesn't exist.
     * 
     * @throws IOException if the directory cannot be created
     */
    public void createStorageDirectory() throws IOException {
        Path dirPath = Paths.get(storageDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }
    
    /**
     * Saves all entries to CSV files, organized by date.
     * 
     * @param entries List of entries to save
     * @throws IOException if writing fails
     */
    public void saveAll(List<ActivityEntry> entries) throws IOException {
        // Ensure storage directory exists
        if (!storageDirectoryExists()) {
            createStorageDirectory();
        }
        
        // Save each entry
        for (ActivityEntry entry : entries) {
            try {
                save(entry);
            } catch (IOException e) {
                throw new IOException("Failed to save entry: " + entry.description(), e);
            }
        }
    }
}