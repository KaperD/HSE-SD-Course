package ru.hse.command

import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.cli.*
import ru.hse.utils.writeln
import java.io.BufferedInputStream
import java.util.*
import java.util.regex.Pattern

/**
 * @brief grep <pattern> <filename> - Считает число вхождений <pattern> в <filename>
 *
 * @note <pattern> всегда считается регулярным выражением
 *
 * Доступные флаги
 *  -w -
 */
class GrepCommand(private val args: List<String>) : IOCommand, Executable {
    override val commandName: String
        get() = "grep"

    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        val grepOptions = Options()

        grepOptions.addOption("w", false, "Find entire word")
        grepOptions.addOption("i", false, "Make lookup non case-sensitive")
        grepOptions.addOption("A", true, "Set number of lines printed after a match")

        val cmdLineParser = DefaultParser()

        val cmdLine = cmdLineParser.parse(grepOptions, args.toTypedArray())

        return try {
            val matchFullWord = cmdLine.hasOption("w")
            val caseNonSensitive = cmdLine.hasOption("i")
            val numberOfPrintedLines = cmdLine.getOptionValue("A")?.toInt() ?: 1
            val patternString = cmdLine.args.getOrNull(0) ?: throw GrepArgumentException("pattern is not specified")
            val inputFilename = cmdLine.args.getOrNull(1) ?: throw GrepArgumentException("input file is not specified")

            val pattern = buildPattern(patternString, caseNonSensitive)
            readFile(inputFilename, error) { inputStream ->
                matchedLines(inputStream, pattern, matchFullWord, numberOfPrintedLines).forEach {
                    output.writeln(it)
                }
            }
            ExecutionResult.success
        } catch (e: GrepArgumentException) {
            error.writeln(e.message)
            ExecutionResult.fail
        }
    }

    private fun buildPattern(patternString: String, caseNonSensitive: Boolean): Pattern {
        return if (caseNonSensitive)
            Pattern.compile(patternString, Pattern.CASE_INSENSITIVE)
        else
            Pattern.compile(patternString)
    }

    private fun matchedLines(
        input: InputStream,
        pattern: Pattern,
        matchWord: Boolean,
        nLines: Int
    ) = sequence {
        if (nLines < 1) {
            throw GrepArgumentException("number of printed lines can not be less than 1")
        }

        var prevMatchFov = 0
        val scanner = Scanner(BufferedInputStream(input))

        for (currentLineI in 0..Int.MAX_VALUE) {
            if (!scanner.hasNextLine()) break

            val line = scanner.nextLine()
            val sources = if (matchWord) line.split("\\s+".toPattern()) else listOf(line)
            val anyMatched = sources
                .stream()
                .anyMatch { pattern.matcher(it).find() }
            if (anyMatched) {
                prevMatchFov = nLines
            }
            if (prevMatchFov > 0) {
                yield(line)
                prevMatchFov -= 1
            }
        }
    }

    class GrepArgumentException(e: String) : Exception("grep: Argument error: $e")
}
