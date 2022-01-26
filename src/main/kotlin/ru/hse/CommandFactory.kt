package ru.hse

import java.util.function.Function

interface CommandFactory {
    fun registerCommand(name: String, factory: Function<List<String>, Executable>)
    fun create(tokens: List<String>): Executable
}
