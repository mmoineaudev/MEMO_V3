package com.memo.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ActivityEntry record.
 * Tests all constructors, validation, and utility methods.
 */
class ActivityEntryTest {

    @Test
    void testActivityEntryWithAutoTimestamp() {
        // Arrange
        LocalDateTime beforeCreation = LocalDateTime.now();
        
        // Act
        ActivityEntry entry = new ActivityEntry(
                "Coding", 
                "Fix bug in search module", 
                "TODO", 
                "Need to review query logic", 
                45
        );
        
        LocalDateTime afterCreation = LocalDateTime.now();
        
        // Assert
        assertNotNull(entry);
        assertEquals("Coding", entry.activityType());
        assertEquals("Fix bug in search module", entry.description());
        assertEquals("TODO", entry.status());
        assertEquals("Need to review query logic", entry.comment());
        assertTrue(entry.timeSpent() == 45);
        
        // Timestamp should be between before and after creation
        assertTrue(entry.timestamp().isAfter(beforeCreation) || entry.timestamp().isEqual(beforeCreation));
        assertTrue(entry.timestamp().isBefore(afterCreation) || entry.timestamp().isEqual(afterCreation));
    }

    @Test
    void testActivityEntryWithExplicitTimestamp() {
        // Arrange
        LocalDateTime specificTime = LocalDateTime.of(2026, 3, 31, 14, 30, 0);
        
        // Act
        ActivityEntry entry = new ActivityEntry(
                "Meeting", 
                "Team sync", 
                "DOING", 
                "Discussed sprint progress", 
                specificTime, 
                60
        );
        
        // Assert
        assertNotNull(entry);
        assertEquals(specificTime, entry.timestamp());
    }

    @Test
    void testGetFormattedTimestamp() {
        // Arrange
        LocalDateTime testTime = LocalDateTime.of(2026, 3, 31, 10, 30, 0);
        ActivityEntry entry = new ActivityEntry(
                "Test", 
                "Test entry", 
                "TODO", 
                "", 
                testTime, 
                0
        );
        
        // Act & Assert
        String formatted = entry.getFormattedTimestamp();
        assertNotNull(formatted);
        assertEquals("2026-03-31 10:30:00", formatted);
    }

    @Test
    void testIsValid() {
        // Arrange
        ActivityEntry validEntry = new ActivityEntry(
                "Development", 
                "Implement feature X", 
                "DOING", 
                "This is a comment\nwith multiple lines", 
                120
        );
        
        ActivityEntry invalidEmptyType = new ActivityEntry(
                "", 
                "Description", 
                "TODO", 
                "", 
                0
        );
        
        ActivityEntry invalidNullStatus = new ActivityEntry(
                "Test", 
                "Desc", 
                null, 
                "", 
                0
        );
        
        // Act & Assert
        assertTrue(validEntry.isValid());
        assertFalse(invalidEmptyType.isValid());
        assertFalse(invalidNullStatus.isValid());
    }

    @Test
    void testMultilineComment() {
        // Arrange
        String multilineComment = "First line\nSecond line\nThird line";
        
        // Act
        ActivityEntry entry = new ActivityEntry(
                "Note", 
                "Quick note", 
                "NOTE", 
                multilineComment, 
                0
        );
        
        // Assert
        assertEquals(multilineComment, entry.comment());
    }

    @Test
    void testTimeSpentValidation() {
        // Arrange
        ActivityEntry zeroTime = new ActivityEntry(
                "Test", 
                "Desc", 
                "DONE", 
                "", 
                0
        );
        
        ActivityEntry negativeTime = new ActivityEntry(
                "Test", 
                "Desc", 
                "DONE", 
                "", 
                -10
        );
        
        ActivityEntry positiveTime = new ActivityEntry(
                "Test", 
                "Desc", 
                "DONE", 
                "", 
                100
        );
        
        // Act & Assert
        assertTrue(zeroTime.isValid());
        assertFalse(negativeTime.isValid());
        assertTrue(positiveTime.isValid());
    }

    @Test
    void testAllStatusValues() {
        // Arrange
        String[] statuses = {"TODO", "DOING", "DONE", "NOTE"};
        
        // Act & Assert
        for (String status : statuses) {
            ActivityEntry entry = new ActivityEntry(
                    "Test", 
                    "Desc", 
                    status, 
                    "", 
                    0
            );
            assertEquals(status, entry.status());
            assertTrue(entry.isValid());
        }
    }

    @Test
    void testActivityTypeBlanks() {
        // Arrange & Act & Assert
        assertFalse(new ActivityEntry(
                "   ", 
                "Desc", 
                "TODO", 
                "", 
                0
        ).isValid());
        
        assertTrue(new ActivityEntry(
                "Test", 
                "Desc", 
                "TODO", 
                "", 
                0
        ).isValid());
    }

    @Test
    void testDescriptionBlanks() {
        // Arrange & Act & Assert
        assertFalse(new ActivityEntry(
                "Test", 
                "   ", 
                "TODO", 
                "", 
                0
        ).isValid());
        
        assertFalse(new ActivityEntry(
                "Test", 
                null, 
                "TODO", 
                "", 
                0
        ).isValid());
    }

    @Test
    void testNullFields() {
        // Arrange & Act & Assert - Java records don't throw NPE for null constructor args
        // Instead, the isValid() method should return false
        
        ActivityEntry entry1 = new ActivityEntry(null, "Desc", "TODO", "", 0);
        assertFalse(entry1.isValid());
        
        ActivityEntry entry2 = new ActivityEntry("Type", null, "TODO", "", 0);
        assertFalse(entry2.isValid());
    }

    @Test
    void testDescriptionReuse() {
        // Arrange
        String commonDescription = "Code review";
        
        // Act
        ActivityEntry entry1 = new ActivityEntry(
                "Dev", 
                commonDescription, 
                "DOING", 
                "", 
                30
        );
        
        ActivityEntry entry2 = new ActivityEntry(
                "PM", 
                commonDescription, 
                "TODO", 
                "", 
                60
        );
        
        // Assert
        assertEquals(commonDescription, entry1.description());
        assertEquals(commonDescription, entry2.description());
    }

    @Test
    void testLargeTimeSpent() {
        // Arrange & Act & Assert
        ActivityEntry longEntry = new ActivityEntry(
                "Long task", 
                "Extremely long task description", 
                "DONE", 
                "", 
                1000
        );
        
        assertTrue(longEntry.isValid());
        assertEquals(1000, longEntry.timeSpent());
    }

    @Test
    void testCommentWithSpecialCharacters() {
        // Arrange & Act
        ActivityEntry entry = new ActivityEntry(
                "Test", 
                "Desc", 
                "TODO", 
                "Note: Special chars: @#$%^&*()", 
                15
        );
        
        // Assert
        assertEquals("Note: Special chars: @#$%^&*()", entry.comment());
        assertTrue(entry.isValid());
    }

    @Test
    void testEmptyComment() {
        // Arrange & Act
        ActivityEntry entry = new ActivityEntry(
                "Test", 
                "Desc", 
                "DONE", 
                "", 
                0
        );
        
        // Assert
        assertEquals("", entry.comment());
        assertTrue(entry.isValid());
    }
}