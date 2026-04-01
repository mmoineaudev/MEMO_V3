# MEMO V3 - Data Models and API Reference

## 1. Core Data Models

### 1.1 ActivityEntry (Record)

**Location**: `src/main/java/com/memo/model/ActivityEntry.java`

```java
public record ActivityEntry(
    String activityType,    // Unique identifier for the activity category
    String description,     // Detailed description of what was done
    String status,          // Current state: TODO|DOING|DONE|NOTE
    String comment,         // Optional notes or additional context (can be null)
    LocalDateTime timestamp,// When the entry was created/last modified
    int timeSpent           // Minutes spent on the activity (0 if not tracked)
)
```

**Characteristics**:
- Immutable data structure (Java Record)
- No validation in constructor - validated by services
- All fields are non-null except comment
- Timestamp uses `LocalDateTime` for date and time precision

**Example Usage**:
```java
ActivityEntry entry = new ActivityEntry(
    "Development",           // activityType
    "Fixed bug in login",    // description
    "DONE",                  // status
    "Issue #123 resolved",   // comment
    LocalDateTime.now(),     // timestamp
    45                       // timeSpent (minutes)
);
```

---

### 1.2 Status (Enum)

**Location**: `src/main/java/com/memo/model/Status.java`

```java
public enum Status {
    TODO("To Do"),
    DOING("In Progress"),
    DONE("Done"),
    NOTE("Note");
    
    private final String displayName;
    
    Status(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

**Valid Values**:
| Constant | Display Name | UI Color | Description |
|----------|--------------|----------|-------------|
| TODO | To Do | Blue | Task to be completed |
| DOING | In Progress | Orange | Currently being worked on |
| DONE | Done | Green | Completed task |
| NOTE | Note | Magenta | Informational note |

**Usage**:
```java
// Create entry with status
ActivityEntry entry = new ActivityEntry("Dev", "Task", Status.DOING, "", now, 30);

// Check status
if (entry.status().equals(Status.DONE)) {
    System.out.println("Completed!");
}

// Get display name
String displayName = Status.DONE.getDisplayName(); // "Done"
```

---

## 2. Service Layer API Reference

### 2.1 HistoryService

**Location**: `src/main/java/com/memo/service/HistoryService.java`

**Purpose**: In-memory repository for ActivityEntry objects.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `add()` | ActivityEntry | void | Add new entry to history |
| `getAll()` | - | List<ActivityEntry> | Get all entries (newest first) |
| `get()` | String id | ActivityEntry | Find entry by activityType |
| `update()` | ActivityEntry | void | Replace existing entry |
| `delete()` | String id | void | Remove entry by activityType |
| `clear()` | - | void | Remove all entries |
| `size()` | - | int | Get entry count |

**Example**:
```java
HistoryService history = new HistoryService();

// Add entry
history.add(new ActivityEntry("Dev", "Task", "TODO", "", now, 0));

// Get all entries
List<ActivityEntry> all = history.getAll();

// Find specific entry
ActivityEntry found = history.get("Development");

// Update entry
ActivityEntry updated = new ActivityEntry("Dev", "Updated", "DONE", "", now, 60);
history.update(updated);

// Delete entry
history.delete("Development");
```

---

### 2.2 EntryEditorService

**Location**: `src/main/java/com/memo/service/EntryEditorService.java`

**Purpose**: Facilitate entry creation and editing with intelligent defaults.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `create()` | String type, String desc, int time | ActivityEntry | Create new entry |
| `create()` | String type, String desc, int time, LocalDateTime ts | ActivityEntry | Create with custom timestamp |
| `update()` | String type, String desc, String status, int time | ActivityEntry | Update existing entry |
| `update()` | String type, String desc, String status, int time, LocalDateTime ts | ActivityEntry | Update with custom timestamp |
| `getRecentDescriptions()` | int limit | List<String> | Get recent descriptions for auto-suggest |
| `setLastDescription()` | String description | void | Set description to remember |
| `addToHistory()` | ActivityEntry | void | Directly add entry to history |
| `getHistoryService()` | - | HistoryService | Access underlying service |

**Example**:
```java
EntryEditorService editor = new EntryEditorService(historyService);

// Create new entry (auto-populates description from history if empty)
ActivityEntry entry = editor.create("Development", "Fixed bug", 45);

// Update existing entry
ActivityEntry updated = editor.update("Development", "Updated desc", "DONE", 60);

// Get suggestions for dropdown
List<String> suggestions = editor.getRecentDescriptions(10);
```

---

### 2.3 CsvStorageService

**Location**: `src/main/java/com/memo/service/CsvStorageService.java`

**Purpose**: Handle CSV file I/O operations for persistent storage.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `loadAllEntries()` | - | List<ActivityEntry> | Read all entries from all CSV files |
| `saveEntry()` | ActivityEntry | void | Append entry to current day's file |
| `getStorageDirectory()` | - | String | Get configured storage path |
| `setStorageDirectory()` | String path | void | Set new storage path |
| `ensureStorageDirectoryExists()` | - | void | Create directory if missing |

**CSV Format**:
```csv
activityType,description,status,comment,timestamp,timeSpent
Development,Fixed bug,DONE,Issue #123,2026-04-01T10:30:00,45
Meeting,Team standup,TODO,,2026-04-01T09:00:00,30
```

**File Naming**: `YYYY-MM-DD.csv` (one file per day)

**Example**:
```java
CsvStorageService storage = new CsvStorageService("./data/");

// Load all entries on startup
List<ActivityEntry> entries = storage.loadAllEntries();

// Save new entry
storage.saveEntry(new ActivityEntry("Dev", "Task", "TODO", "", now, 0));

// Configure storage location
storage.setStorageDirectory("/custom/path/to/data/");
```

---

### 2.4 SearchService

**Location**: `src/main/java/com/memo/service/SearchService.java`

**Purpose**: Filter entries based on multiple criteria.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `search()` | List<String> descFilters, List<String> typeFilters, String status | List<ActivityEntry> | Multi-criteria search |
| `calculateTotalTime()` | List<ActivityEntry> entries | int | Sum time spent |

**Search Behavior**:
- Description filter: Case-insensitive partial match
- Type filter: Exact match (case-insensitive)
- Status filter: Exact match (optional, null = all statuses)
- All filters are AND conditions

**Example**:
```java
SearchService search = new SearchService(historyService);

// Search by description only
List<String> descFilters = List.of("bug", "fix");
List<ActivityEntry> results = search.search(descFilters, List.of(), null);

// Search by type and status
results = search.search(List.of(), List.of("Development"), "DONE");

// Get total time for results
int totalTime = search.calculateTotalTime(results);
```

---

### 2.5 SummaryService

**Location**: `src/main/java/com/memo/service/SummaryService.java`

**Purpose**: Generate summary statistics from entries.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `getDailySummary()` | List<ActivityEntry> | Map<String, Integer> | Time per type for today |
| `getWeeklySummary()` | - | Map<String, Integer> | Time per type for current week |

**Return Format**: `Map<activityType, totalMinutes>`

**Example**:
```java
SummaryService summary = new SummaryService(historyService);

// Get daily breakdown
Map<String, Integer> daily = summary.getDailySummary(allEntries);
// Example: {"Development": 180, "Meeting": 60}

// Get weekly breakdown
Map<String, Integer> weekly = summary.getWeeklySummary();
```

---

### 2.6 TimeCalculationService

**Location**: `src/main/java/com/memo/service/TimeCalculationService.java`

**Purpose**: Perform time-related calculations and formatting.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `formatTime()` | int minutes | String | Convert to "X days, Y hours, Z min" |
| `calculateTotalMinutes()` | List<ActivityEntry> | int | Sum all time spent |

**Formatting Examples**:
- `formatTime(0)` → `"0 minutes"`
- `formatTime(90)` → `"1 hour 30 minutes"`
- `formatTime(1500)` → `"1 day, 1 hour 0 minutes"`

**Example**:
```java
TimeCalculationService timeCalc = new TimeCalculationService();

// Format for display
String formatted = timeCalc.formatTime(180); // "3 hours"

// Calculate total from entries
int totalMinutes = timeCalc.calculateTotalMinutes(entries);
```

---

### 2.7 KanbanService

**Location**: `src/main/java/com/memo/service/KanbanService.java`

**Purpose**: Manage Kanban board state and filtering.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `getTasksByStatus()` | String status | List<ActivityEntry> | Get entries in specific column |
| `getTaskCountByStatus()` | String status | int | Count tasks in column |
| `getAllTasks()` | - | List<ActivityEntry> | Return all entries for board |

**Example**:
```java
KanbanService kanban = new KanbanService(historyService);

// Get TODO tasks
List<ActivityEntry> todos = kanban.getTasksByStatus(Status.TODO);

// Count DOING tasks
int doingCount = kanban.getTaskCountByStatus(Status.DOING);

// Display all on board
List<ActivityEntry> all = kanban.getAllTasks();
```

---

### 2.8 SettingsService

**Location**: `src/main/java/com/memo/service/SettingsService.java`

**Purpose**: Manage application configuration and settings persistence.

**Methods**:

| Method | Parameters | Returns | Description |
|--------|------------|---------|-------------|
| `getStorageDirectory()` | - | String | Get configured storage path |
| `setStorageDirectory()` | String path | void | Set new storage path |
| `saveSettings()` | - | void | Persist settings to disk |
| `loadSettings()` | - | void | Restore settings from disk |

**Example**:
```java
SettingsService settings = new SettingsService();

// Get current storage path
String path = settings.getStorageDirectory(); // "./data/"

// Change storage location
settings.setStorageDirectory("/custom/storage/");
settings.saveSettings();
```

---

## 3. UI Components API

### 3.1 MemoFrame

**Location**: `src/main/java/com/memo/ui/MemoFrame.java`

**Purpose**: Main application window.

**Constructor**:
```java
public MemoFrame()
```

**Key Methods**:
- `createMainLayout()`: Assembles UI components
- `refreshAll()`: Refresh all panels

---

### 3.2 ActivitiesPanel

**Location**: `src/main/java/com/memo/ui/ActivitiesPanel.java`

**Purpose**: Unified panel for history, search, and summary views.

**Constructor**:
```java
public ActivitiesPanel(HistoryService historyService,
                       EntryEditorService editorService,
                       SearchService searchService,
                       SummaryService summaryService,
                       TimeCalculationService timeCalcService)
```

**Key Features**:
- JTable for entry display
- Filter panel (description, type, status)
- Daily and weekly summary panels
- Sortable columns
- Color-coded status renderer

---

### 3.3 EntryEditorDialog

**Location**: `src/main/java/com/memo/ui/EntryEditorDialog.java`

**Purpose**: Modal dialog for creating new entries.

**Constructor**:
```java
public EntryEditorDialog(Frame owner, HistoryService historyService)
```

**Key Features**:
- Activity type input field
- Description with auto-suggest dropdown
- Status selection (combo box)
- Comment text area
- Time spent numeric field
- Recent descriptions from history

---

### 3.4 KanbanPanel

**Location**: `src/main/java/com/memo/ui/KanbanPanel.java`

**Purpose**: Visual workflow management interface.

**Constructor**:
```java
public KanbanPanel(HistoryService historyService, KanbanService kanbanService)
```

**Key Features**:
- Four columns (TODO, DOING, DONE, NOTE)
- Entry cards with action buttons
- Inline editing via double-click
- Status change buttons (← →)
- Task count per column

---

## 4. Integration Example

### Creating a Complete Application Flow

```java
// 1. Initialize services
CsvStorageService storage = new CsvStorageService("./data/");
HistoryService history = new HistoryService(storage.loadAllEntries());
EntryEditorService editor = new EntryEditorService(history);
KanbanService kanban = new KanbanService(history);
SearchService search = new SearchService(history);
SummaryService summary = new SummaryService(history);
TimeCalculationService timeCalc = new TimeCalculationService();

// 2. Create UI with services
MemoFrame frame = new MemoFrame();
frame.setServices(history, editor, kanban, search, summary, timeCalc);

// 3. User creates entry
ActivityEntry newEntry = editor.create("Development", "Implement feature", 120);
storage.saveEntry(newEntry);
history.add(newEntry);

// 4. User searches
List<ActivityEntry> results = search.search(
    List.of("feature"), 
    List.of("Development"), 
    null
);

// 5. User views summary
Map<String, Integer> daily = summary.getDailySummary(history.getAll());
String formatted = timeCalc.formatTime(timeCalc.calculateTotalMinutes(results));

// 6. User moves entry on Kanban
ActivityEntry todo = kanban.getTasksByStatus(Status.TODO).get(0);
// User clicks "→" button
ActivityEntry doing = new ActivityEntry(
    todo.activityType(), 
    todo.description(), 
    Status.DOING, 
    todo.comment(), 
    todo.timestamp(), 
    todo.timeSpent()
);
history.update(doing);
```

---

## 5. Error Handling

### Expected Error Conditions

| Scenario | Handling | User Impact |
|----------|----------|-------------|
| Storage directory missing | Auto-created | None |
| CSV file read error | Logged, skipped | Entry missing from display |
| Invalid input in dialog | Validation before save | Error message shown |
| No entries found | Empty state displayed | "No entries" message |
| Search with no results | Empty result list | "No matches found" |

### Exception Types

- **IOException**: File I/O errors (caught and logged)
- **NumberFormatException**: Invalid time input (validated in UI)
- **NullPointerException**: Null data from services (defensive checks)

---

## 6. Data Flow Diagrams

### Entry Creation Flow
```
User Input → EntryEditorDialog → EntryEditorService.create() 
           → CsvStorageService.saveEntry() → CSV File
           → HistoryService.add() → In-Memory Cache
           → UI Refresh → Display Updated
```

### Search Flow
```
User Filters → ActivitiesPanel → SearchService.search()
            → Filter Criteria Applied → Matching Entries
            → TimeCalculationService → Total Time
            → Table Update → Display Results
```

### Kanban Status Change Flow
```
User Clicks Button → KanbanPanel.changeEntryStatus()
                  → HistoryService.update() → In-Memory Cache
                  → CsvStorageService.saveEntry() → CSV File
                  → refreshKanban() → Reload Columns
                  → UI Refresh → Display Updated
```
