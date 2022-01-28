package ru.hse.charset

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

interface HseshCharsets {
    companion object {
        val default: Charset = StandardCharsets.UTF_8
    }
}
