package com.example.swadebuilder.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.util.Base64

object SecurityHardening {

    // TODO: Developer must replace this with the SHA-256 hash of their release keystore
    // Use: keytool -list -v -keystore my-release-key.keystore
    private const val EXPECTED_SIGNATURE_HASH = ""

    /**
     * Executes all security checks.
     * Returns true if the environment is considered safe.
     * Returns false if any threat is detected.
     */
    fun isSafe(context: Context): Boolean {
        if (isDebuggerAttached()) return false
        if (isRooted(context)) return false
        if (isHooked()) return false
        if (isEmulator()) return false
        // Only verify signature if a hash is provided (Production mode)
        if (EXPECTED_SIGNATURE_HASH.isNotBlank() && !verifySignature(context)) return false
        return true
    }

    @Suppress("DEPRECATION")
    private fun verifySignature(context: Context): Boolean {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                packageInfo.signatures
            }

            signatures?.any { signature ->
                val digest = MessageDigest.getInstance("SHA-256")
                val hash = digest.digest(signature.toByteArray())
                val encoded = Base64.encodeToString(hash, Base64.NO_WRAP)
                encoded == EXPECTED_SIGNATURE_HASH
            } ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected() || Debug.waitingForDebugger()
    }

    private fun isRooted(context: Context): Boolean {
        // 1. Check Build Tags
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // 2. Check for SU binary
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }

        // 3. Exec SU
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val inReader = BufferedReader(InputStreamReader(process.inputStream))
            if (inReader.readLine() != null) return true
        } catch (t: Throwable) {
            // ignore
        } finally {
            process?.destroy()
        }

        return false
    }

    private fun isHooked(): Boolean {
        return try {
            throw Exception("Stack trace check")
        } catch (e: Exception) {
            e.stackTrace.any { element ->
                val className = element.className
                className.contains("de.robv.android.xposed") ||
                className.contains("com.saurik.substrate") ||
                className.contains("com.android.internal.os.ZygoteInit") && element.methodName == "main"
            }
        }
    }

    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator")
    }
}
