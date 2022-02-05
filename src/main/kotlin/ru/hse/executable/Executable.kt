package ru.hse.executable

import java.io.InputStream
import java.io.OutputStream

/**
 * Обозначает сущность, которую можно исполнить
 */
interface Executable {
    /**
     * @param input откуда можно брать ввод
     * @param output куда можно направлять вывод
     * @param error куда можно направлять сообщения об ошибках
     */
    fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult
}
