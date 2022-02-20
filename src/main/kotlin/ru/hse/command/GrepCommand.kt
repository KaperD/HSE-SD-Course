package ru.hse.command

import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.InputStream
import java.io.OutputStream
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * grep &lt;pattern&gt; &#91;file&#93; - Выводит строки из файла в которых встретился pattern.
 * Если файл не задан, то берет данные из стандартного потока ввода
 *
 * &lt;pattern&gt; всегда считается регулярным выражением
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
                Pattern.compile(pattern, Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE)
            } else {
                Pattern.compile(pattern)
            }
        } catch (e: PatternSyntaxException) {
            error.grepError("Pattern error: ${e.message}")
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
            process(input, output, error, pattern, numberOfLinesToPrintAfter)
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
            process(input, output, error, pattern, numberOfLinesToPrintAfter)
        }
        return if (success) ExecutionResult.success else ExecutionResult.fail
    }

    private fun process(
        input: InputStream,
        output: OutputStream,
        error: OutputStream,
        pattern: Pattern,
        numberOfLinesToPrintAfter: Int
    ): Boolean {
        val reader = input.bufferedReader(HseshCharsets.default)
        var numberOfLinesToPrint = 0
        for (line in reader.lineSequence()) {
            val wasFound = try {
                pattern.matcher(line).find()
            } catch (ignored: StackOverflowError) {
                error.grepError("Match error: pattern is too complex or line is too big")
                return false
            }
            if (wasFound) {
                numberOfLinesToPrint = 1 + numberOfLinesToPrintAfter
            }
            if (numberOfLinesToPrint > 0) {
                numberOfLinesToPrint--
                output.writeln(line)
            }
        }
        return true
    }

    private fun tryParseOptions(error: OutputStream): GrepOptions? {
        val grepOptions = Options()

        grepOptions.addOption("w", false, "Find entire word")
        grepOptions.addOption("i", false, "Make lookup non case-sensitive")
        grepOptions.addOption("A", true, "Set number of lines printed after a match")

        val cmdLineParser = DefaultParser()

        val cmdLine = try {
            cmdLineParser.parse(grepOptions, args.toTypedArray())
        } catch (e: ParseException) {
            error.grepError("Parse error: ${e.message}")
            return null
        }

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
