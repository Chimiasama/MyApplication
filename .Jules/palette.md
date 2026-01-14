## 2024-05-23 - Accessibility in Custom Accordions
**Learning:** Even simple toggle components like accordions require explicit `stateDescription` semantics to be fully accessible. Just using `clickable` and an icon change isn't enough for screen reader users to know if a section is expanded or collapsed.
**Action:** Always add `semantics { stateDescription = ... }` and `role = Role.Button` to custom toggleable UI elements, utilizing localized strings for the state description.

## 2024-05-24 - Touch Targets in Inline Chips
**Learning:** Custom "chip-like" rows often suffer from small touch targets (e.g., 18dp icons) that violate accessibility standards.
**Action:** Replace custom implementations with standard Material3 `InputChip` or `SuggestionChip`, leveraging the component's built-in min-height and semantics, and map complex actions (Edit/Remove) to the chip body and trailing icon respectively.
