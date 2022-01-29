package ru.hse.executable

data class ExecutionResult(val exitCode: Int, val needExit: Boolean) {
    companion object {
        fun success(): ExecutionResult {
            return ExecutionResult(0, false)
        }
        fun fail(): ExecutionResult {
            return ExecutionResult(1, false)
        }
        fun exit(): ExecutionResult {
            return ExecutionResult(0, true)
        }
    }
}
