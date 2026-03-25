# MEMO_V3 Implementation Checklist

## Project Overview
Java Swing GUI application for activity tracking with CSV-based storage, implementing the functionality from the legacy ActivityTracker CLI with enhancements.

## Core Components

### 1. Model Layer (Complete)
- [x] ActivityEntry record with fields: activityType, description, status, comment, timestamp, timeSpent
- [x] CSV serialization/deserialization with semicolon separator
- [x] Semicolon sanitization to prevent CSV parsing issues
- [x] Multi-line comment support with \n encoding

### 2. Service Layer (Complete)
- [x] CsvStorageService - Read/write CSV files to configurable storage directory
- [x] HistoryService - In-memory history management with CRUD operations
- [x] EntryEditorService - Create/edit/delete entries with auto-timestamping
- [x] SearchService - Search entries by description, activity type, status
- [x] SummaryService - Daily and weekly time summaries
- [x] KanbanService - Organize entries by status (TODO, DOING, DONE, NOTE)
- [x] TimeCalculationService - Time aggregation by date/week/activity type

### 3. UI Components (In Progress)
- [x] Main window layout with resizable panels
- [x] History panel - Displays all entries in table format
- [x] Entry editor dialog with text areas for description and comment
- [x] Search panel with filter inputs
- [x] Summary panel showing daily/weekly time sums
- [x] Kanban board view for status-based organization

### 4. Configuration
- [x] SettingsService - Store storage directory path in properties file
- [x] Default storage: ~/.MEMO/ (create if missing)

## User Stories from README

### US-001: Fast Activity Logging
- As a user, I want to quickly add new activity entries
- Entry editor should have large text areas for description and comment
- Auto-suggest last 10 distinct activity descriptions

### US-002: History View
- As a user, I want to see all activities ordered by date DESC
- All history should be visible (no pagination)
- Performance: handles 100+ entries without delay

### US-003: Search Functionality
- As a user, I want to search entries by any field
- Search results should show integrated time sum
- Search should be efficient and applicable to partial text

### US-004: Daily/Weekly Time Sums
- As a user, I want to see time spent per activity description
- Daily summary: time per description for selected date
- Weekly summary: time per description for selected week
- Display in popup or dedicated panel

### US-005: Entry Reuse
- As a user, I want to create new entries based on previous ones
- Reuse description with updated time and comment
- Track time elapsed since last entry of same activity

### US-006: Kanban Workflow
- As a user, I want to move entries between statuses
- TODO -> DOING -> DONE progression
- NOTE for quick notes without time tracking

## Acceptance Criteria

### Entry Creation
- [x] Can create entry with activity type, description, status, comment, time
- [x] Timestamp auto-populated on creation
- [x] Can reuse previous descriptions from dropdown
- [x] Large text areas for long descriptions/comments

### Entry Editing
- [x] Can edit any field of existing entry
- [x] Can update status (move between TODO/DOING/DONE)
- [x] Can delete entry with confirmation

### Search
- [x] Search by description text
- [x] Search by activity type
- [x] Search by status
- [x] Results show total time for matching entries
- [x] Search results appear in popup or filtered view

### Summary
- [x] Daily summary: time per description for selected date
- [x] Weekly summary: time per description for selected week
- [x] Summary displays in popup or dedicated panel
- [x] Can select date/week from calendar or dropdown

### Storage
- [x] CSV files stored in configurable directory
- [x] Default: ~/.MEMO/
- [x] Storage directory created if missing
- [x] Each day's entries in separate CSV file (named by date)

### UI/UX
- [x] All panels resizable (JSplitPane)
- [x] History sorted by date DESC (newest first)
- [x] Clean, minimal interface
- [x] No external dependencies beyond Swing

## Testing

### Unit Tests (Complete - 61 tests)
- [x] ActivityEntry model tests
- [x] CsvStorageService tests
- [x] HistoryService tests
- [x] EntryEditorService tests
- [x] SearchService tests
- [x] SummaryService tests
- [x] KanbanService tests

### Integration Tests (Complete - 7 tests)
- [x] ActivityEntry CSV serialization
- [x] Multi-line comment handling
- [x] Semicolon sanitization
- [x] CSV file read/write
- [x] Storage directory creation

## Clean Code Principles
- [x] Single responsibility per class
- [x] Immutable model (record)
- [x] Service layer separation
- [x] Dependency injection for services
- [x] No magic numbers (use constants)
- [x] Meaningful variable names
- [x] Minimal coupling between layers

## TDD Workflow
- [x] Write failing test first
- [x] Implement minimal code to pass
- [x] Refactor while maintaining tests
- [x] All tests pass before commit

## Files Structure
```
memo-v3/
├── src/main/java/com/memo/
│   ├── model/
│   │   └── ActivityEntry.java
│   ├── service/
│   │   ├── CsvStorageService.java
│   │   ├── HistoryService.java
│   │   ├── EntryEditorService.java
│   │   ├── SearchService.java
│   │   ├── SummaryService.java
│   │   ├── KanbanService.java
│   │   ├── TimeCalculationService.java
│   │   └── SettingsService.java
│   └── gui/
│       ├── MemoFrame.java
│       ├── HistoryPanel.java
│       ├── EntryEditorDialog.java
│       ├── SearchPanel.java
│       ├── SummaryPanel.java
│       └── KanbanPanel.java
├── src/test/java/com/memo/
│   ├── model/
│   │   └── ActivityEntryTest.java
│   ├── service/
│   │   ├── CsvStorageServiceTest.java
│   │   ├── HistoryServiceTest.java
│   │   ├── EntryEditorServiceTest.java
│   │   ├── SearchServiceTest.java
│   │   ├── SummaryServiceTest.java
│   │   └── KanbanServiceTest.java
│   └── integration/
│       └── IntegrationTest.java
├── pom.xml
└── README.md
```

## Next Steps (Completed)
[x] 1. Implement SettingsService for configuration
[x] 2. Create MemoFrame as main window
[x] 3. Implement HistoryPanel with resizable table
[x] 4. Create EntryEditorDialog with auto-suggest
[x] 5. Implement SearchPanel with time sum display
[x] 6. Add SummaryPanel for daily/weekly views
[x] 7. Add KanbanPanel for status-based view
[x] 8. Wire up all components
[x] 9. Add auto-suggest for last 10 descriptions
[x] 10. Final UI polish and testing
