**Mobile Development 2025/26 Portfolio**
# API Description

Student ID: `c24067465`

_Complete the information above and then write your 300-word API description here. Do not exceed the limit. You can delete this line.__

UI & State Management (Jetpack Compose)

To avoid XML bloat and accelerate the development, I built the whole interface using
Jetpack Compose. Because QuoteScout handles such inconsistent data, Compose’s declarative and
State-driven architecture was the optimal solution. By using derivedStateOf to process
metrics like the "Overall Best", it makes sure the UI is only recomposing when the data
actually changes. completely removing the boilerplate you normally need for a
traditional RecyclerView.

Screen Routing (Navigation Component)

To avoid managing multiple activities and complex intent passing, I implemented the
Compose navigation component. Which gives a clean single-activity architecture. Passing the
profile IDs safely as NavType. IntType arguments in the navigation graph keep that data.
context seamless across screens without heavy activity lifecycle overhead.

Data Persistence (AndroidX Room)

I chose AndroidX Room over raw SQLite since the SQL abstraction layer mirrors the
Java Persistence API (JPA). Which let me leverage existing knowledge for a rapid, error-free
setup. Additionally, serialising the ML-extracted JSON straight into Room entities ensures sensitive
Corporate pricing never leaves the device. adhering to the strict offline requirements.

Asynchronous Processing (Kotlin Coroutines)

Extracting text via ML Kit and generating PDFBox reports are process-heavy operations. To
To prevent UI stuttering, I directed this pipeline using Kotlin Coroutines rather than legacy
AsyncTask methods. Offloading database queries and file I/O to dispatchers. IO provides better
performance and structured error handling. In addition, I used suspendCoroutine to bridge ML Kit’s
older listener-based callbacks into clean, suspended functions.

System File Access (Activity Result API)

To import PDFs without compromising the UX with permission pop-ups, I implemented
'ActivityResultContracts. OpenMultipleDocuments()'. Which natively distributes file selection
straight to the Android OS. Securely returning document URIs without broad read/write
permissions. Respecting user privacy and totally eliminating the boilerplate code required to
handle legacy permission requests manually.