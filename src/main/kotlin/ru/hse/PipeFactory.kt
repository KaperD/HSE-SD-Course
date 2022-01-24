package ru.hse

interface PipeFactory {
    fun create(commands: List<Command>): Command
}