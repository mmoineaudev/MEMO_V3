package com.memo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActivityEntryTest {
    
    @Test
    void shouldParseValidCsvLineWithAllFields() {
        String line = "DEV;Test description;TODO;Comment text;25/03/2026 09:30;0.5";
        
        ActivityEntry entry = ActivityEntry.fromCsv(line);
        
        assertNotNull(entry);
        assertEquals("DEV", entry.activityType());
        assertEquals("Test description", entry.description());
        assertEquals("TODO", entry.status());
        assertEquals("Comment text", entry.comment());
        assertEquals("25/03/2026 09:30", entry.timestamp());
        assertEquals(0.5, entry.timeSpent());
    }
    
    @Test
    void shouldParseCsvLineWithCeremonyActivity() {
        String line = "CEREMONY;Meeting;DOING;;25/03/2026 10:00;1.0";
        
        ActivityEntry entry = ActivityEntry.fromCsv(line);
        
        assertNotNull(entry);
        assertEquals("CEREMONY", entry.activityType());
        assertEquals("Meeting", entry.description());
        assertEquals("DOING", entry.status());
        assertEquals("", entry.comment());
        assertEquals(1.0, entry.timeSpent());
    }
    
    @Test
    void shouldParseCsvLineWithDoneStatus() {
        String line = "SUPPORT;Help user;DONE;;25/03/2026 11:00;0.25";
        
        ActivityEntry entry = ActivityEntry.fromCsv(line);
        
        assertNotNull(entry);
        assertEquals("SUPPORT", entry.activityType());
        assertEquals("Help user", entry.description());
        assertEquals("DONE", entry.status());
        assertEquals(0.25, entry.timeSpent());
    }
    
    @Test
    void shouldParseCsvLineWithNoteStatusAndNoTime() {
        String line = "LEARNING;Read docs;NOTE;Important;;0.0";
        
        ActivityEntry entry = ActivityEntry.fromCsv(line);
        
        assertNotNull(entry);
        assertEquals("LEARNING", entry.activityType());
        assertEquals("Read docs", entry.description());
        assertEquals("NOTE", entry.status());
        assertEquals("Important", entry.comment());
        assertEquals(0.0, entry.timeSpent());
    }
    
    @Test
    void shouldReturnNullForBlankLine() {
        assertNull(ActivityEntry.fromCsv(""));
        assertNull(ActivityEntry.fromCsv("   "));
        assertNull(ActivityEntry.fromCsv(null));
    }
    
    @Test
    void shouldHandleEmptyFields() {
        String line = ";;;;;";
        ActivityEntry entry = ActivityEntry.fromCsv(line);
        
        assertNotNull(entry);
        assertTrue(entry.activityType().isEmpty());
        assertTrue(entry.description().isEmpty());
        assertTrue(entry.status().isEmpty());
        assertTrue(entry.comment().isEmpty());
        assertTrue(entry.timestamp().isEmpty());
        assertEquals(0.0, entry.timeSpent());
    }
    
    @Test
    void shouldConvertToCsv() {
        ActivityEntry entry = ActivityEntry.createNew("DEV", "Test", "TODO", "Comment", 0.5);
        
        String csv = entry.toCsv();
        
        assertNotNull(csv);
        assertTrue(csv.contains("DEV"));
        assertTrue(csv.contains("Test"));
        assertTrue(csv.contains("TODO"));
        assertTrue(csv.contains("0.5"));
    }
    
    @Test
    void shouldCreateNewEntryWithCurrentTimestamp() {
        ActivityEntry entry = ActivityEntry.createNew("DEV", "Test", "TODO", "Comment", null);
        
        assertNotNull(entry.timestamp());
        assertFalse(entry.timestamp().isBlank());
        assertEquals(0.0, entry.timeSpent());
    }
    
    @Test
    void shouldCreateCopyWithUpdatedFields() {
        ActivityEntry original = ActivityEntry.createNew("DEV", "Test", "TODO", "Comment", 0.5);
        ActivityEntry updated = original.withUpdated("DEV", "Updated", "DOING", null, 1.0);
        
        assertEquals("DEV", updated.activityType());
        assertEquals("Updated", updated.description());
        assertEquals("DOING", updated.status());
        assertEquals("Comment", updated.comment());
        assertEquals(1.0, updated.timeSpent());
    }
    
    @Test
    void shouldEqualSameTimestampAndType() {
        ActivityEntry e1 = ActivityEntry.createNew("DEV", "Test", "TODO", "Comment", 0.5);
        ActivityEntry e2 = ActivityEntry.createNew("DEV", "Test", "TODO", "Comment", 0.5);
        ActivityEntry e3 = ActivityEntry.createNew("DEV", "Other", "TODO", "Comment", 0.5);
        
        assertEquals(e1, e2);
        assertNotEquals(e1, e3);
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}
