package ru.hse.utils

import ru.hse.charset.HseshCharsets
import java.io.OutputStream

fun OutputStream.write(message: String?) {
    message?.let { write(it.toByteArray(HseshCharsets.default)) }
}
