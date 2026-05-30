# Project Changelog

This document keeps track of all the features and updates implemented in our Search Engine project across all three iterations.

---

## [v1.0.0] - Iteration 1: Search Engine Core

In this first iteration, we built the base database indexing and crawl system.

### Added
- **File Crawler**: Created recursive folder scanning that goes through directories to find files. Handles restricted folders and symlink loops safely without crashing.
- **Database & Schema**: Created a database connection to store indexed files and their metadata (name, size, path, file type, last modified date).
- **FTS Search Queries**: Implemented database-backed search utilizing the DBMS's full-text search capability. Handles single-word and multi-word searches.
- **Console & Basic UI**: Created a simple layout to run search queries and see search results with a text snippet preview.
- **Incremental Indexing**: Added logic to detect if a file was changed since the last index run so we only update modified files instead of rebuilding the database.
- **Reporting & Config**: Added setting ignore rules for directories, setting root folders at runtime, and printing a summary index report at the end.

---

## [v2.0.0] - Iteration 2: Query Parsing, Ranking, and Search History

In the second iteration, we added smart search features like query qualifiers, custom relevance scoring, swappable rankings, and search history suggestions.

### Added
- **Query Parser Qualifiers**: Added support to filter searches using qualifiers like `path:src` or `content:java` in the search box.
- **Path Relevance Scoring**: Implemented file path scoring during indexing, prioritizing shorter paths and keyword matches in the directory path.
- **Swappable Ranking Strategies**: Used the Strategy Pattern to let users toggle search sorting (like alphabetical, path length, or history ranking) in the UI.
- **Search History tracking (Observer Pattern)**: Created a system that records queries so we can:
  - Provide fuzzy autocomplete suggestions in the UI.
  - Prioritize search results that were previously clicked or searched.

---

## [v3.0.0] - Iteration 3: Colors, Widgets, Decorators, and Threading

In the final iteration, we added image dominant color matching, context-aware panels, query pre-processors, and multi-threaded indexing.

### Added
- **Multimodal Search & Strategy Pattern**:
  - Implemented the Strategy Pattern to split processing by file types (text files vs image files).
  - Built an image processor that decodes images, calculates their average RGB color, and classifies them into basic names (red, green, blue, brown, black, white, gray, yellow, purple, orange).
  - Enabled searching for image colors using queries like `color:red`.
- **Context-Aware Widgets (Factory Pattern)**:
  - Created `WidgetFactory` to check results for specific file types.
  - Implemented `AnalyzeLogsWidget` (shown when results contain mostly `.log` files) and `GalleryWidget` (shown when many images are found).
- **Query Decorator Pipeline (Decorator Pattern)**:
  - Built decorators to clean and expand queries before they run:
    - `SanitizationDecorator`: Removes bad characters.
    - `SynonymDecorator`: Expands terms (e.g. `img` expands to `image OR photo`).
    - `LogicDecorator`: Adds wildcards (`*`) to support prefix matching.
- **Producer-Consumer Multi-threading**:
  - Re-engineered indexing to run in parallel.
  - Uses multiple reader threads (`FileReaderWorker`) to parse files and single writer thread (`DatabaseWriterWorker`) to commit changes to the database.
  - Shared a thread-safe `BlockingQueue` and used a "poison pill" object to shut down cleanly.
- **Pre-commit check**:
  - Added a Git pre-commit script to verify that the project compiles with Maven before allowing commits.
