package ru.hse.command.flags

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.function.Consumer
import kotlin.streams.toList

interface Flag {
    fun handle(value: String?)
    fun requiresValue(): Boolean
    fun shortName(): Char?
    fun fullName(): String?

    fun oneDashFlag() = shortName()?.let { "-$it" }
    fun twoDashFlag() = fullName()?.let { "--$it" }

    fun primaryFlag(): String {
        val oneDash = oneDashFlag()
        val twoDash = twoDashFlag()
        return oneDash ?: twoDash ?: throw IllegalStateException("Flag must have at least one representation")
    }

    fun secondaryFlag(): String? {
        val oneDash = oneDashFlag()
        val twoDash = twoDashFlag()
        return if (oneDash == primaryFlag()) twoDash else oneDash
    }

    fun allRepresentations(): List<String> {
        val all = arrayListOf(primaryFlag())
        secondaryFlag()?.let { all.add(it) }
        return all
    }
}

class BooleanFlag(
    private val shortForm: Char?,
    private val fullForm: String?,
    private val handler: Runnable
) : Flag {
    override fun requiresValue() = false
    override fun shortName() = shortForm
    override fun fullName() = fullForm
    override fun handle(value: String?) = handler.run()
}

class ValuedFlag(
    private val shortForm: Char?,
    private val fullForm: String?,
    private val handler: Consumer<String>
) : Flag {
    override fun requiresValue() = true
    override fun shortName() = shortForm
    override fun fullName() = fullForm
    override fun handle(value: String?) = handler.accept(value!!)
}

class FlagParser(private val handlers: Map<String, Flag>) {
    constructor(flags: List<Flag>)
        : this(
        flags.stream()
            .flatMap { flag ->
                flag.allRepresentations().stream().map { r -> Pair(r, flag) }
            }.toList()
            .toMap()
    )

    /**
     * @return List of not parsed arguments
     */
    fun parseArgs(args: List<String>): List<String> {
        val notParsedArguments = ArrayList<String>()
        var skipNext = false
        for (argI in args.indices) {
            if (skipNext) {
                skipNext = false
                continue
            }
            handlers[args[argI]]
                ?.let {
                    if (it.requiresValue()) {
                        if (argI == args.size - 1) {
                            throw IllegalArgumentException("Flag ${it.primaryFlag()} requires value")
                        }
                        it.handle(args[argI + 1])
                        skipNext = true
                    } else {
                        it.handle(null)
                    }
                }
                ?: run {
                    notParsedArguments.add(args[argI])
                }
        }
        return notParsedArguments
    }
}
