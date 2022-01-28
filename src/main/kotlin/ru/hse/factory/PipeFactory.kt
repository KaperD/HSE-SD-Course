package ru.hse.factory

import ru.hse.executable.Executable

interface PipeFactory {
    fun create(commands: List<Executable>): Executable
}
