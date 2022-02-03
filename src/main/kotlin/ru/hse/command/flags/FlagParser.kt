package ru.hse.command.flags

import java.lang.IllegalArgumentException
import java.util.*
import kotlin.IllegalStateException
import kotlin.collections.ArrayList

class FlagParser(
    private val arguments: MutableList<Argument> = ArrayList(),
    private var lastPositional: Int = 0,
    private val notRecognizedArguments: SortedSet<Int> = TreeSet(),
) {
    fun parseArgs(args: List<String>): List<String> {
        notRecognizedArguments.addAll(args.indices)
        arguments.forEach { a -> notRecognizedArguments.removeAll(a.parse(args)) }
        return notRecognizedArguments.map(args::get).toList()
    }

    fun addBoolFlag(shortForm: Char): OptionalFlag {
        val f = OptionalFlag(shortForm, null, false, false, null)
        arguments.add(f)
        return f
    }

    fun addArg(required: Boolean): PositionalArgument {
        val argument = PositionalArgument(lastPositional, required, null)
        arguments.add(argument)
        lastPositional += 1
        return argument
    }

    fun addNonRecognized(description: String, amountRange: IntRange): NotRecognizedArguments {
        val arg = NotRecognizedArguments(amountRange, null, description)
        arguments.add(arg)
        return arg
    }

    class ArgumentParseException(arg: Argument, message: String) :
        Exception("Can not parse argument ${arg.name()}: $message")

    data class ParseResult(val value: Any, val recognized: List<Int>)

    abstract class Argument(
        private val required: Boolean,
        private var value: Any?,
        private var parseException: ArgumentParseException? = null
    ) {
        protected abstract fun extractValue(args: List<String>): ParseResult
        protected fun parseException(message: String) = ArgumentParseException(this, message)

        abstract fun name(): String

        fun parse(args: List<String>): List<Int> {
            try {
                val parseResult = extractValue(args)
                value = parseResult.value
                return parseResult.recognized
            } catch (e: ArgumentParseException) {
                parseException = e
                if (required) {
                    throw e
                }
            }
            return emptyList()
        }

        fun isDefined(): Boolean = value != null

        fun <T> valueAs(reader: (Any) -> T): T? = value?.let(reader)

        inline fun <reified T> valueCast(): T? = valueAs {
            if (it is T) {
                it
            } else {
                throw IllegalArgumentException("Value of argument ${name()} can not be casted")
            }
        }

        fun setDefault(value: String = "") {
            this.value = value
        }
    }

    class PositionalArgument(
        private val position: Int,
        required: Boolean,
        defaultValue: String?
    ) : Argument(required, defaultValue) {
        override fun extractValue(args: List<String>): ParseResult {
            if (args.size <= position) {
                throw parseException("too few arguments")
            }
            return ParseResult(args[position], listOf(position))
        }

        override fun name() = "#$position"
    }

    inner class NotRecognizedArguments(
        private val limit: IntRange,
        defaultValue: String?,
        private val description: String = "not recognized"
    ) : Argument(limit.first > 0, defaultValue) {
        override fun extractValue(args: List<String>): ParseResult {
            val recognizedArguments = args.indices.toMutableList()
            recognizedArguments.removeAll(notRecognizedArguments)

            if (notRecognizedArguments.size !in limit) {
                val errorDesc = if (notRecognizedArguments.size < limit.first) "too few" else "too much"

                throw parseException("$errorDesc: " + notRecognizedArguments
                    .map { s -> "'$s'" }
                    .joinToString { ", " })
            }

            if (notRecognizedArguments.isEmpty()) {
                throw parseException("nothing is not '$description'")
            }

            val notRecognizedStrings = notRecognizedArguments.map(args::get).toList()

            return ParseResult(
                notRecognizedStrings,
                recognizedArguments
            )
        }

        override fun name(): String = description
    }

    class OptionalFlag(
        private val shortForm: Char?,
        private val fullForm: String?,
        private val followedByValue: Boolean,
        required: Boolean,
        defaultValue: String?
    ) : Argument(required, defaultValue) {
        private fun oneDashFlag() = shortForm?.let { "-$it" }
        private fun twoDashFlag() = fullForm?.let { "--$it" }

        override fun name(): String {
            val oneDash = oneDashFlag()
            val twoDash = twoDashFlag()
            return oneDash ?: twoDash ?: throw IllegalStateException("Flag must have at least one representation")
        }

        override fun extractValue(args: List<String>): ParseResult {
            for ((i, arg) in args.withIndex()) {
                if (arg == oneDashFlag() || arg == twoDashFlag()) {
                    if (!followedByValue) {
                        return ParseResult("", listOf(i))
                    }
                    return ParseResult(
                        args.getOrNull(i + 1) ?: throw parseException("should be followed by a value"),
                        listOf(i, i + 1)
                    )

                }
            }
            throw parseException("not present")
        }
    }
}
