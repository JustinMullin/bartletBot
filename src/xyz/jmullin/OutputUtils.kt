package xyz.jmullin

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

object OutputUtils {
    fun write(path: String, contents: String) {
        val writer = PrintWriter(FileOutputStream(File(path)))
        writer.println(contents)
        writer.close()
        writer.flush()
    }
}