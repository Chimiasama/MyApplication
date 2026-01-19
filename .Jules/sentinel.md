# Sentinel's Journal

## 2025-02-14 - Cleartext Traffic Allowed by Default
**Vulnerability:** The application did not explicitly forbid cleartext traffic (HTTP), and supports a minimum SDK of 25 (Android 7.1). On Android versions below 9.0 (API 28), cleartext traffic is permitted by default.
**Learning:** Even if an application doesn't currently make network requests, missing the `android:usesCleartextTraffic="false"` attribute leaves a "door open" for future dependencies or features to inadvertently use insecure connections, or for the app to be vulnerable on older devices if it ever does network operations.
**Prevention:** Always explicitly set `android:usesCleartextTraffic="false"` in the `AndroidManifest.xml` for all Android applications unless there is a specific, justified need for HTTP. This enforces defense-in-depth.

## 2025-02-14 - Path Traversal in Portrait Loading
**Vulnerability:** Even if file creation uses safe names (UUIDs), reading filenames from persisted state (JSON) without validation allows Path Traversal if the state file is tampered with (integrity check was non-cryptographic).
**Learning:** Always use `SecurityUtils.getSafeChildFile` (or equivalent canonical path check) when constructing `File` objects from string paths, even if those strings originated from the app itself previously.
**Prevention:** Enforce usage of `SecurityUtils.getSafeChildFile` for all file access involving dynamic paths.

## 2025-02-14 - Input Validation for User-Defined Filenames
**Vulnerability:** The application allowed arbitrary strings for filenames in the Save Dialog, relying on backend exceptions to catch invalid paths. This could lead to crashes or confusing errors, and potentially allowed excessively long filenames or reserved characters.
**Learning:** Validating input at the UI layer (using `OutlinedTextField`'s `isError` and `supportingText`) provides a better user experience and reduces the attack surface by filtering out malformed data early. Using a strict whitelist (alphanumeric + `_.-`) for filenames is safer than a blacklist.
**Prevention:** Implement `isValidFilename` and `sanitizeFilename` utilities and integrate them into UI input fields. Always pre-fill and validate filenames before attempting file operations.

## 2025-02-14 - Insecure File Sharing (Least Privilege & Race Condition)
**Vulnerability:** The application saved generated PDFs to the root of External Storage (`getExternalFilesDir(null)`) with a static filename (`ficha_preenchida.pdf`). This exposed the file to race conditions (overwriting before sharing completes) and violated the Principle of Least Privilege by exposing the entire external files root via FileProvider.
**Learning:** Using `context.cacheDir` is safer for temporary files to be shared, as it is internal to the app and managed by the OS. Dynamic filenames prevent data leakage between operations.
**Prevention:** Use `cache-path` in `file_paths.xml` with a specific subdirectory. Generate unique or sanitized filenames for shared content.
