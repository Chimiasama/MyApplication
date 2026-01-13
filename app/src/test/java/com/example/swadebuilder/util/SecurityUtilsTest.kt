package com.example.swadebuilder.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class SecurityUtilsTest {

    @Test
    fun `getSafeChildFile returns correct file for valid input`() {
        val parent = File("temp").absoluteFile
        val fileName = "safe.txt"
        val result = SecurityUtils.getSafeChildFile(parent, fileName)

        assertEquals(File(parent, fileName), result)
    }

    @Test
    fun `getSafeChildFile throws for path traversal`() {
        val parent = File("temp").absoluteFile
        // Note: Java File behavior on .. depends on OS and canonization.
        // We simulate a path traversal attempt.
        // On Linux, File(parent, "../hack").canonicalPath resolves to parent's parent.

        // This test assumes canonical path resolution works standardly.
        val fileName = "../hack.txt"

        // Since we check for separators first, this should actually throw IllegalArgumentException
        // because ".." does not necessarily contain separators, but "../" does.
        // SecurityUtils checks: if (fileName.contains(File.separator) || fileName.contains("/") || fileName.contains("\\"))

        val ex = assertThrows(IllegalArgumentException::class.java) {
             SecurityUtils.getSafeChildFile(parent, "../hack.txt")
        }
        assertEquals("Nome de arquivo inválido: ../hack.txt", ex.message)
    }

    @Test
    fun `getSafeChildFile throws for filename with separators`() {
        val parent = File("temp")
        assertThrows(IllegalArgumentException::class.java) {
            SecurityUtils.getSafeChildFile(parent, "subdir/file.txt")
        }
    }

    // Testing the canonical path check requires bypassing the separator check or having a filesystem structure
    // where '..' resolves differently without explicit separators in the string passed?
    // Actually, if we pass "foo/../bar", it's caught by separator check.
    // If we pass "..", it's just a file named ".." effectively?
    // No, File(parent, "..") resolves to parent of parent.
    // Does ".." contain separators? No.

    @Test
    fun `getSafeChildFile throws SecurityException for double dot traversal without separators`() {
        val parent = File("temp/subdir").absoluteFile
        parent.mkdirs()

        // If we pass "..", File(parent, "..") is the parent of 'subdir', i.e., 'temp'.
        // Its canonical path is '.../temp'.
        // Parent canonical path is '.../temp/subdir'.
        // canonicalParent.parent == canonicalFile.
        // Our check: file.canonicalFile.parentFile != parentDir.canonicalFile
        // file.canonicalFile is 'temp'. parentFile of 'temp' is '...'.
        // parentDir.canonicalFile is 'subdir'.
        // So they are different. It should throw SecurityException.

        assertThrows(SecurityException::class.java) {
            SecurityUtils.getSafeChildFile(parent, "..")
        }

        parent.delete()
        File("temp").delete()
    }
}
