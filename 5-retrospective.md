**Mobile Development 2025/26 Portfolio**
# Retrospective

Student ID: `c24067465`

_Complete the information above and then write your 200-word Retrospective here. Do not exceed the limit. You can delete this line.__

QuoteScout was originally designed to rely solely on fixed text extraction rules, though the inconsistencies in PDF formatting forced me to pivot to Google's ML Kit API which was a more robust method. Replacing brittle parsing exceptions with on-device natural language processing enabling a fully offline application that worked for off-site workers without risking any data privacy.

The most significant technical issue that I came across was managing thread execution, this was because using ML Kits extraction initially cause UI stuttering. locating the main thread blockage forced me to deeply engage with Kotlin coroutines. I refactored the architecture to perform expensive tasks asynchronously using Dispatchers.IO,  fundamentally changing how I approach Android lifecycles. Additionally, upgrading the recommendation engine to mathematical algorithm proved challenging, although necessary to having an accurate rank balanced quotes filter.

Building this significantly increased my understanding of Android architecture, transitioning from procedural code to a centralised state management architecture via  ViewModels and robust Jetpack Compose. If I worked on a new version, I would integrate Android's CameraX API alongside ML Kit Vision. So allowing users to take photos of physical, printed quotes, feeding extracted text right into my existing NLP pipeline, making it a multi-modal scanning tool.