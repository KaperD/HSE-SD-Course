package ru.hse.parser

import ru.hse.splitter.PipeSplitter
import ru.hse.substitutor.Substitutor
import ru.hse.tokenizer.Tokenizer

class PipeParser(
    private val tokenizer: Tokenizer,
    private val pipeSplitter: PipeSplitter,
    private val substitutor: Substitutor
) : Parser<List<List<String>>> {
    override fun parse(line: String): Result<List<List<String>>> {
        return substitute(split(tokenize(line)))
    }

    private fun tokenize(line: String): Result<List<String>> {
        return tokenizer.tokenize(line)
    }

    private fun split(tokens: Result<List<String>>): Result<List<List<String>>> {
        if (tokens.isFailure) {
            return Result.failure(tokens.exceptionOrNull()!!)
        }
        return pipeSplitter.split(tokens.getOrThrow())
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
