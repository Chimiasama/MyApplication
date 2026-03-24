#!/bin/bash
cat << 'KOTLIN_EOF' > app/src/test/java/com/example/swadebuilder/model/TempTest.kt
package com.example.swadebuilder.model

import com.example.swadebuilder.CriadorState
import org.junit.Test

class TempTest {
    @Test
    fun test() {
        println("TEMP TEST")
    }
}
KOTLIN_EOF
