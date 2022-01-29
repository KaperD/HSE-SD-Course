package ru.hse

import ru.hse.cli.CLI
import ru.hse.executor.ExpressionExecutor

class HseshApplication(
    private val cli: CLI,
    private val expressionExecutor: ExpressionExecutor
) {
    fun run() {
        while (true) {
            val executionResult = expressionExecutor.execute(
                cli.getLine(),
                cli.inputStream,
                cli.outputStream,
                cli.errorStream
            )
            if (executionResult.needExit) {
                return
            }
        }
    }
}
