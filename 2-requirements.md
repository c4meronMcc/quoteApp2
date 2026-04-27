**Mobile Development 2025/26 Portfolio**
# Requirements

Student ID: `your id here`

_Complete the information above and then enumerate your functional and non-functional requirements below. You can delete this line.__
=======
**Mobile Development 2025/26 Portfolio**
# Requirements

Student ID: `c24067465`

_Complete the information above and then enumerate your functional and non-functional requirements below. You can delete this line.__


### Functional Requirements
**Data Ingestion & Processing**

- FR1: The system must allow the user to select multiple digital PDF documents from the device's local file storage.

- FR2: The system must extract textual data, specifically cost and delivery time from the selected PDFs entirely offline.

- FR3: The system must assign a unique identifier to each parsed quote to track it accurately through the application lifecycle.

**User Interface & Interaction**

- FR4: The system must display a comparative list of the top five parsed quotes, sorted by a user-selected metric.

- FR5: The system must provide a UI toggle to switch between the top five quotes and a complete list of all parsed quotes.

- FR6: The system must allow the user to select a single priority metric (e.g., lowest cost, fastest delivery) to drive the recommendation engine.

- FR7: The system must ensure the AI-recommended quote is visually distinguishable from non-recommended quotes at a glance within the list.

**External Integration & Export**

- FR8: The system must transmit the structured quote data and the user's chosen priority metric to an external AI API to request a recommendation.

- FR9: The system must generate and save a formatted summary PDF containing the comparative list and the explicitly highlighted recommendation.

### Non-Functional Requirements

**Performance & Reliability**

- NFR1: Local extraction of data from a single PDF must complete within 2 seconds.

- NFR2: External API requests for recommendations must resolve or time out within 10 seconds.

- NFR3: The system must block API requests and display a contextual error message if the user attempts to request a recommendation without an active internet connection.

**Android-Specific Constraints**

- NFR4: The application's UI state and parsed data must survive standard Android configuration changes, such as screen rotations, without requiring the user to re-import documents.

**Privacy & Security**

- NFR5: The system must transmit only the extracted, necessary data points to the external API, strictly retaining the original PDF files on the local device.

