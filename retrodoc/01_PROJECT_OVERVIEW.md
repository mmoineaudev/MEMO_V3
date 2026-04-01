# MEMO V3 - Project Overview

## Project Description

MEMO V3 is a Java Swing-based desktop application for tracking and managing daily activities with time logging capabilities. It provides users with tools to log activities, view history, search entries, track time spent on various tasks, and manage workflow through a Kanban board interface.

## Technology Stack

- **Language**: Java 17
- **UI Framework**: Java Swing (javax.swing)
- **Build Tool**: Maven
- **Testing Framework**: JUnit 5
- **Data Storage**: CSV files with per-day organization
- **Version Control**: Git

## Project Structure

```
MEMO_V3/
├── pom.xml                          # Maven build configuration
├── USE_CASES/                       # Original use case specifications
│   └── UC-001.md to UC-016.md       # 16 use case documents
├── retrodoc/                        # Generated documentation (this directory)
├── src/main/java/com/memo/
│   ├── model/
│   │   ├── ActivityEntry.java       # Core data model
│   │   └── Status.java              # Status enumeration
│   └── service/
│       ├── CsvStorageService.java   # File I/O operations
│       ├── EntryEditorService.java  # Entry creation/editing logic
│       ├── HistoryService.java      # In-memory entry management
│       ├── KanbanService.java       # Kanban board state management
│       ├── SearchService.java       # Search functionality
│       ├── SettingsService.java     # Application settings
│       ├── SummaryService.java      # Summary calculations
│       └── TimeCalculationService.java  # Time computations
│   └── ui/
│       ├── ActivitiesPanel.java     # Unified history/search/summary view
│       ├── EntryEditorDialog.java   # New entry creation dialog
│       ├── KanbanPanel.java         # Kanban board interface
│       └── MemoFrame.java           # Main application window
└── src/test/java/com/memo/          # Unit tests
```

## Core Features

1. **Activity Logging**: Create new activity entries with type, description, status, comment, and optional time tracking
2. **History View**: Display all logged activities in a sortable table with filtering capabilities
3. **Search Functionality**: Filter entries by multiple criteria (type, description, status, date range)
4. **Time Summaries**: Calculate daily and weekly time totals per activity type
5. **Kanban Board**: Visual workflow management with TODO, DOING, DONE, and NOTE columns
6. **Entry Editing**: Modify existing entries with auto-save functionality
7. **Settings Management**: Configure storage directory and application preferences

## Data Model

### ActivityEntry (Record)
The core data structure representing a single activity log:
- `activityType`: String identifier for the activity category
- `description`: Detailed description of what was done
- `status`: Current state (TODO, DOING, DONE, NOTE)
- `comment`: Optional notes or additional context
- `timestamp`: LocalDateTime when the entry was created/modified
- `timeSpent`: Integer minutes spent on the activity

### Status (Enum)
Defines four possible states for an activity:
- `TODO`: Task to be done (blue in UI)
- `DOING`: Currently in progress (orange in UI)
- `DONE`: Completed task (green in UI)
- `NOTE`: Informational note (magenta in UI)

## Architecture Pattern

The application follows a **Service-Oriented Architecture** with clear separation of concerns:

1. **Model Layer**: Data structures (ActivityEntry, Status)
2. **Service Layer**: Business logic and data management
3. **UI Layer**: Swing-based user interface components

### Component Interaction Flow

```
User Action → UI Component → Service Layer → Model/Data → Response to User
```

## Build & Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build Commands
```bash
# Compile project
mvn clean compile

# Run tests
mvn test

# Package application
mvn package

# Run application
java -jar target/memo-v3-1.0-SNAPSHOT.jar
```

## Storage Format

Entries are stored in CSV files organized by date:
- Location: Configurable storage directory (default: `./data/`)
- File naming: `YYYY-MM-DD.csv` (one file per day)
- CSV columns: `activityType,description,status,comment,timestamp,timeSpent`

## Testing

The project includes comprehensive unit tests covering all service layers:
- **108 tests total** with 100% pass rate
- Test coverage for models, services, and data operations
- No test failures or errors reported

## Version History

- Current version: 1.0-SNAPSHOT
- Build system: Maven
- Java target: 17
