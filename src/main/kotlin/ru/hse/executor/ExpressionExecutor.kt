package ru.hse.executor

import ru.hse.executable.ExecutionResult
import java.io.InputStream
import java.io.OutputStream

/**
 * Отвечает за исполнение выражений
 */
interface ExpressionExecutor {
    /**
     * @param expression выражение, которое нужно исполнить
     * @param input откуда можно брать ввод
     * @param output куда можно направлять вывод
     * @param error куда можно направлять сообщения об ошибках
     * @return результат выполнения выражения
     */
    fun execute(expression: String, input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult
}
