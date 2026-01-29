package com.example.swadebuilder.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import android.util.Log
import java.io.File
import java.security.MessageDigest

object SecurityHardening {

    // Placeholder: Replace with actual SHA-256 of your release key
    // Run: keytool -list -v -keystore release.jks -alias key0
    // Example: "A1:B2:..." (remove colons and lowercase)
    private const val EXPECTED_SIGNATURE_HASH = "0000000000000000000000000000000000000000000000000000000000000000"

    fun integrityCheck(context: Context) {
        if (checkDebuggable(context)) throw SecurityException("Debugger detectado ou App Debuggable.")
        if (checkRoot()) throw SecurityException("Ambiente Root detectado.")
        if (checkEmulator()) throw SecurityException("Execução em emulador não permitida.")

        // Only check signature if NOT a debug build to allow development
        if (!isDebugBuild(context)) {
             // In a real scenario, you would uncomment this once you have the hash
             // if (!checkSignature(context)) throw SecurityException("Assinatura inválida/adulterada.")
        }
    }

    private fun isDebugBuild(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun checkDebuggable(context: Context): Boolean {
        // If it's a dev build, allow debugging
        if (isDebugBuild(context)) return false

        if (Debug.isDebuggerConnected()) return true
        return false
    }

    private fun checkRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        val tags = Build.TAGS
        if (tags != null && tags.contains("test-keys")) return true
        return false
    }

    private fun checkEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    private fun checkSignature(context: Context): Boolean {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            signatures?.forEach { signature ->
                val md = MessageDigest.getInstance("SHA-256")
                md.update(signature.toByteArray())
                val digest = md.digest()
                val hexString = digest.joinToString("") { "%02x".format(it) }
                if (hexString.equals(EXPECTED_SIGNATURE_HASH, ignoreCase = true)) {
                    return true
                }
            }
        } catch (e: Exception) {
            // Log.e("Security", "Erro ao verificar assinatura", e)
        }
        return false
    }
}
