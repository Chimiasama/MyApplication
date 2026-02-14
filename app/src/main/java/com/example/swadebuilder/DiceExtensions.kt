package com.example.swadebuilder

fun Int.toDiceString(): String =
    if (this == 0) "-" else if (this <= 12) "d$this" else "d12+${(this - 12)}"
