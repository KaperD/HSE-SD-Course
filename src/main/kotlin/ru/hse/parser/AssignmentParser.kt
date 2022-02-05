package ru.hse.parser

import ru.hse.substitutor.Substitutor
import ru.hse.tokenizer.Tokenizer
import ru.hse.utils.failure
import ru.hse.validator.VarNameValidator

/**
 * Отвечает за парсинг присваиваний
 */
class AssignmentParser(
    private val varNameValidator: VarNameValidator,
    private val tokenizer: Tokenizer,
    private val substitutor: Substitutor
) : Parser<Pair<String, String>> {
    /**
     * Проверяет, является ли выражение присваиванием
     * Если является, то возвращает готовые к использованию название и значение переменной
     */
    override fun parse(line: String): Result<Pair<String, String>> {
        val trimmedLine = line.trim()

        val positionOfEqualSign = trimmedLine.indexOf('=')
        if (positionOfEqualSign < 0) {
            return failure("No equals sign")
        }
        val varName = trimmedLine.substring(0, positionOfEqualSign)
        val expression = trimmedLine.substring(positionOfEqualSign + 1)

        return when {
            !checkVarName(varName) -> failure("Invalid variable name")
            !checkExpression(expression) -> failure("Invalid expression")
            else -> Result.success(Pair(varName, substitutor.substitute(expression)))
        }
    }

    private fun checkVarName(varName: String): Boolean {
        return varNameValidator.check(varName)
    }

    private fun checkExpression(expression: String): Boolean {
        if (expression.isNotEmpty() && expression.first().isWhitespace()) {
            return false
        }
        val tokens = tokenizer.tokenize(expression)
        return tokens.isSuccess && tokens.getOrThrow().size <= 1
    }
}
