package ru.hse.utils

import ru.hse.charset.HseshCharsets
import java.io.OutputStream
import java.lang.System.lineSeparator

/**
 * Записывает сообщение в OutputStream, с необходимой кодировкой
 */
fun OutputStream.write(message: String?) {
    message?.let { write(it.toByteArray(HseshCharsets.default)) }
}

/**
 * Записывает сообщение с переводом строки после него в OutputStream, с необходимой кодировкой
 */
fun OutputStream.writeln(message: String?) {
    message?.let {
        write(it.toByteArray(HseshCharsets.default))
        write(lineSeparator().toByteArray(HseshCharsets.default))
    }
}

/**
 * Создаёт Result со сбоем и переданным сообщением
 */
fun <T> failure(message: String): Result<T> {
    return Result.failure(RuntimeException(message))
}

/**
 * Замена стандартного trimMargin.
 * Необходим, так как стандартная реализация делает перенос строки с помощью \n даже на Windows, что некорректно
 */
fun String.trimMarginCrossPlatform(marginPrefix: String = "|"): String {
    return trimMargin(marginPrefix).replace("\n", lineSeparator())
}

/**
 * Замена стандартный trimIndent.
 * Необходим, так как стандартная реализация перенос строки с помощью \n даже на Windows, что некорректно
 */
fun String.trimIndentCrossPlatform(): String {
    return trimIndent().replace("\n", lineSeparator())
}
