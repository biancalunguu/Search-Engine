# Software Architecture: Search engine

---

## Level 1: System Context

The system context is the top level representing the entire system. It illustrates the high-level boundaries and interactions between the user, the core search application, and the local host environment.

### Core Entities

* **User**: The person searching for files on their device. 
* **Local Search Engine (Software System)**: The core system built to index files on the device, including documents, media, and binaries. It allows users to find files using filename, content, and metadata Note that for the first iteration, content search is focused specifically on textual files.
* **Local File System (External System)**: The host operating system's file system. The search engine treats this as an external data source, reading from it to discover files, build its index, and retrieve file contents.

---

## Level 2: Containers

The system context comprises a number of containers which are deployable units such as a web service or a database. One of the goals of a good architecture is to define the various boundaries between these containers so that when new requirements arise, the cost of change is minimized.

To ensure the search engine feels fast and responsive, the read (querying) and write (indexing) responsibilities are separated into distinct containers.

### Container Breakdown

* **Search Application (UI/CLI)**: The primary interface that captures user input, sends queries to the Query Engine, and renders formatted results including crucial file previews.
* **Indexing Service (Background Worker)**: A background process that crawls your local content , filters unwanted data, and performs incremental indexing by updating only modified records.
* **Query Engine (API/Service)**: An intermediary service that translates user input into optimized single-word and multi-word SQL queries utilizing the DBMS's full-text search features.
* **Database (DBMS)**: The offloaded storage layer whose schema determines how you process your data at query time
---

## Level 3: Components

Components are the major structural building blocks in code. Each container from Level 2 is broken down into specific components to organize the system's internal logic and separate concerns.

### Components of the Search Application (UI/CLI)
* **Input Controller**: Captures keystrokes in real-time and applies debounce logic to efficiently trigger searches as the user types, ensuring the interface remains fast and responsive.
* **API Client**: Manages the communication bridge, forwarding the raw search strings to the Query Engine and receiving the structured dataset in response.
* **Result Renderer**: Formats the returned data for the screen, specifically handling the clean layout of file paths, metadata, and the contextual file previews.

### Components of the Indexing Service
* **Configuration Manager**: Loads and manages system settings, including root directories to scan and specific folder ignore rules.
* **File Crawler**: Traverses the local directory tree recursively. It is built to safely handle operating system edge cases, such as circular folder references and restricted access permissions.
* **Data Extractor**: Reads the textual content of files and gathers associated metadata (e.g., file extensions, creation timestamps, and sizes) to build a rich search profile.
* **Database Synchronizer**: Manages the connection to the storage layer. It compares current file states against the existing database to update only the modified or new records, ensuring efficient background processing.
* **Telemetry & Reporting**: Monitors the background crawling progress and outputs summary reports upon completion.

### Components of the Query Engine
* **Query Parser**: Analyzes the raw text inputted by the user, cleans it, and structures it into standardized search parameters.
* **Search Executor**: Translates the parsed parameters into optimized, read-only database queries utilizing underlying text-matching capabilities.
* **Snippet Generator**: Processes the matched files to extract relevant contextual text blocks (previews) to display alongside the search results.

---

## Level 4: Code (Example: File Crawler)

The code level details the specific classes, functions, and data models that implement the components. While this layer changes frequently and is usually excluded from high-level C4 diagrams, the following classes illustrate the internal structure of the **File Crawler Component**:

* `DirectoryScanner`: Contains the core recursive methods to iterate through folders on the local drive.
* `FileValidator`: Implements the logic to verify read permissions and filter paths against the `ConfigurationManager`'s ignore list.
* `FileSystemNode`: A standardized data model representing a generic file or folder path.
* `CircularReferenceHandler`: A utility class specifically designed to detect and safely break out of infinite directory loops.