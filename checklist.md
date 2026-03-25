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

# Use Case: UC-001 Create New Activity Entry

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User logs new activity with description, status, comment, and optional time
* Scope: Entry form with reusable history
* Level: Functional requirement
* Preconditions: Application running, storage directory exists
* Success End Condition: Entry written to CSV file
* Failed End Condition: Invalid input rejected, no file modification
* Primary Actor: User
* Trigger: User clicks "New Entry" button

### MAIN SUCCESS SCENARIO

1. User initiates new entry
2. Form displays with pre-populated description from last 10 distinct entries
3. User fills activity type, description, status, comment
4. User optionally enters time spent (or leaves as 0)
5. User confirms entry
6. Entry appended to current day's CSV file
7. History view refreshes to show new entry

### EXTENSIONS

1a <step 2> <No previous entries exist> : Start with empty description field

2a <step 2> <User selects from history> : Auto-populate description with selected history item

4a <step 4> <Invalid time format> : Show error, require valid format or empty

5a <step 5> <User cancels> : Discard entry, return to history view

### SUB-VARIATIONS

1 <list of sub-variations> : Can be triggered from toolbar button, menu, or keyboard shortcut (Ctrl+N)

4 <list of sub-variations> : Time can be entered as decimal (0.5) or left blank for "just started"

### RELATED INFORMATION (optional)

* Priority: Critical
* Performance Target: <100ms for save operation
* Frequency: Multiple times per day

---

# Use Case: UC-002 Display History of All Entries

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User views all logged activities across all CSV files
* Scope: Scrollable history panel showing all entries
* Level: Functional requirement
* Preconditions: CSV files exist in storage directory
* Success End Condition: All entries displayed with timestamps, timespans, and calculated totals
* Failed End Condition: Empty state shown if no entries exist
* Primary Actor: User
* Trigger: Application startup or manual refresh

### MAIN SUCCESS SCENARIO

1. Application loads or user requests refresh
2. Storage directory scanned for CSV files matching project pattern
3. All entries read from all CSV files
4. Entries displayed in chronological order (oldest first)
5. Daily total time calculated and displayed
6. Per-activity time summary displayed

### EXTENSIONS

1a <step 2> <No CSV files found> : Display empty state message

3a <step 3> <File read error> : Log error, skip problematic file, continue with others

### SUB-VARIATIONS

4 <list of sub-variations> : Can sort by date, activity type, or time spent

### RELATED INFORMATION (optional)

* Priority: Critical
* Performance Target: <1s to load 100 files
* Frequency: On startup, manual refresh

---

# Use Case: UC-003 Search Activity Entries

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User searches across all CSV columns for specific entries
* Scope: Search dialog with multiple field filters
* Level: Functional requirement
* Preconditions: History loaded
* Success End Condition: Matching entries displayed with time sum
* Failed End Condition: No results shown if no matches
* Primary Actor: User
* Trigger: User opens search dialog and enters criteria

### MAIN SUCCESS SCENARIO

1. User opens search dialog
2. User enters search criteria (activity type, description, status, comment, date range)
3. Search executed across all CSV entries
4. Matching entries displayed in result panel
5. Time sum calculated for filtered results
6. User can select result to view full entry details

### EXTENSIONS

2a <step 2> <No criteria entered> : Show all entries (equivalent to no filter)

3a <step 3> <Search error> : Show error message, keep previous results

### SUB-VARIATIONS

1 <list of sub-variations> : Can be triggered from toolbar, menu, or keyboard shortcut (Ctrl+F)

2 <list of sub-variations> : Supports partial matching, case-insensitive search

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: <500ms for search execution
* Frequency: Several times per week

---

# Use Case: UC-004 Display Search Results with Time Sum

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User views filtered results with calculated time totals
* Scope: Result panel with summary statistics
* Level: Functional requirement
* Preconditions: Search executed with results
* Success End Condition: Results displayed with individual and total time
* Failed End Condition: Empty state if no matches
* Primary Actor: User
* Trigger: Search completion

### MAIN SUCCESS SCENARIO

1. Search results displayed in scrollable list
2. Each entry shows: timestamp, activity type, description, status, comment
3. Individual time spent shown per entry
4. Total time sum displayed at bottom of results
5. Results can be sorted by any column

### EXTENSIONS

1a <step 1> <No search results> : Show "No matches found" message

### SUB-VARIATIONS

4 <list of sub-variations> : Can group by date, activity type, or show as flat list

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: Display <100ms after search
* Frequency: After each search

---

# Use Case: UC-005 Calculate Daily Time Summary

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User views total time spent per activity type for current day
* Scope: Summary panel with per-activity breakdown
* Level: Functional requirement
* Preconditions: Entries loaded from CSV files
* Success End Condition: Time summary displayed with daily total
* Failed End Condition: Summary not shown if no entries with time data
* Primary Actor: User
* Trigger: Display of history view

### MAIN SUCCESS SCENARIO

1. All entries read from CSV files
2. Entries grouped by activity type
3. Time spent summed per activity type
4. Daily total calculated (sum of all entries)
5. Summary displayed with activity type, days, hours, minutes
6. Color-coded for readability

### EXTENSIONS

2a <step 2> <No activity types> : Show empty summary

### SUB-VARIATIONS

5 <list of sub-variations> : Can show as list, table, or visual bars

### RELATED INFORMATION (optional)

* Priority: Critical
* Performance Target: <100ms calculation
* Frequency: On each history display

---

# Use Case: UC-006 Calculate Weekly Time Summary

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User views total time spent per activity type for current week
* Scope: Weekly summary dialog/panel
* Level: Functional requirement
* Preconditions: Entries loaded from CSV files
* Success End Condition: Weekly summary displayed with per-activity breakdown
* Failed End Condition: Summary not shown if no entries in week
* Primary Actor: User
* Trigger: User requests weekly summary

### MAIN SUCCESS SCENARIO

1. User opens weekly summary dialog
2. Current week range calculated (Monday to Sunday)
3. Entries filtered by week range
4. Entries grouped by activity type
5. Time spent summed per activity type
6. Summary displayed with activity type, days, hours

### EXTENSIONS

2a <step 2> <Custom week range> : User can select different week range

3a <step 3> <No entries in week> : Show message "No entries in selected week"

### SUB-VARIATIONS

6 <list of sub-variations> : Can show rolling 7 days, or fixed Monday-Sunday

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: <500ms calculation
* Frequency: Weekly review

---

# Use Case: UC-007 Display Weekly Summary Popup

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User views weekly summary in a popup dialog
* Scope: Modal or non-modal dialog with summary data
* Level: Functional requirement
* Preconditions: Weekly calculation complete
* Success End Condition: Summary visible in popup with all required information
* Failed End Condition: Error shown if calculation fails
* Primary Actor: User
* Trigger: User clicks weekly summary button/menu

### MAIN SUCCESS SCENARIO

1. User triggers weekly summary action
2. Weekly calculation executed (if not cached)
3. Popup dialog opens with summary data
4. Summary shows per-activity breakdown
5. Total weekly time displayed
6. User can close popup or export data

### EXTENSIONS

3a <step 3> <Large dataset> : Show loading indicator during calculation

6a <step 6> <Export requested> : Save summary to text file

### SUB-VARIATIONS

1 <list of sub-variations> : Can be toolbar button, menu item, or keyboard shortcut (Ctrl+W)

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: Popup opens <200ms
* Frequency: Weekly

---

# Use Case: UC-008 Resize Application Components

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User can resize all panels and components dynamically
* Scope: Layout management for resizable UI
* Level: Technical requirement
* Preconditions: Application window open
* Success End Condition: Components resize and reflow properly
* Failed End Condition: Components overlap or become unusable
* Primary Actor: User
* Trigger: User drags window border or panel divider

### MAIN SUCCESS SCENARIO

1. User resizes main application window
2. All panels resize proportionally or according to layout constraints
3. Component text areas expand/collapse with available space
4. Scrollbars appear when content exceeds visible area
5. Layout maintains readability and usability

### EXTENSIONS

1a <step 1> <Panel divider resize> : User drags divider between panels

2a <step 2> <Minimum size enforced> : Components don't shrink below readable size

### SUB-VARIATIONS

1 <list of sub-variations> : Can resize vertically, horizontally, or both

### RELATED INFORMATION (optional)

* Priority: Medium
* Performance Target: Resize response <50ms
* Frequency: As needed by user

---

# Use Case: UC-009 Configure Storage Directory

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User can configure and change CSV storage directory
* Scope: Configuration dialog/settings panel
* Level: Functional requirement
* Preconditions: Application settings accessible
* Success End Condition: New storage directory set and applied
* Failed End Condition: Invalid directory rejected, previous setting retained
* Primary Actor: User
* Trigger: User opens settings and changes storage path

### MAIN SUCCESS SCENARIO

1. User opens settings/preferences dialog
2. User navigates to storage configuration
3. User enters or browses for storage directory path
4. Application validates directory path
5. Application creates directory if it doesn't exist
6. Setting saved and applied immediately
7. All future CSV files use new storage directory
8. Existing entries remain accessible from old location

### EXTENSIONS

3a <step 3> <Empty path> : Use default ./log/ directory

3b <step 3> <Relative path> : Resolve relative to application root

3c <step 3> <Absolute path> : Use path as-is

4a <step 4> <Invalid path> : Show error, require valid path

5a <step 5> <Directory creation fails> : Show error, require different path

### SUB-VARIATIONS

6 <list of sub-variations> : Can set per-project storage or global default

### RELATED INFORMATION (optional)

* Priority: Medium
* Performance Target: <100ms for setting change
* Frequency: Rare (initial setup only)

---

# Use Case: UC-010 Auto-create Storage Directory

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: Application creates storage directory automatically if missing
* Scope: Directory creation on application startup
* Level: Technical requirement
* Preconditions: Storage path configured
* Success End Condition: Storage directory exists and is writable
* Failed End Condition: Error shown if directory cannot be created
* Primary Actor: Application
* Trigger: Application startup

### MAIN SUCCESS SCENARIO

1. Application starts
2. Storage path resolved (config or default ./log/)
3. Application checks if directory exists
4. If not exists, create directory with parent directories
5. Verify directory is writable
6. Continue with normal startup

### EXTENSIONS

4a <step 4> <Permission denied> : Show error, exit with message

5a <step 5> <Not writable> : Show error, exit with message

### SUB-VARIATIONS

3 <list of sub-variations> : Can also create if path is a file (error)

### RELATED INFORMATION (optional)

* Priority: Critical
* Performance Target: <100ms for directory check/create
* Frequency: On every startup

---

# Use Case: UC-011 Load All History on Startup

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: Application automatically displays all activity history on startup
* Scope: Auto-load of history on application launch
* Level: Functional requirement
* Preconditions: Storage directory exists with CSV files
* Success End Condition: All entries displayed in history view
* Failed End Condition: Empty state shown if no files or load error
* Primary Actor: User
* Trigger: Application startup

### MAIN SUCCESS SCENARIO

1. Application launches
2. Storage directory loaded
3. All CSV files matching pattern found
4. All entries read from all files
5. Entries displayed in chronological order
6. Time sums calculated and displayed
7. History view ready for interaction

### EXTENSIONS

2a <step 2> <No storage directory> : Create directory (UC-010), show empty state

3a <step 3> <No CSV files> : Show empty state with message

4a <step 4> <File read error> : Log error, skip file, continue with others

### SUB-VARIATIONS

5 <list of sub-variations> : Can be disabled in settings for faster startup

### RELATED INFORMATION (optional)

* Priority: Critical
* Performance Target: <1s for complete load
* Frequency: On every startup

---

# Use Case: UC-012 Edit Activity Entry

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User modifies an existing activity entry
* Scope: Inline editing or dialog-based editing
* Level: Functional requirement
* Preconditions: Entry visible in history view
* Success End Condition: Modified entry saved to CSV, persisted to disk
* Failed End Condition: Invalid modification rejected, no file change
* Primary Actor: User
* Trigger: User double-clicks entry or selects edit option

### MAIN SUCCESS SCENARIO

1. User initiates edit on selected entry
2. Current values displayed in editable form
3. User modifies any field (activity type, description, status, comment, time)
4. User confirms changes
5. Entry updated in memory
6. CSV file rewritten with updated entry
7. History view refreshes to show modification
8. Timestamp of modification tracked (optional column)

### EXTENSIONS

2a <step 2> <Entry locked> : Show error, entry currently being edited by another instance

4a <step 4> <User cancels> : Discard changes, return to history view

5a <step 5> <Validation error> : Show specific field error, require correction

### SUB-VARIATIONS

1 <list of sub-variations> : Can double-click entry, right-click menu, or keyboard shortcut (Ctrl+E)

4 <list of sub-variations> : Can also use Escape key to cancel

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: <200ms for save operation
* Frequency: Several times per day

---

# Use Case: UC-013 Persist Entry Edits to Disk

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: Modified entries written back to CSV file permanently
* Scope: CSV file rewrite with atomic save
* Level: Technical requirement
* Preconditions: Entry modified in memory
* Success End Condition: CSV file on disk contains updated entry
* Failed End Condition: Error shown, original file unchanged
* Primary Actor: Application
* Trigger: User confirms edit changes

### MAIN SUCCESS SCENARIO

1. Entry modification confirmed by user
2. Application reads entire CSV file into memory
3. Modified entry replaces original in memory
4. Temporary file created with new content
5. Atomic rename: temp file becomes original file
6. Temporary file deleted if rename fails
7. Confirmation shown to user

### EXTENSIONS

2a <step 2> <File read error> : Show error, abort save

4a <step 4> <Temp file creation fails> : Show error, abort save

5a <step 5> <Rename fails> : Restore original file, show error

### SUB-VARIATIONS

7 <list of sub-variations> : Can auto-save on every change (user preference)

### RELATED INFORMATION (optional)

* Priority: Critical
* Performance Target: <500ms for complete save
* Frequency: On each edit confirmation

---

# Use Case: UC-014 Display Status with Color Coding

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: Status field displayed with appropriate color for quick visual identification
* Scope: Text rendering with color based on status value
* Level: Technical requirement
* Preconditions: Entry displayed in history or search results
* Success End Condition: Status text rendered in correct color
* Failed End Condition: Default color used if status unknown
* Primary Actor: Application
* Trigger: Entry display rendering

### MAIN SUCCESS SCENARIO

1. Entry to be displayed retrieved
2. Status field value extracted
3. Color mapped based on status:
   - TODO: Yellow/Gold
   - DOING: Blue
   - DONE: Green
   - NOTE: Gray
   - Unknown: Default (white)
4. Status text rendered with color in UI
5. Color persists on scroll and resize

### EXTENSIONS

3a <step 3> <Custom status values> : Allow user-defined status colors in settings

### SUB-VARIATIONS

1 <list of sub-variations> : Can also color-code activity type or entire row

### RELATED INFORMATION (optional)

* Priority: Medium
* Performance Target: Color rendering <10ms per entry
* Frequency: On every display refresh

---

# Use Case: UC-015 Display Kanban View

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User views activities grouped by description in a Kanban-style board
* Scope: Kanban board with columns for each unique description
* Level: Functional requirement
* Preconditions: All entries loaded from CSV files
* Success End Condition: Kanban board displayed with columns for each description
* Failed End Condition: Empty state if no non-DONE entries exist
* Primary Actor: User
* Trigger: User switches to Kanban view

### MAIN SUCCESS SCENARIO

1. User switches to Kanban view tab
2. All entries loaded from all CSV files
3. Entries filtered: only keep lines where status != DONE
4. Entries grouped by description field
5. For each description, create a column/card
6. Entries within each column sorted by timestamp (oldest first)
7. Columns displayed in grid layout (2x2 or flexible)
8. Each column shows: description header, count, all entries in timestamp order

### EXTENSIONS

3a <step 3> <All entries DONE> : Show "All items completed" message

4a <step 4> <Many unique descriptions> : Add scrollable area or pagination

### SUB-VARIATIONS

1 <list of sub-variations> : Can be separate tab, modal dialog, or side panel

7 <list of sub-variations> : Can be 1 column per description, or grouped by status first

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: <1s to render board with all descriptions
* Frequency: Daily review

---

# Use Case: UC-016 Interact with Kanban Entries

* [ ] implementation
* [ ] test 

## CHARACTERISTIC INFORMATION

* Goal in Context: User can interact with entries in Kanban view
* Scope: Click, edit, mark as done actions on Kanban cards
* Level: Functional requirement
* Preconditions: Kanban board displayed
* Success End Condition: User actions reflected in data and UI
* Failed End Condition: Invalid action rejected, no change
* Primary Actor: User
* Trigger: User clicks on Kanban card or action button

### MAIN SUCCESS SCENARIO

1. User clicks on entry card in Kanban column
2. Entry details shown (full description, comment, timestamp, time)
3. User can:
   - Edit entry (opens UC-012 edit dialog)
   - Mark as done (changes status to DONE, removes from Kanban)
   - Copy description (for quick new entry)
4. Changes persisted to CSV (UC-013)
5. Kanban view updates immediately

### EXTENSIONS

3a <step 3> <User marks as done> : Entry removed from column on next render

3b <step 3> <User edits status> : If changed to DONE, remove from Kanban

4a <step 4> <Save fails> : Show error, keep original state

### SUB-VARIATIONS

3 <list of sub-variations> : Can drag-and-drop between descriptions

### RELATED INFORMATION (optional)

* Priority: High
* Performance Target: Action response <200ms
* Frequency: During daily review

---

## Implementation Notes for Agents

### Project Structure
```
memo-v2/
├── pom.xml
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
