# LiteLoop - Lightweight Wear OS Periodic Reminder

LiteLoop is a minimalist Wear OS application designed for the Samsung Galaxy Watch 4 (and other Wear OS devices). It allows users to set multiple periodic reminders that trigger a **vibration** and a **Text-to-Speech (TTS) announcement** of the task name.

This app is specifically optimized for efficiency and handles complex scheduling scenarios, including windows that span across midnight.

## Features

- 🕒 **Minute-Level Precision**: Set start and end times for your reminders with exact minute control.
- 🔄 **Periodic Intervals**: Choose how often you want to be reminded (e.g., every 5, 10, or 30 minutes).
- 🌓 **Overnight Support**: Robust scheduling logic that handles windows spanning across midnight (e.g., 22:00 to 04:00).
- 🗣️ **Voice & Vibration**: Every reminder triggers a haptic vibration and speaks the name of the task using the watch's TTS engine.
- 📋 **Next Run Indicator**: See exactly when your next reminder is scheduled to trigger directly on the home screen.
- 💾 **Persistence**: Tasks are stored locally using Room and are automatically rescheduled after a watch reboot.

## UI Preview

| Task List | Add/Edit Task |
| :---: | :---: |
| ![Task List](https://raw.githubusercontent.com/placeholder-link-to-screenshot1.png) | ![Edit Task](https://raw.githubusercontent.com/placeholder-link-to-screenshot2.png) |

*(Note: Replace with actual screenshots after uploading to GitHub)*

## Installation for Developers

### Prerequisites
- [Android SDK Platform-Tools](https://developer.android.com/studio/releases/platform-tools) (for `adb`).
- Java 17 or higher.

### Steps
1. **Enable Developer Options** on your watch (Settings > About watch > Software info > Tap "Software version" 7 times).
2. **Enable Wireless Debugging** (Settings > Developer options > Wireless debugging).
3. **Connect via ADB**:
   ```bash
   adb pair [IP_ADDRESS]:[PAIRING_PORT] [PAIRING_CODE]
   adb connect [IP_ADDRESS]:[CONNECT_PORT]
   ```
4. **Build and Install**:
   ```bash
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Technical Details

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose for Wear OS (Material 3)
- **Database**: Room Persistence Library
- **Scheduling**: AlarmManager (`setExactAndAllowWhileIdle`) for high-precision timing and battery efficiency.
- **Architecture**: MVVM with Coroutines and Flow.

## License

This project is licensed under the MIT License.
