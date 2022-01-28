package ru.hse.environment

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SystemEnvironmentTest {

    @Test
    fun `test system environment works`() {
        val env: Environment = EnvironmentImpl(null, System.getenv())
        val map = System.getenv()
        assertEquals(map["HOME"], env.get("HOME"))
        assertEquals(map.getOrDefault("AoAoA", ""), env.get("AoAoA"))
    }
}
