package ru.hse.executable

data class ExecutionResult(val exitCode: Int, val needExit: Boolean) {
    companion object {
        fun success(needExit: Boolean): ExecutionResult {
            return ExecutionResult(0, needExit)
        }
        fun fail(needExit: Boolean): ExecutionResult {
            return ExecutionResult(1, needExit)
        }
    }
}
