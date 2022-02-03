package ru.hse.command

import ru.hse.command.flags.FlagParser
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.write
import ru.hse.utils.writeln
import java.io.*
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.collections.ArrayList
import kotlin.io.path.*


class WcCommand(private val args: List<String>, private val padding: Int) : Executable {
    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        val flagParser = FlagParser()

        val flagBytes = flagParser.addBoolFlag('c')
        val flagLines = flagParser.addBoolFlag('l')
        val flagChars = flagParser.addBoolFlag('m')
        val flagWords = flagParser.addBoolFlag('w')

        val fileArgs = flagParser.addNonRecognized("input files", 0..Int.MAX_VALUE)

        flagParser.parseArgs(args)

        val charset = StandardCharsets.UTF_8

        val inputPaths = fileArgs.valueCast<List<String>>()
            ?.map { Path(it) }
            ?: emptyList()

        for (path in inputPaths) {
            if (!path.exists()) {
                error.writeln("wc: $path: No such file or directory")
                return ExecutionResult.fail()
            }
        }

        val inputs = if (inputPaths.isEmpty()) {
            listOf(input to null)
        } else {
            inputPaths.map { path -> path.inputStream() to path.toString() }
        }

        val anyDefined = Stream.of(flagBytes, flagLines, flagChars, flagWords).anyMatch { it.isDefined() }

        if (!anyDefined) {
            flagBytes.setDefault()
            flagLines.setDefault()
            flagWords.setDefault()
        }

        fun buildMetrics(): InputStreamMetric {
            val charMetrics = ArrayList<IterativeMetric<Int>>()
            val byteMetrics = ArrayList<IterativeMetric<Byte>>()

            if (flagBytes.isDefined()) byteMetrics.add(ByteCountFromStream())
            if (flagLines.isDefined()) charMetrics.add(LineCountMetric())
            if (flagChars.isDefined()) charMetrics.add(CharacterCountMetric())
            if (flagWords.isDefined()) charMetrics.add(WordCountMetric())

            return InputStreamMetricWrapper(charMetrics, byteMetrics, charset)
        }

        output.write(evaluateMetric({ buildMetrics() }, inputs, padding))

        return ExecutionResult.success()
    }

    interface AllMeasurementsResultsContainer<R> {
        fun allMeasurementsResult(): R
    }

    interface Metric<I, R, A> : AllMeasurementsResultsContainer<A> {
        fun measure(input: I): R
    }

    interface InputStreamMetric : Metric<InputStream, MetricResults, Unit> {
        override fun allMeasurementsResult() {}
    }

    interface IterativeMetric<T> : Metric<T, Unit, MetricResults> {
        fun name(): MetricName
        fun value(): Long

        override fun allMeasurementsResult() = sortedMapOf(name() to value())
    }

    open class PrimitivesCount<T>(
        private var name: MetricName,
        private var count: Long = 0
    ) :
        IterativeMetric<T> {
        override fun measure(input: T) {
            count += 1
        }

        override fun name() = name
        override fun value() = count
    }

    class ByteProcessingInputStream(
        private val wrappedStream: InputStream,
        private val byteConsumer: (Byte) -> Unit,
        private val finisher: () -> Unit
    ) : InputStream() {
        override fun read(): Int {
            val b = wrappedStream.read()
            if (b == -1) {
                finisher()
                return b
            }
            byteConsumer(b.toByte())
            return b
        }
    }

    class InputStreamMetricWrapper(
        private val characterMetrics: List<IterativeMetric<Int>>,
        private val byteMetrics: List<IterativeMetric<Byte>>,
        private val charset: Charset
    ) : InputStreamMetric {
        override fun measure(input: InputStream): MetricResults {
            val bufferedIS = BufferedInputStream(input)
            val byteProcessingIS = ByteProcessingInputStream(
                bufferedIS,
                { byte -> byteMetrics.forEach { it.measure(byte) } },
                {}
            )
            val charReaderIR = InputStreamReader(byteProcessingIS, charset)
            var ch: Int = charReaderIR.read()
            while (ch != -1) {
                characterMetrics.forEach { it.measure(ch) }
                ch = charReaderIR.read()
            }
            val allResults: MetricResults = TreeMap()
            characterMetrics.forEach { m -> allResults.joinResults(m.allMeasurementsResult()) }
            byteMetrics.forEach { m -> allResults.joinResults(m.allMeasurementsResult()) }
            return allResults
        }
    }

    class ByteCountFromStream() :
        PrimitivesCount<Byte>(MetricName.Bytes)

    class CharacterCountMetric() :
        PrimitivesCount<Int>(MetricName.Characters)

    class LineCountMetric(
        private var lineCount: Long = 0,
        private var prevCharacter: Int? = null
    ) : IterativeMetric<Int> {
        override fun measure(input: Int) {
            val charsBuffer = StringBuilder()
            prevCharacter?.let { charsBuffer.appendCodePoint(it) }
            charsBuffer.appendCodePoint(input)
            if (charsBuffer.toString().endsWith(System.lineSeparator())) {
                lineCount += 1
            }
            prevCharacter = input
        }

        override fun name() = MetricName.Lines

        override fun value() = lineCount
    }

    class WordCountMetric(
        private var wordCount: Long = 0,
        private var wasOnWhitespace: Boolean = true,
    ) : IterativeMetric<Int> {
        override fun measure(input: Int) {
            val nowOnWhitespace = Character.isWhitespace(input)
            if (wasOnWhitespace && !nowOnWhitespace) {
                wordCount += 1
            }
            wasOnWhitespace = nowOnWhitespace
        }

        override fun name() = MetricName.Words

        override fun value() = wordCount
    }
}

enum class MetricName {
    Lines,
    Words,
    Bytes,
    Characters,
}

typealias MetricResults = SortedMap<MetricName, Long>

fun MetricResults.joinResults(others: MetricResults) {
    for ((otherK, otherV) in others) {
        val curV = this.getOrDefault(otherK, 0)
        this[otherK] = curV + otherV
    }
}

fun MetricResults.formatResults(padding: Int, title: String?): String {
    val values = this.values
        .stream()
        .map { it.toString().padStart(padding) }
        .collect(Collectors.joining(" "))
    return title?.let { " $values $it" } ?: values
}

fun evaluateMetric(
    metricBuilder: () -> WcCommand.InputStreamMetric,
    inputs: List<Pair<InputStream, String?>>,
    padding: Int,
): String {
    val totalResults: MetricResults = TreeMap()
    val formattedResultsBuilder = StringBuilder()
    for ((input, inputName) in inputs) {
        val metric = metricBuilder()
        val inputResults: MetricResults = metric.measure(input)
        totalResults.joinResults(inputResults)
        formattedResultsBuilder.appendLine(inputResults.formatResults(padding, inputName))
    }
    if (inputs.size > 1) {
        formattedResultsBuilder.appendLine(totalResults.formatResults(padding, "total"))
    }
    return formattedResultsBuilder.toString()
}


fun main() {
    val builder = {
        WcCommand.InputStreamMetricWrapper(
            listOf(
                WcCommand.CharacterCountMetric(),
                WcCommand.LineCountMetric(),
                WcCommand.WordCountMetric()
            ),
            listOf(
                WcCommand.ByteCountFromStream()
            ),
            StandardCharsets.UTF_8
        )
    }
    println(evaluateMetric(builder, listOf(System.`in` to "stdin"), 7))
}
