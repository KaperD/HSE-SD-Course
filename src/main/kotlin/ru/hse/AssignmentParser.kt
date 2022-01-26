package ru.hse

class AssignmentParser(
    private val varNameValidator: VarNameValidator,
    private val tokenizer: Tokenizer
) : Parser<Pair<String, String>> {
    override fun parse(line: String): Result<Pair<String, String>> {
        val positionOfEqualSign = line.indexOf('=')

        val varName = line.substring(0, positionOfEqualSign)
        if (!varNameValidator.check(varName)) {
            return Result.failure(RuntimeException("Invalid variable name"))
        }

        val expression = line.substring(positionOfEqualSign + 1)
        val result = tokenizer.tokenize(expression)
        if (result.isFailure || result.getOrThrow().size > 1 ||
            (result.getOrThrow().size == 1 && expression[0].isWhitespace())
        ) {
            return Result.failure(RuntimeException("Invalid expression"))
        }

        return Result.success(Pair(varName, expression.trim()))
    }
}
