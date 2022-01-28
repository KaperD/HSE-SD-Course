package ru.hse.parser

import ru.hse.validator.VarNameValidator
import ru.hse.tokenizer.Tokenizer

class AssignmentParser(
    private val varNameValidator: VarNameValidator,
    private val tokenizer: Tokenizer
) : Parser<Pair<String, String>> {
    override fun parse(line: String): Result<Pair<String, String>> {
        val trimmedLine = line.trim()

        val positionOfEqualSign = trimmedLine.indexOf('=')
        val varName = trimmedLine.substring(0, positionOfEqualSign)
        val expression = trimmedLine.substring(positionOfEqualSign + 1)

        return when {
            !checkVarName(varName) -> Result.failure(RuntimeException("Invalid variable name"))
            !checkExpression(expression) -> Result.failure(RuntimeException("Invalid expression"))
            else -> Result.success(Pair(varName, expression))
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
