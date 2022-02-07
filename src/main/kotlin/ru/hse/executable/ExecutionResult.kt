package ru.hse.executable

/**
 * Результат исполнения
 * @param exitCode 0 — не было ошибок; отличный от 0 — были ошибки
 * @param needExit true, если hsesh нужно завершить работу
 */
data class ExecutionResult(val exitCode: Int, val needExit: Boolean) {
    companion object {
        val success: ExecutionResult = ExecutionResult(0, false)
        val fail: ExecutionResult = ExecutionResult(1, false)
        val exit: ExecutionResult = ExecutionResult(0, true)
    }
}
