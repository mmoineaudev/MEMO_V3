# MEMO_V2 - Activity Tracker GUI

## Overall Goal
Build a Swing-based GUI application for activity tracking with CSV storage, replacing the legacy CLI implementation.

## Main Architectural and Technical Guidelines
- Maven project with minimal dependencies (Swing only, no external libraries)
- Clean Code principles: small methods, meaningful names, single responsibility
- Strict TDD: write tests first, green/red/refactor cycle
- Resizable components using layout managers (GridBagLayout or SpringLayout)
- CSV storage in configurable directory (default: ./log/)
- Editable entries with persistent disk saves
- Color-coded status display (TODO=yellow, DOING=blue, DONE=green, NOTE=gray)
- Follow existing data model from legacy ActivityTracker.java
- Commits after each change with descriptive messages

## Scope Definition
- GUI with resizable panels for entry form, history view, and search
- Activity entry with large text areas and history reuse (last 10 distinct descriptions)
- Search functionality across all CSV columns
- Daily and weekly time sums per activity description
- CSV storage in configurable directory (default: ./log/)
- Editable entries with persistent saves
- Color-coded status display (TODO=yellow, DOING=blue, DONE=green, NOTE=gray)
- Kanban view: grouped by description, shows non-DONE lines sorted by timestamp
- History always visible (no pagination, performance not a concern)

## Cross-Reference Matrix
| User Feature | Use Cases |
|--------------|-----------|
| New entry edition | UC-001, UC-002 |
| History reuse | UC-001 |
| Editable entries | UC-012, UC-013 |
| Search functionality | UC-003, UC-004 |
| Time sums (daily/weekly) | UC-005, UC-006, UC-007 |
| Resizable components | UC-008 |
| CSV storage config | UC-009, UC-010 |
| Display all history | UC-011 |
| Color-coded status | UC-014 |
| Kanban view | UC-015, UC-016 |

---

## Use Cases

### UC-001: Create New Activity Entry
`USE_CASES/UC-001.md`
* [ ] implementation
* [ ] test

### UC-002: Display History of All Entries
`USE_CASES/UC-002.md`
* [ ] implementation
* [ ] test

### UC-003: Search Activity Entries
`USE_CASES/UC-003.md`
* [ ] implementation
* [ ] test

### UC-004: Display Search Results with Time Sum
`USE_CASES/UC-004.md`
* [ ] implementation
* [ ] test

### UC-005: Calculate Daily Time Summary
`USE_CASES/UC-005.md`
* [ ] implementation
* [ ] test

### UC-006: Calculate Weekly Time Summary
`USE_CASES/UC-006.md`
* [ ] implementation
* [ ] test

### UC-007: Display Weekly Summary Popup
`USE_CASES/UC-007.md`
* [ ] implementation
* [ ] test

### UC-008: Resize Application Components
`USE_CASES/UC-008.md`
* [ ] implementation
* [ ] test

### UC-009: Configure Storage Directory
`USE_CASES/UC-009.md`
* [ ] implementation
* [ ] test

### UC-010: Auto-create Storage Directory
`USE_CASES/UC-010.md`
* [ ] implementation
* [ ] test

### UC-011: Load All History on Startup
`USE_CASES/UC-011.md`
* [ ] implementation
* [ ] test

### UC-012: Edit Activity Entry
`USE_CASES/UC-012.md`
* [ ] implementation
* [ ] test

### UC-013: Persist Entry Edits to Disk
`USE_CASES/UC-013.md`
* [ ] implementation
* [ ] test

### UC-014: Display Status with Color Coding
`USE_CASES/UC-014.md`
* [ ] implementation
* [ ] test

### UC-015: Display Kanban View
`USE_CASES/UC-015.md`
* [ ] implementation
* [ ] test

### UC-016: Interact with Kanban Entries
`USE_CASES/UC-016.md`
* [ ] implementation
* [ ] test

---

## Implementation Notes for Agents

### Project Structure
```
memo-v2/
├── pom.xml
├── checklist.md
├── USE_CASES/
│   ├── UC-001.md
│   ├── UC-002.md
│   ├── ...
│   └── UC-016.md
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── memo/
│       │           ├── MemoApplication.java
│       │           ├── model/
│       │           │   └── ActivityEntry.java
│       │           ├── service/
│       │           │   ├── CsvStorageService.java
│       │           │   ├── TimeCalculationService.java
│       │           │   ├── EntryEditorService.java
│       │           │   └── KanbanDataService.java
│       │           └── view/
│       │               ├── MainFrame.java
│       │               ├── EntryPanel.java
│       │               ├── HistoryPanel.java
│       │               ├── SearchPanel.java
│       │               ├── KanbanPanel.java
│       │               └── components/
│       │                   ├── ColoredStatusLabel.java
│       │                   ├── EditableEntryCell.java
│       │                   └── KanbanCard.java
│       └── resources/
│           └── config.properties
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── memo/
│                   ├── model/
│                   │   └── ActivityEntryTest.java
│                   ├── service/
│                   │   ├── CsvStorageServiceTest.java
│                   │   ├── TimeCalculationServiceTest.java
│                   │   ├── EntryEditorServiceTest.java
│                   │   └── KanbanDataServiceTest.java
│                   └── view/
│                       ├── MainFrameTest.java
│                       └── KanbanPanelTest.java
└── log/ (storage directory, auto-created)
```

### Data Model (from legacy)
CSV Format: `PROJECT;ACTIVITY_TYPE;DESCRIPTION;STATUS;COMMENT;TIMESTAMP;TIME_SPENT`

Example: `CAPGEMINI;DEV;Code review JIRA-1234;TODO;;25/03/2026 09:30;0.0`

### Status Color Mapping
- TODO: `new JLabel("<html><font color='#FFD700'>TODO</font></html>")` (Gold/Yellow)
- DOING: `new JLabel("<html><font color='#1E90FF'>DOING</font></html>")` (Blue)
- DONE: `new JLabel("<html><font color='#32CD32'>DONE</font></html>")` (Green)
- NOTE: `new JLabel("<html><font color='#808080'>NOTE</font></html>")` (Gray)

### Key Classes to Implement
1. **ActivityEntry** - Record/class for activity data (immutable)
2. **CsvStorageService** - Read/write CSV files, directory management
3. **TimeCalculationService** - Daily/weekly time sums, grouping
4. **EntryEditorService** - Handle entry modifications, atomic file writes
5. **KanbanDataService** - Filter non-DONE entries, group by description, sort by timestamp
6. **MainFrame** - Main window with resizable panels and tabbed view
7. **EntryPanel** - New entry form with history reuse
8. **HistoryPanel** - Scrollable list of all entries
9. **SearchPanel** - Search dialog with filters
10. **KanbanPanel** - Kanban board with columns per description
11. **ColoredStatusLabel** - Label component with dynamic color based on status
12. **EditableEntryCell** - Swing cell editor for inline editing
13. **KanbanCard** - Individual entry card component for Kanban view
14. **MemoApplication** - Application entry point

### Maven Dependencies (Minimal)
- Only Java standard library (no external deps)
- Use JUnit 5 for testing (optional, can use system junit)

### Layout Recommendations
- Use GridBagLayout for main frame with resizable constraints
- Use JSplitPane for vertical/horizontal dividers
- Use JScrollPane for all content panels
- Set minimum sizes for readability
- Use JTable with custom renderer for colored status display

### Editable Entries Implementation
- Option A: JTable with DefaultCellEditor for inline editing
- Option B: Dialog-based editing for complex entries
- Option C: Double-click opens edit panel in place
- Atomic file writes: write to temp file, then rename for safety

### Kanban View Implementation
- MainFrame uses JTabbedPane: History | Search | Kanban
- KanbanDataService groups entries by description (excluding DONE status)
- Each column shows unique description with entry count badge
- Entries within column sorted by timestamp (oldest → newest)
- KanbanCard: JPanel with timestamp, status, truncated description
- Click on card: shows full details, edit, or mark-as-done actions
- Mark as DONE: removes from Kanban immediately on next render
- Grid layout: 2 columns by default, expandable with JScrollPane

### View Switching
- Tab-based navigation between History, Search, and Kanban views
- Kanban view recalculates on entry save or status change
- Cached Kanban data with invalidate-on-change pattern

### TDD Approach
1. Start with model tests (ActivityEntry parsing)
2. Service tests (CSV I/O, time calculations)
3. Editor service tests (edit, validate, atomic save)
4. UI tests (swing tester or headless tests)
5. Integration tests (full workflow)

### Commit Pattern
- Commit after each use case complete
- Message format: `feat: UC-XXX <description>` or `test: UC-XXX <description>`
- Push after each commit
