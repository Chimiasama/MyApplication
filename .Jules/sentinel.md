# Sentinel's Journal

## 2025-02-14 - Cleartext Traffic Allowed by Default
**Vulnerability:** The application did not explicitly forbid cleartext traffic (HTTP), and supports a minimum SDK of 25 (Android 7.1). On Android versions below 9.0 (API 28), cleartext traffic is permitted by default.
**Learning:** Even if an application doesn't currently make network requests, missing the `android:usesCleartextTraffic="false"` attribute leaves a "door open" for future dependencies or features to inadvertently use insecure connections, or for the app to be vulnerable on older devices if it ever does network operations.
**Prevention:** Always explicitly set `android:usesCleartextTraffic="false"` in the `AndroidManifest.xml` for all Android applications unless there is a specific, justified need for HTTP. This enforces defense-in-depth.

## 2025-02-14 - Path Traversal in Portrait Loading
**Vulnerability:** Even if file creation uses safe names (UUIDs), reading filenames from persisted state (JSON) without validation allows Path Traversal if the state file is tampered with (integrity check was non-cryptographic).
**Learning:** Always use `SecurityUtils.getSafeChildFile` (or equivalent canonical path check) when constructing `File` objects from string paths, even if those strings originated from the app itself previously.
**Prevention:** Enforce usage of `SecurityUtils.getSafeChildFile` for all file access involving dynamic paths.
