# Palette's Journal

## 2024-05-22 - Semantic States for Custom Accordions
**Learning:** Custom accordion headers using `clickable` rows need explicit `stateDescription` (e.g., "Expanded"/"Collapsed"). Simply adding `Role.Button` isn't enough because it doesn't convey the current state of the toggle to screen reader users, leaving them guessing if the section is open or closed.
**Action:** Always pair `Role.Button` with `stateDescription` when building custom expand/collapse components in Compose.
