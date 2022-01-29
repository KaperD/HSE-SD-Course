package ru.hse.factory

import ru.hse.executable.Executable
import ru.hse.pipe.InMemoryPipe

class PipeFactoryImpl : PipeFactory {
    override fun create(commands: List<Executable>): Executable = InMemoryPipe(commands)
}
