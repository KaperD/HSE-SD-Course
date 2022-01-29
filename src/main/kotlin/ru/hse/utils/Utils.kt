package ru.hse.utils

import ru.hse.charset.HseshCharsets
import java.io.OutputStream

fun OutputStream.write(message: String?) {
    message?.let { write(it.toByteArray(HseshCharsets.default)) }
}

fun OutputStream.writeln(message: String?) {
    message?.let {
        write(it.toByteArray(HseshCharsets.default))
        write("\n".toByteArray(HseshCharsets.default))
    }
}

fun <T> failure(message: String): Result<T> {
    return Result.failure(RuntimeException(message))
}
