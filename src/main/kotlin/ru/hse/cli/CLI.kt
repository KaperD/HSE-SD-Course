package ru.hse.cli

import java.io.InputStream
import java.io.OutputStream

/**
 * Отвечает за ввод/вывод
 */
interface CLI {
    /**
     * Поток ввода
     */
    val inputStream: InputStream
    /**
     * Поток вывода
     */
    val outputStream: OutputStream
    /**
     * Поток ошибок
     */
    val errorStream: OutputStream

    /**
     * @return следующую строку из inputStream или null, если достигнут конец потока
     */
    fun getLine(): String?

    /**
     * Вывести сообщение в outputStream
     */
    fun showMessage(message: String)
}
