package com.example.swadebuilder.util

fun Int.toDiceString(): String =
    if (this <= 12) "d$this" else "d12+${(this - 12)}"
