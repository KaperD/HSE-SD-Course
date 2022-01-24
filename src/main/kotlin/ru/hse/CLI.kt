package ru.hse

import java.io.InputStream
import java.io.OutputStream

interface CLI {
    fun readExpression(): String
    fun getInputStream(): InputStream
    fun getOutputStream(): OutputStream
    fun getErrorStream(): OutputStream
    fun showMessage(message: String)
}