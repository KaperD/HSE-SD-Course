package ru.hse.executor

import ru.hse.environment.Environment
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.executable.ExecutionResult.Companion.fail
import ru.hse.executable.ExecutionResult.Companion.success
import ru.hse.parser.AssignmentParser
import ru.hse.parser.PipeParser
import java.io.InputStream
import java.io.OutputStream

class ExpressionExecutorImpl(
    private val assignmentParser: AssignmentParser,
    private val pipeParser: PipeParser,
    private val environment: Environment,
    private val pipeExecutor: Executable
) : ExpressionExecutor {
    override fun execute(
        expression: String,
        input: InputStream,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        val resultAssignmentParser = assignmentParser.parse(expression)
        if (resultAssignmentParser.isSuccess) {
            val key = resultAssignmentParser.getOrThrow().first
            val value = resultAssignmentParser.getOrThrow().second
            environment.set(key, value)
            return success()
        }

        val resultPipeParser = pipeParser.parse(expression)

        return when {
            resultPipeParser.isSuccess -> pipeExecutor.run(input, output, error)
            else -> {
                error.write(resultPipeParser.exceptionOrNull()?.message!!.toByteArray())
                fail()
            }
        }
    }
}
