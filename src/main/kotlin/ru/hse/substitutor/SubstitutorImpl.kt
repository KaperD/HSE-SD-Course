package ru.hse.substitutor

import ru.hse.environment.Environment
import ru.hse.validator.VarNameValidator

/**
 * Заменяет $varname на значение varname, если для $varname внешние кавычки двойные или их нет
 * Также убирает внешние кавычки
 */
class SubstitutorImpl(
    private val environment: Environment,
    private val varNameValidator: VarNameValidator
) : Substitutor {
    override fun substitute(token: String): String {
        val resultBuilder: StringBuilder = StringBuilder()

        var index = 0
        while (index < token.length) {
            when (token[index]) {
                DOUBLE_QUOTE -> index = doubleQuote(resultBuilder, token, index)
                SINGLE_QUOTE -> index = singleQuote(resultBuilder, token, index)
                DOLLAR -> index = processDollar(resultBuilder, token, index)
                else -> {
                    resultBuilder.append(token[index])
                    index += 1
                }
            }
        }

        return resultBuilder.toString()
    }

    private fun doubleQuote(resultBuilder: StringBuilder, token: String, startIndex: Int): Int {
        var index = startIndex + 1
        while (index < token.length) {
            when (token[index]) {
                DOUBLE_QUOTE -> return index + 1
                DOLLAR -> index = processDollar(resultBuilder, token, index)
                else -> {
                    resultBuilder.append(token[index])
                    index += 1
                }
            }
        }
        throw InternalError("Could not find a pair for a double quote in the token: $token")
    }

    private fun singleQuote(resultBuilder: StringBuilder, token: String, startIndex: Int): Int {
        var index = startIndex + 1
        while (index < token.length) {
            when (token[index]) {
                SINGLE_QUOTE -> return index + 1
                else -> {
                    resultBuilder.append(token[index])
                    index += 1
                }
            }
        }
        throw InternalError("Could not find a pair for a single quote in the token: $token")
    }

    private fun processDollar(resultBuilder: StringBuilder, token: String, startIndex: Int): Int {
        val index = startIndex + 1

        val varName = if (index < token.length) varNameValidator.nameFromBeginningIn(token.substring(index)) else ""

        if (varName.isEmpty()) {
            resultBuilder.append(DOLLAR)
        } else {
            resultBuilder.append(environment.get(varName))
        }

        return index + varName.length
    }

    companion object {
        const val DOUBLE_QUOTE = '"'
        const val SINGLE_QUOTE = '\''
        const val DOLLAR = '$'
    }
}
