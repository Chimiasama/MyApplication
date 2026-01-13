# Sentinel's Journal

## 2025-02-14 - Cleartext Traffic Allowed by Default
**Vulnerability:** The application did not explicitly forbid cleartext traffic (HTTP), and supports a minimum SDK of 25 (Android 7.1). On Android versions below 9.0 (API 28), cleartext traffic is permitted by default.
**Learning:** Even if an application doesn't currently make network requests, missing the `android:usesCleartextTraffic="false"` attribute leaves a "door open" for future dependencies or features to inadvertently use insecure connections, or for the app to be vulnerable on older devices if it ever does network operations.
**Prevention:** Always explicitly set `android:usesCleartextTraffic="false"` in the `AndroidManifest.xml` for all Android applications unless there is a specific, justified need for HTTP. This enforces defense-in-depth.
