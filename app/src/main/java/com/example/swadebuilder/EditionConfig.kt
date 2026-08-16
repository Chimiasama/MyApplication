package com.example.swadebuilder

object EditionConfig {
    val isFullEdition: Boolean
        get() = BuildConfig.FLAVOR == "full"
}
