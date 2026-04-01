# MEMO V3 - Technical Architecture Documentation

## 1. System Architecture Overview

MEMO V3 implements a **Three-Tier Architecture** with clear separation between:
- **Presentation Layer** (UI)
- **Business Logic Layer** (Services)
- **Data Access Layer** (Storage)

### Architectural Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   MemoFrame  │  │ActivitiesPanel│  │ KanbanPanel  │      │
│  │              │  │              │  │              │      │
│  │ EntryEditor  │  │ HistoryView  │  │ TODO Column  │      │
│  │   Dialog     │  │ SearchView   │  │ DOING Column │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                    Service Layer                     │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │   │
│  │  │ History  │ │ Kanban   │ │ Search   │ │Summary  │ │   │
│  │  │ Service  │ │ Service  │ │ Service  │ │ Service │ │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └─────────┘ │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌─────────┐ │   │
│  │  │ Entry    │ │ CSV      │ │Settings  │ │TimeCalc │ │   │
│  │  │ Editor   │ │ Storage  │ │ Service  │ │ Service │ │   │
│  │  │ Service  │ │ Service  │ └──────────┘ └─────────┘ │   │
│  │  └──────────┘ └──────────┘                        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                     DATA ACCESS LAYER                        │
│  ┌──────────────────────────────────────────────────────┐   │
│  │                  Data Models                         │   │
│  │  ActivityEntry (Record)  │  Status (Enum)           │   │
│  └──────────────────────────────────────────────────────┘   │
│                            │                                 │
│                            ▼                                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              External Storage                        │   │
│  │         CSV Files (per-day organization)             │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## 2. Component Details

### 2.1 UI Components

#### MemoFrame.java
**Purpose**: Main application window and entry point  
**Responsibilities**:
- Initialize application frame with proper sizing and layout
- Create and manage all panel components
- Handle window close operations
- Coordinate between different UI panels

**Key Methods**:
- `createMainLayout()`: Assembles the main UI structure
- `refreshAll()`: Triggers refresh of all panels

#### ActivitiesPanel.java
**Purpose**: Unified panel for history, search, and summary views  
**Responsibilities**:
- Display sortable table of all activity entries
- Provide filter inputs (description, type, status)
- Show daily and weekly time summaries
- Handle user interactions with entries

**Key Components**:
- `JTable`: Displays entries with columns for timestamp, type, description, status, comment, time
- Search panel with multiple filter fields
- Summary panels for daily and weekly statistics

#### EntryEditorDialog.java
**Purpose**: Modal dialog for creating new activity entries  
**Responsibilities**:
- Present form for entry creation
- Auto-populate description from recent history
- Validate input before submission
- Trigger storage service to persist entry

**Key Features**:
- History-based suggestion dropdown
- Optional time spent field
- Status selection (TODO, DOING, DONE, NOTE)

#### KanbanPanel.java
**Purpose**: Visual workflow management interface  
**Responsibilities**:
- Display four columns (TODO, DOING, DONE, NOTE)
- Enable drag-and-drop style status changes via buttons
- Show task counts per column
- Provide inline editing capability

**Key Methods**:
- `createKanbanColumn(Status)`: Creates a column with proper styling
- `refreshKanban()`: Reloads data from KanbanService
- `changeEntryStatus()`: Updates entry status in service layer

### 2.2 Service Components

#### HistoryService.java
**Purpose**: In-memory management of activity entries  
**Responsibilities**:
- Store all entries in a List
- Provide CRUD operations (add, get, update, delete)
- Return sorted list (newest first) on request

**Key Methods**:
- `add(ActivityEntry)`: Add new entry
- `get(String id)`: Find entry by activityType
- `getAll()`: Return all entries sorted
- `update(ActivityEntry)`: Replace existing entry
- `delete(String id)`: Remove entry

#### EntryEditorService.java
**Purpose**: Facilitate entry creation with intelligent defaults  
**Responsibilities**:
- Auto-populate description from last used or history
- Create new entries with proper timestamping
- Update existing entries while preserving data
- Provide recent descriptions for auto-suggest

**Key Methods**:
- `create(String activityType, String description, int timeSpent)`: New entry
- `update(String activityType, String description, String status, int timeSpent)`: Edit entry
- `getRecentDescriptions(int limit)`: Get history for suggestions
- `addToHistory(ActivityEntry)`: Direct history addition

#### KanbanService.java
**Purpose**: Manage Kanban board state  
**Responsibilities**:
- Track which entries belong to each column
- Provide task counts per status
- Enable filtering by status

**Key Methods**:
- `getTasksByStatus(String status)`: Get entries in specific column
- `getTaskCountByStatus(String status)`: Count tasks in column
- `getAllTasks()`: Return all entries for board display

#### SearchService.java
**Purpose**: Filter entries based on multiple criteria  
**Responsibilities**:
- Match entries against description, type, status filters
- Support partial, case-insensitive matching
- Calculate total time for filtered results

**Key Methods**:
- `search(List<String> descFilters, List<String> typeFilters, String status)`: Multi-criteria search
- `calculateTotalTime(List<ActivityEntry>)`: Sum time spent

#### SummaryService.java
**Purpose**: Generate summary statistics  
**Responsibilities**:
- Calculate daily time totals per activity type
- Compute weekly summaries (current week)
- Format time as days/hours/minutes

**Key Methods**:
- `getDailySummary(List<ActivityEntry>)`: Daily breakdown by type
- `getWeeklySummary()`: Weekly breakdown with date range

#### TimeCalculationService.java
**Purpose**: Perform time-related calculations  
**Responsibilities**:
- Convert minutes to hours:minutes format
- Calculate total time from entries
- Format time for display

**Key Methods**:
- `formatTime(int minutes)`: Convert to "X days, Y hours, Z minutes"
- `calculateTotalMinutes(List<ActivityEntry>)`: Sum all time

#### CsvStorageService.java
**Purpose**: Handle CSV file I/O operations  
**Responsibilities**:
- Read entries from CSV files (all dates)
- Write new entries to appropriate day's file
- Handle file creation if missing
- Parse and serialize ActivityEntry objects

**Key Methods**:
- `loadAllEntries()`: Read all CSV files from storage directory
- `saveEntry(ActivityEntry)`: Append entry to current day's file
- `getStorageDirectory()`: Return configured storage path
- `ensureStorageDirectoryExists()`: Auto-create if missing

#### SettingsService.java
**Purpose**: Manage application configuration  
**Responsibilities**:
- Store and retrieve storage directory path
- Persist settings across sessions
- Provide default values

**Key Methods**:
- `getStorageDirectory()`: Get configured path
- `setStorageDirectory(String path)`: Update path setting
- `saveSettings()`: Persist to disk
- `loadSettings()`: Restore from disk

### 2.3 Data Models

#### ActivityEntry.java (Record)
```java
public record ActivityEntry(
    String activityType,    // Category identifier
    String description,     // What was done
    String status,          // TODO|DOING|DONE|NOTE
    String comment,         // Optional notes
    LocalDateTime timestamp,// When logged
    int timeSpent           // Minutes spent
)
```

**Characteristics**:
- Immutable data structure (Java Record)
- No business logic, pure data carrier
- Used throughout all layers

#### Status.java (Enum)
```java
public enum Status {
    TODO("To Do"),
    DOING("In Progress"),
    DONE("Done"),
    NOTE("Note");
    
    private final String displayName;
}
```

**Characteristics**:
- Centralized status management
- Provides human-readable display names
- Color coding for UI representation
- Defines valid workflow states

## 3. Data Flow

### 3.1 Creating a New Entry

```
User clicks "New Entry" 
    → EntryEditorDialog opens
    → Description auto-populated from last used (EntryEditorService)
    → User fills form and confirms
    → EntryEditorService.create() called
    → CsvStorageService.saveEntry() persists to disk
    → HistoryService.add() updates in-memory cache
    → UI panels refresh via callbacks
```

### 3.2 Viewing History

```
Application starts or user requests refresh
    → MemoFrame.refreshAll() triggered
    → ActivitiesPanel loads data:
        → CsvStorageService.loadAllEntries() reads all CSV files
        → HistoryService stores entries in memory
        → SummaryService calculates daily totals
        → Table displays with sorted entries
```

### 3.3 Searching Entries

```
User enters search criteria in ActivitiesPanel
    → SearchService.search() executed
    → Filters applied (description, type, status)
    → Matching entries returned
    → Total time calculated via TimeCalculationService
    → Results displayed in filtered table view
```

### 3.4 Changing Kanban Status

```
User clicks "→" button on Kanban entry
    → changeEntryStatus() called with new status
    → KanbanService state updated
    → HistoryService.update() modifies entry
    → CsvStorageService.saveEntry() persists change
    → refreshKanban() reloads all columns
```

## 4. Design Patterns Used

### 4.1 Service Layer Pattern
Clear separation of business logic from UI and data access. Each service has a single responsibility.

### 4.2 Repository Pattern (Simplified)
HistoryService acts as an in-memory repository for ActivityEntry objects, providing CRUD operations.

### 4.3 Observer Pattern (Implicit)
UI components listen to service changes and refresh accordingly when data is modified.

### 4.4 Factory Pattern (Implicit)
CsvStorageService creates/parses ActivityEntry records from CSV data.

## 5. Concurrency Considerations

- All operations are single-threaded (Swing Event Dispatch Thread)
- No explicit synchronization required for current scope
- Large file loads may block UI (consider async loading for future)

## 6. Error Handling Strategy

- File I/O errors: Logged and skipped, application continues
- Invalid input: Validated before processing, user notified
- Missing data: Default values provided (e.g., "Task" as default description)
- Storage directory missing: Auto-created on first use

## 7. Extensibility Points

1. **New Status Types**: Extend Status enum and update UI color coding
2. **Alternative Storage**: Implement new storage service interface
3. **Additional Panels**: Add new JPanel subclasses to MemoFrame
4. **Export Formats**: Extend CsvStorageService for CSV variants
5. **Reporting**: Add new summary calculation services
