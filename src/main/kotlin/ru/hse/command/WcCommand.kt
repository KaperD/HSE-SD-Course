package ru.hse.command

import ru.hse.charset.HseshCharsets
import ru.hse.executable.Executable
import ru.hse.executable.ExecutionResult
import ru.hse.utils.writeln
import java.io.*
import java.lang.System.lineSeparator
import java.util.*
import java.util.stream.Collectors

const val DEFAULT_PADDING = 7

/**
 * wc [file ...] — считает число переводов строки, слов и байт в файлах и выводит это в поток вывода
 * Если список файлов пуст, то считает эти же метрики в потоке ввода
 */
class WcCommand(private val args: List<String>, private val padding: Int = DEFAULT_PADDING) : IOCommand, Executable {
    override val commandName: String = "wc"

    override fun run(input: InputStream, output: OutputStream, error: OutputStream): ExecutionResult {
        fun buildMetrics(): InputStreamMetric {
            val charMetrics = listOf(LineCountMetric(), WordCountMetric())
            val byteMetrics = listOf(ByteCountFromStream())

            return InputStreamMetricWrapper(charMetrics, byteMetrics)
        }
        return when {
            args.isEmpty() -> processEmptyArgumentList(input, output, error) { buildMetrics() }
            else -> processNotEmptyArgumentList(output, error) { buildMetrics() }
        }
    }

    private fun processEmptyArgumentList(
        input: InputStream,
        output: OutputStream,
        error: OutputStream,
        metricBuilder: () -> InputStreamMetric
    ): ExecutionResult {
        val success = safeIO(error) {
            val metric = metricBuilder()
            val result = metric.measure(input)
            output.writeln(result.formatResults(null))
        }
        return if (success) ExecutionResult.success else ExecutionResult.fail
    }

    private fun processNotEmptyArgumentList(
        output: OutputStream,
        error: OutputStream,
        metricBuilder: () -> InputStreamMetric
    ): ExecutionResult {
        var allSuccessful = true
        val totalResults: MetricResults = TreeMap()
        for (fileName in args) {
            val success = readFile(fileName, error) {
                val metric = metricBuilder()
                val inputResults: MetricResults = metric.measure(it)
                totalResults.joinResults(inputResults)
                output.writeln(inputResults.formatResults(fileName))
            }
            if (!success) {
                allSuccessful = false
            }
        }
        if (args.size > 1) {
            output.writeln(totalResults.formatResults("total"))
        }
        return if (allSuccessful) ExecutionResult.success else ExecutionResult.fail
    }

    private fun MetricResults.formatResults(title: String?): String {
        val values = this.values
            .stream()
            .map { it.toString().padStart(padding) }
            .collect(Collectors.joining(" "))
        return title?.let { " $values $it" } ?: values
    }

    private interface AllMeasurementsResultsContainer<R> {
        fun allMeasurementsResult(): R
    }

    private interface Metric<I, R> {
        fun measure(input: I): R
    }

    private interface InputStreamMetric : Metric<InputStream, MetricResults>

    private interface IterativeMetric<T> :
        Metric<T, Unit>,
        AllMeasurementsResultsContainer<MetricResults> {
        fun name(): MetricName
        fun value(): Long

        override fun allMeasurementsResult() = sortedMapOf(name() to value())
    }

    private open class PrimitivesCount<T>(
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

    private class ByteProcessingInputStream(
        wrappedStream: InputStream,
        private val byteConsumer: (Byte) -> Unit,
        private val finisher: () -> Unit
    ) : FilterInputStream(wrappedStream) {
        override fun read(): Int {
            val b = super.read()
            if (b == -1) {
                finisher()
                return b
            }
            byteConsumer(b.toByte())
            return b
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            val res = super.read(b, off, len)
            if (res == -1) {
                finisher()
                return res
            }
            for (i in off until off + res) {
                byteConsumer(b[i])
            }
            return res
        }
    }

    private class InputStreamMetricWrapper(
        private val characterMetrics: List<IterativeMetric<Int>>,
        private val byteMetrics: List<IterativeMetric<Byte>>,
    ) : InputStreamMetric {
        override fun measure(input: InputStream): MetricResults {
            val bufferedIS = BufferedInputStream(input)
            val byteProcessingIS = ByteProcessingInputStream(
                bufferedIS,
                { byte -> byteMetrics.forEach { it.measure(byte) } },
                {}
            )
            val charReaderIR = InputStreamReader(byteProcessingIS, HseshCharsets.default)
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

    private class ByteCountFromStream :
        PrimitivesCount<Byte>(MetricName.Bytes)

    private class LineCountMetric(
        private var lineCount: Long = 0,
    ) : IterativeMetric<Int> {
        private val whitespaceCharsArray = lineSeparator().toCharArray()
        private val buffer = CharArray(whitespaceCharsArray.size)

        override fun measure(input: Int) {
            for (i in 0 until buffer.size - 1) {
                buffer[i] = buffer[i + 1]
            }
            buffer[buffer.lastIndex] = input.toChar()
            if (isNewLine()) {
                lineCount += 1
            }
        }

        override fun name() = MetricName.Lines
        override fun value() = lineCount

        private fun isNewLine(): Boolean {
            return whitespaceCharsArray.contentEquals(buffer)
        }
    }

    private class WordCountMetric(
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

private enum class MetricName {
    Lines,
    Words,
    Bytes,
}

private typealias MetricResults = SortedMap<MetricName, Long>

private fun MetricResults.joinResults(others: MetricResults) {
    for ((otherK, otherV) in others) {
        val curV = this.getOrDefault(otherK, 0)
        this[otherK] = curV + otherV
    }
}
