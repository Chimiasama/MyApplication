package com.example.swadebuilder.util

import com.example.swadebuilder.model.CustomContentItem
import com.example.swadebuilder.model.CustomContentPackage
import kotlinx.serialization.json.Json

sealed class CustomContentValidationResult {
    data object Valid : CustomContentValidationResult()
    data class Invalid(val reason: String) : CustomContentValidationResult()
}

class CustomContentManager(
    private val json: Json = Json { ignoreUnknownKeys = true; prettyPrint = true }
) {
    fun validateItem(item: CustomContentItem): CustomContentValidationResult {
        if (item.id.isBlank()) {
            return CustomContentValidationResult.Invalid("ID cannot be blank.")
        }
        if (!item.id.startsWith("custom:") && !item.id.startsWith("fanmade:")) {
            return CustomContentValidationResult.Invalid("Custom item ID must start with 'custom:' or 'fanmade:' namespace.")
        }
        if (item.name.isBlank()) {
            return CustomContentValidationResult.Invalid("Item name cannot be blank.")
        }
        if (item.description.isBlank()) {
            return CustomContentValidationResult.Invalid("Item description cannot be blank.")
        }
        return CustomContentValidationResult.Valid
    }

    fun validatePackage(pkg: CustomContentPackage): CustomContentValidationResult {
        if (pkg.packageId.isBlank()) {
            return CustomContentValidationResult.Invalid("Package ID cannot be blank.")
        }
        if (pkg.packageName.isBlank()) {
            return CustomContentValidationResult.Invalid("Package name cannot be blank.")
        }
        for (item in pkg.items) {
            val itemValidation = validateItem(item)
            if (itemValidation is CustomContentValidationResult.Invalid) {
                return CustomContentValidationResult.Invalid("Invalid item in package '${item.id}': ${itemValidation.reason}")
            }
        }
        return CustomContentValidationResult.Valid
    }

    fun exportPackageToJson(pkg: CustomContentPackage): String {
        return json.encodeToString(CustomContentPackage.serializer(), pkg)
    }

    fun importPackageFromJson(jsonString: String): Result<CustomContentPackage> {
        return try {
            val pkg = json.decodeFromString(CustomContentPackage.serializer(), jsonString)
            when (val validation = validatePackage(pkg)) {
                is CustomContentValidationResult.Valid -> Result.success(pkg)
                is CustomContentValidationResult.Invalid -> Result.failure(IllegalArgumentException(validation.reason))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
