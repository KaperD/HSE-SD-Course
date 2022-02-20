package ru.hse.command

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * grep pattern [file] - Выводит строки из файла в которых встретился pattern.
 * Если файл не задан, то берет данные из стандартного потока ввода
 *
 * pattern всегда считается регулярным выражением
 *
 * Доступные флаги
 * - -w — поиск только слова целиком
 * - -i — регистронезависимый (case-insensitive) поиск
 * - -A — следующее за -A число говорит, сколько строк после совпадения надо распечатать
 */
class GrepCommand(private val args: List<String>) : IOCommand, Executable {
    override val commandName: String
        get() = "grep"

    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        val options = tryParseOptions(error)
        val pattern = options?.let { buildPattern(it.patternString, it.matchFullWord, it.caseInsensitive, error) }
        if (options == null || pattern == null) {
            return ExecutionResult.fail
        }
        return when (options.inputFilename) {
            null -> processEmptyFile(input, output, error, pattern, options.numberOfLinesToPrintAfter)
            else -> processFile(options.inputFilename, output, error, pattern, options.numberOfLinesToPrintAfter)
        }
    }

    private fun buildPattern(
        patternString: String,
        matchFullWord: Boolean,
        caseInsensitive: Boolean,
        error: OutputStream
    ): Pattern? {
        val pattern = if (matchFullWord) """\b(?=\w)$patternString\b(?<=\w)""" else patternString
        return try {
            return if (caseInsensitive) {
                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
            } else {
                Pattern.compile(pattern)
            }
        } catch (e: PatternSyntaxException) {
            error.grepError("Pattern syntax error: ${e.message}")
            null
        } catch (ignored: StackOverflowError) {
            error.grepError("Pattern compile error: pattern is too complex to compile")
            null
        }
    }

    private fun processEmptyFile(
        input: InputStream,
        output: OutputStream,
        error: OutputStream,
        pattern: Pattern,
        numberOfLinesToPrintAfter: Int
    ): ExecutionResult {
        val success = safeIO(error) {
            process(input, output, pattern, numberOfLinesToPrintAfter)
        }
        return if (success) ExecutionResult.success else ExecutionResult.fail
    }

    private fun processFile(
        fileName: String,
        output: OutputStream,
        error: OutputStream,
        pattern: Pattern,
        numberOfLinesToPrintAfter: Int
    ): ExecutionResult {
        val success = readFile(fileName, error) { input ->
            process(input, output, pattern, numberOfLinesToPrintAfter)
        }
        return if (success) ExecutionResult.success else ExecutionResult.fail
    }

    private fun process(
        input: InputStream,
        output: OutputStream,
        pattern: Pattern,
        numberOfLinesToPrintAfter: Int
    ) {
        val reader = input.bufferedReader(HseshCharsets.default)
        var numberOfLinesToPrint = 0
        reader.forEachLine {
            if (pattern.matcher(it).find()) {
                numberOfLinesToPrint = 1 + numberOfLinesToPrintAfter
            }
            if (numberOfLinesToPrint > 0) {
                numberOfLinesToPrint--
                output.writeln(it)
            }
        }
    }

    private fun tryParseOptions(error: OutputStream): GrepOptions? {
        val grepOptions = Options()

        grepOptions.addOption("w", false, "Find entire word")
        grepOptions.addOption("i", false, "Make lookup non case-sensitive")
        grepOptions.addOption("A", true, "Set number of lines printed after a match")

        val cmdLineParser = DefaultParser()

        val cmdLine = cmdLineParser.parse(grepOptions, args.toTypedArray())

        val numberOfLinesToPrintAfter: String? = cmdLine.getOptionValue("A")
        return if (cmdLine.argList.getOrNull(0) == null) {
            error.grepError("Argument error: pattern is not specified")
            null
        } else if (numberOfLinesToPrintAfter != null && numberOfLinesToPrintAfter.toIntOrNull() == null) {
            error.grepError("Argument error: -A argument should be a number, but found $numberOfLinesToPrintAfter")
            null
        } else if (numberOfLinesToPrintAfter != null && numberOfLinesToPrintAfter.toInt() < 0) {
            error.grepError("Argument error: -A argument should be non negative, but found $numberOfLinesToPrintAfter")
            null
        } else {
            GrepOptions(
                cmdLine.hasOption("w"),
                cmdLine.hasOption("i"),
                numberOfLinesToPrintAfter?.toInt() ?: 0,
                cmdLine.argList[0],
                cmdLine.argList.getOrNull(1)
            )
        }
    }

    private data class GrepOptions(
        val matchFullWord: Boolean,
        val caseInsensitive: Boolean,
        val numberOfLinesToPrintAfter: Int,
        val patternString: String,
        val inputFilename: String?
    )

    private fun OutputStream.grepError(message: String) {
        writeln("$commandName: $message")
    }
}
