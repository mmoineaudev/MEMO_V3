# MEMO_V3 Implementation Checklist

## Project Overview
Java Swing GUI application for activity tracking with CSV-based storage, implementing the functionality from the legacy ActivityTracker CLI with enhancements.

## Core Components

### 1. Model Layer
- [ ] ActivityEntry record with fields: activityType, description, status, comment, timestamp, timeSpent
- [ ] CSV serialization/deserialization with semicolon separator
- [ ] Semicolon sanitization to prevent CSV parsing issues
- [ ] Multi-line comment support with \n encoding

### 2. Service Layer
- [ ] CsvStorageService - Read/write CSV files to configurable storage directory
- [ ] HistoryService - In-memory history management with CRUD operations
- [ ] EntryEditorService - Create/edit/delete entries with auto-timestamping
- [ ] SearchService - Search entries by description, activity type, status
- [ ] SummaryService - Daily and weekly time summaries
- [ ] KanbanService - Organize entries by status (TODO, DOING, DONE, NOTE)
- [ ] TimeCalculationService - Time aggregation by date/week/activity type

### 3. UI Components
- [ ] Main window layout with resizable panels
- [ ] History panel - Displays all entries in table format
- [ ] Entry editor dialog with text areas for description and comment
- [ ] Search panel with filter inputs
- [ ] Summary panel showing daily/weekly time sums
- [ ] Kanban board view for status-based organization

### 4. Configuration
- [ ] SettingsService - Store storage directory path in properties file
- [ ] Default storage: ./log (create if missing)

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
- [ ] Can create entry with activity type, description, status, comment, time
- [ ] Timestamp auto-populated on creation
- [ ] Can reuse previous descriptions from dropdown
- [ ] Large text areas for long descriptions/comments

### Entry Editing
- [ ] Can edit any field of existing entry
- [ ] Can update status (move between TODO/DOING/DONE)
- [ ] Can delete entry with confirmation

### Search
- [ ] Search by description text
- [ ] Search by activity type
- [ ] Search by status
- [ ] Results show total time for matching entries
- [ ] Search results appear in popup or filtered view

### Summary
- [ ] Daily summary: time per description for selected date
- [ ] Weekly summary: time per description for selected week
- [ ] Summary displays in popup or dedicated panel
- [ ] Can select date/week from calendar or dropdown

### Storage
- [ ] CSV files stored in configurable directory
- [ ] Default: ./log
- [ ] Storage directory created if missing
- [ ] Each day's entries in separate CSV file (named by date)

### UI/UX
- [ ] All panels resizable (JSplitPane)
- [ ] History sorted by date DESC (newest first)
- [ ] Clean, minimal interface
- [ ] No external dependencies beyond Swing

## Testing

### Unit Tests
- [ ] ActivityEntry model tests
- [ ] CsvStorageService tests
- [ ] HistoryService tests
- [ ] EntryEditorService tests
- [ ] SearchService tests
- [ ] SummaryService tests
- [ ] KanbanService tests

### Integration Tests
- [ ] ActivityEntry CSV serialization
- [ ] Multi-line comment handling
- [ ] Semicolon sanitization
- [ ] CSV file read/write
- [ ] Storage directory creation
- [ ] Write and read all entries preserving data
- [ ] Handle multiple CSV files correctly
- [ ] Preserve multi-line comments through round-trip
- [ ] Handle empty comments
- [ ] Handle special characters in comments
- [ ] Complete workflow: create, save, reload, verify
- [ ] Entry update and delete workflow
- [ ] HistoryService loads from multiple files
- [ ] Safety: never write to user home directory
- [ ] Safety: fail safely on unwritable directory
- [ ] Safety: never modify files outside storage

## Clean Code Principles
- [ ] Single responsibility per class
- [ ] Immutable model (record)
- [ ] Service layer separation
- [ ] Dependency injection for services
- [ ] No magic numbers (use constants)
- [ ] Meaningful variable names
- [ ] Minimal coupling between layers

## TDD Workflow
- [ ] Write failing test first
- [ ] Implement minimal code to pass
- [ ] Refactor while maintaining tests
- [ ] All tests pass before commit

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

## Next Steps
- [ ] 1. Implement SettingsService for configuration
- [ ] 2. Create MemoFrame as main window
- [ ] 3. Implement HistoryPanel with resizable table
- [ ] 4. Create EntryEditorDialog with auto-suggest
- [ ] 5. Implement SearchPanel with time sum display
- [ ] 6. Add SummaryPanel for daily/weekly views
- [ ] 7. Add KanbanPanel for status-based view
- [ ] 8. Wire up all components
- [ ] 9. Add auto-suggest for last 10 descriptions
- [ ] 10. Final UI polish and testing
