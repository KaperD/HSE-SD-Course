package ru.hse.parser

import ru.hse.splitter.PipeSplitter
import ru.hse.substitutor.Substitutor
import ru.hse.tokenizer.Tokenizer

/**
 * Отвечает за парсинг пайпов
 */
class PipeParser(
    private val tokenizer: Tokenizer,
    private val pipeSplitter: PipeSplitter,
    private val substitutor: Substitutor
) : Parser<List<List<String>>> {
    /**
     * Проверяет, является ли выражение пайпом
     * Если является, то возвращает список команд (команды в виде списка токенов готовых к использованию)
     */
    override fun tryParse(line: String): Result<List<List<String>>> {
        return substitute(split(tokenize(line)))
    }

    private fun tokenize(line: String): Result<List<String>> {
        return tokenizer.tryTokenize(line)
    }

    private fun split(tokens: Result<List<String>>): Result<List<List<String>>> {
        if (tokens.isFailure) {
            return Result.failure(tokens.exceptionOrNull()!!)
        }
        return pipeSplitter.trySplit(tokens.getOrThrow())
    }

    private fun substitute(commandsTokens: Result<List<List<String>>>): Result<List<List<String>>> {
        if (commandsTokens.isFailure) {
            return Result.failure(commandsTokens.exceptionOrNull()!!)
        }
        return Result.success(commandsTokens.getOrThrow().map { substituteList(it) })
    }

    private fun substituteList(tokens: List<String>): List<String> {
        return tokens.map { substitutor.substitute(it) }
    }
}
