# CycleTasker

> A quiet, offline task tracker built to help you keep the promises you make to yourself.

CycleTasker is a native Android application built with **Kotlin**, **Jetpack Compose**, and **Material 3**. It focuses on simplicity, reliability, and long-term consistency instead of notifications, achievements, and productivity gimmicks.

The app supports both standalone repeating tasks and custom routine cycles of any length, making it suitable for training schedules, maintenance routines, study plans, work rotations, and everyday habits.

---

## Philosophy

CycleTasker is intentionally designed to stay out of your way.

- No accounts
- No cloud sync
- No ads
- No subscriptions
- No internet permission
- No intrusive pop-ups
- No streak punishment
- No gamification
- No unnecessary complexity

Your data belongs to you and lives entirely on your device.

---

## Features

### Today
- View everything due today in a single ordered list.
- Edit today's progress at any time.
- Access and modify yesterday if something was forgotten.
- Automatic completion percentages and progress tracking.

### Individual Tasks
- Fixed repeating schedules based on an original start date.
- Examples:
  - Every day
  - Every 3 days
  - Every 14 days
- Schedules never shift automatically.

### Custom Cycles
- Create routines of any length.
- Examples:
  - 3-on / 1-off
  - 8-day training rotations
  - Maintenance schedules
  - Shift patterns
- Each cycle day has its own title and checklist.

### Calendar History
- Clean, uncluttered month view.
- Past days display completion percentages.
- Older entries are locked to preserve history.
- Today and yesterday remain editable.

### Local Storage
- Human-readable JSON files.
- Import and export support.
- Fully offline operation.

---

## Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Design System:** Material 3
- **Storage:** Local JSON
- **Minimum SDK:** Android 10 (API 29)

---

## Building

Open the project in **Android Studio** and run the application normally.

Alternatively, build from the command line:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug
