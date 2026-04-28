**Mobile Development 2025/26 Portfolio**
# Requirements

Student ID: `c24067465`

_Complete the information above and then enumerate your functional and non-functional requirements below. You can delete this line.__

### Functional Requirements
**Data Ingestion & Processing**

- FR1: The system must allow the user to select multiple digital PDF documents from the device's local file storage using the Android Storage Access Framework (SAF).

- FR2: The system must extract data (supplier name, reference, date, and amount) entirely offline. This will be achieved using Google ML Kit’s Entity Extraction API alongside Custom Regex Heuristics.

- FR3: The system must persist imported quotes and grouped profiles across sessions utilizing a local Room Database (SQLite) with structured Data Access Objects (DAOs).

**User Interface & Interaction**

- FR4:The system must display a comparative list of all parsed quotes using Jetpack Compose (LazyColumn), dynamically sorted by a user-selected metric (Cheapest, Most Recent, Overall).

- FR5: To provide recommendations entirely offline, the system must calculate the optimal 'Overall' quote using a custom Euclidean Distance Algorithmic Engine that mathematically ranks price and date variances.

- FR6: The system must ensure the top recommended quote is visually distinguished from the standard list using Jetpack Compose styling and structural hierarchy.

**External Integration & Export**

- FR7: The system must natively generate a formatted summary PDF containing the comparative list and the explicitly highlighted recommendation using the PDFBox library, allowing export via SAF.

### Non-Functional Requirements

**Performance & Reliability**

- NFR1: The text extraction, data parsing, and recommendation pipeline must execute 100% offline to guarantee functionality in low-connectivity field environments and ensure absolute data privacy.

- NFR2: Heavy operations (database queries, PDF parsing) must execute asynchronously using Kotlin Coroutines (Dispatchers.IO) to prevent blocking the main thread and dropping UI frames.

- NFR3: The system must handle malformed PDF data gracefully by applying fallback defaults, ensuring the pipeline drops invalid data to the bottom of the rankings rather than crashing.

**Android-Specific Constraints**

- The system must ensure absolute UI and data consistency across standard Android configuration changes (e.g., screen rotations) by utilizing a ViewModel as the single source of truth, paired with robust Compose state management (derivedStateOf).


