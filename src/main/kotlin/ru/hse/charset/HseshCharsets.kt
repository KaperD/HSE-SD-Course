package ru.hse.charset

import java.nio.charset.Charset

/**
 * Кодировки, которые используются в hsesh
 */
interface HseshCharsets {
    companion object {
        /**
         * Должна использоваться, если нет веских причин её не использовать
         */
        val default: Charset = Charset.defaultCharset()
    }
}
