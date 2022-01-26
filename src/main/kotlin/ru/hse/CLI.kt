package ru.hse

import java.io.InputStream
import java.io.OutputStream

interface CLI {
    fun getLine(): String
    fun showMessage(message: String)
    fun getInputStream(): InputStream
    fun getOutputStream(): OutputStream
    fun getErrorStream(): OutputStream
}
