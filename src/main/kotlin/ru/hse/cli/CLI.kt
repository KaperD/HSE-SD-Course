package ru.hse.cli

import java.io.InputStream
import java.io.OutputStream

interface CLI {
    val inputStream: InputStream
    val outputStream: OutputStream
    val errorStream: OutputStream

    fun getLine(): String?
    fun showMessage(message: String)
}
