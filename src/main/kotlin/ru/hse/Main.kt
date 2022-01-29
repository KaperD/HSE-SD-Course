package ru.hse

import ru.hse.charset.HseshCharsets
import ru.hse.utils.write

fun main(args: Array<String>) {
    println("Hello World!")
    val p = ProcessBuilder("aooa", "3").start()
    p.outputStream.write("Hello")
    p.outputStream.close()

    println(p.errorStream.readAllBytes().toString(HseshCharsets.default))
    // Try adding program arguments at Run/Debug configuration
    println("Program arguments: ${args.joinToString()}")
}
