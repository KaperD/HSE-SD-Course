package ru.hse.factory

import ru.hse.executable.Executable
import ru.hse.pipe.FilePipe

class PipeFactoryImpl : PipeFactory {
    override fun create(commands: List<Executable>): Executable = FilePipe(commands)
}
