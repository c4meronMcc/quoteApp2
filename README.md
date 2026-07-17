# Procurement Quote Extractor: Android ML Application

This is a mobile-optimized procurement decision-support application built for Android. It enables users to rapidly capture, extract, and normalize complex quote data from physical documents and digital files, streamlining the procurement and purchasing process.

Designed with an offline-first architecture, the application relies entirely on on-device machine learning to parse text, ensuring sensitive pricing and vendor data remains secure and private.

## 🚀 Key Features

* **On-Device OCR & Extraction:** Integrates Google ML Kit for high-speed, offline text recognition to parse complex quotation documents without needing a network connection.
* **Flexible Data Capture:** Allows users to capture data immediately using the device camera, or by importing existing digital files via the native Android Storage Access Framework.
* **Robust Local Persistence:** Utilizes the Room Database library (an abstraction layer over SQLite) to securely store, structure, and query normalized quote data locally on the device.
* **Modern UI/UX:** Built with Kotlin and adhering to modern Android design principles for a responsive, intuitive user experience.

## 🛠️ Architecture & Tech Stack

* **Language:** Kotlin
* **Machine Learning:** Google ML Kit (Vision API / Text Recognition)
* **Local Database:** Room (SQLite)
* **File Management:** Android Storage Access Framework
* **Development Environment:** Android Studio

## 🧠 Engineering Decisions & Business Value

A critical architectural decision for this project was the implementation of **offline-first processing**. 

By utilizing Google ML Kit for on-device OCR instead of routing documents to a cloud-based API (like AWS Textract or Google Cloud Vision), the application provides three major business benefits:
1. **Absolute Data Privacy:** Sensitive B2B pricing documents never leave the user's device.
2. **Zero Latency & Offline Utility:** Procurement officers can scan documents in environments with poor connectivity (e.g., warehouses, factory floors, construction sites).
3. **Zero Recurring Costs:** Eliminates the operational overhead of paying per-page API extraction fees.

## ⚙️ Local Setup & Development

### Prerequisites
* Android Studio (Latest stable release)
* JDK 17+
* Android SDK (API Level 24+ recommended)

### Installation & Running Locally
1. Clone the repository: `git clone https://github.com/yourusername/quote-extractor-android.git`
2. Open Android Studio and select **File > Open**, then navigate to the cloned repository directory.
3. Allow Gradle to sync the project dependencies.
4. Set up an Android Virtual Device (AVD) or connect a physical Android device via USB debugging.
5. Click the **Run** button (or press `Shift + F10`) to build and deploy the APK to your device.

## 🧪 Testing
The local database layer (Room) is designed to be easily testable. Instrumented tests can be run directly on an emulator or physical device to verify data insertion, retrieval, and schema migrations.

---
*Engineered by Cameron Mccreadie Chaplin. For professional inquiries regarding Android development, Kotlin, or on-device Machine Learning integrations, feel free to reach out.*
