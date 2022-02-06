package ru.hse.executor

import ru.hse.environment.Environment
import ru.hse.executable.ExecutionResult
import ru.hse.executable.ExecutionResult.Companion.fail
import ru.hse.executable.ExecutionResult.Companion.success
import ru.hse.factory.CommandFactory
import ru.hse.factory.PipeFactory
import ru.hse.parser.AssignmentParser
import ru.hse.parser.PipeParser
import ru.hse.utils.writeln
import java.io.InputStream
import java.io.OutputStream

/**
 * Сначала проверяет, является ли выражение присваиванием. Если да, то записывает значение переменной
 * Если это не присваивание, то проверяет пайп ли это. Если да, то исполняет его
 * Если это не присваивание и не пайп, то возвращает ошибку
 */
class ExpressionExecutorImpl(
    private val assignmentParser: AssignmentParser,
    private val pipeParser: PipeParser,
    private val environment: Environment,
    private val commandFactory: CommandFactory,
    private val pipeFactory: PipeFactory
) : ExpressionExecutor {
    override fun execute(
        expression: String,
        input: InputStream,
        output: OutputStream,
        error: OutputStream
    ): ExecutionResult {
        val resultAssignmentParser = assignmentParser.tryParse(expression)
        if (resultAssignmentParser.isSuccess) {
            val key = resultAssignmentParser.getOrThrow().first
            val value = resultAssignmentParser.getOrThrow().second
            environment.set(key, value)
            return success
        }

        val resultPipeParser = pipeParser.tryParse(expression)

        return when {
            resultPipeParser.isSuccess -> {
                val pipe = pipeFactory.create(resultPipeParser.getOrThrow().map { commandFactory.create(it) })
                pipe.run(input, output, error)
            }
            else -> {
                error.writeln(resultPipeParser.exceptionOrNull()?.message)
                fail
            }
        }
    }
}
