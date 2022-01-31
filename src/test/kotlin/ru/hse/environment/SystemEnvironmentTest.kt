package ru.hse.environment

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SystemEnvironmentTest {

    @Test
    fun `test system environment works`() {
        val env: Environment = EnvironmentImpl(null, System.getenv())
        val map = System.getenv()
        val key = map.entries.first().key
        assertEquals(map[key], env.get(key))
        assertEquals(map.getOrDefault("AoAoA", ""), env.get("AoAoA"))
    }
}
