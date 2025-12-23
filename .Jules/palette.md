## 2024-05-23 - Accessibility in Custom Accordions
**Learning:** Even simple toggle components like accordions require explicit `stateDescription` semantics to be fully accessible. Just using `clickable` and an icon change isn't enough for screen reader users to know if a section is expanded or collapsed.
**Action:** Always add `semantics { stateDescription = ... }` and `role = Role.Button` to custom toggleable UI elements, utilizing localized strings for the state description.
