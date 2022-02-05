package ru.hse

import ru.hse.cli.CLI
import ru.hse.executor.ExpressionExecutor

/**
 * Отвечает за считывание выражений и их исполнение
 */
class HseshApplication(
    private val cli: CLI,
    private val expressionExecutor: ExpressionExecutor
) {
    fun run() {
        while (true) {
            val line = cli.getLine() ?: return
            if (runExpression(line)) {
                return
            }
        }
    }

    private fun runExpression(expression: String): Boolean {
        if (expression.isBlank()) {
            return false
        }
        val executionResult = expressionExecutor.execute(
            expression,
            cli.inputStream,
            cli.outputStream,
            cli.errorStream
        )
        return executionResult.needExit
    }
}
