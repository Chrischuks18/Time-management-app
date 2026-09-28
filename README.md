# TimeFlow — Android Time Management App

TimeFlow is an offline-first Android productivity app designed around a simple loop: **plan → focus → finish → review**.

## Included in v1
- Today dashboard with workload summary and next actions
- Task capture, priorities, estimates, notes and completion
- Optional task reminders
- 25-minute focus timer with saved focus sessions
- Habit tracker with streaks
- Productivity insights: completion rate, focus minutes and completed tasks
- Room local database; data remains useful without an account or internet
- Material 3 / Jetpack Compose UI

## Product roadmap
1. Calendar/time-blocking day planner and drag-to-reschedule
2. Recurring tasks and richer reminder choices
3. Eisenhower Matrix + smart priority suggestions
4. Goals/projects with milestones
5. Weekly review, trends and time-budget reports
6. Widgets, quick-add shortcuts and notification actions
7. Backup/sync and optional calendar integration
8. Adaptive focus sessions, distraction log and break routines

## Build
Open in Android Studio, use JDK 17, sync Gradle and run the `app` configuration on Android 8+.

> Android 13+ requires notification permission. Exact alarm access is used only for user-facing reminders; the app falls back gracefully when exact-alarm access is unavailable.
