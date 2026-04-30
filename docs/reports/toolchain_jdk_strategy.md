# Toolchain/JDK Strategy

## Objective
Ensure deterministic builds across local dev and CI while keeping fast local iteration.

## Adopted corrections
- Keep source/target and Kotlin bytecode at Java 21.
- Remove hard `jvmToolchain(21)` lock from module build so environments with a newer installed JDK (e.g., JDK 25) can still build.
- Enable Gradle toolchain auto-download (`org.gradle.java.installations.auto-download=true`) for environments that do require a specific JDK.

## Recommended operating model
1. **CI release lane (strict):** pin JDK 21 (Temurin 21) explicitly.
2. **CI validation lane (compat):** run with latest supported LTS+ (e.g., JDK 25) to detect ecosystem drift.
3. **Developers:** default to installed JDK if compatible; fallback to auto-download where needed.
4. **Optional strict local mode:** use `-PenforceJdk21=true` in future if strict parity is needed.

## Commands
- Normal local build:
  - `./gradlew test`
- Fast local build without lint (when explicitly desired):
  - `./gradlew test -PdisableLint=true`

## Rationale
- Avoids hard failures when only newer JDKs are installed.
- Preserves Java 21 bytecode target for Android toolchain consistency.
- Keeps lint available by default, preventing silent quality regressions.
