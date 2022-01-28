package ru.hse.environment

import org.junit.jupiter.api.Test
import ru.hse.Environment
import kotlin.test.assertEquals

class SystemEnvironmentTest {

    @Test
    fun `test system environment works`() {
        val env: Environment = TODO("Not yet implemented")
        val map = System.getenv()
        assertEquals(map["HOME"], env.get("HOME"))
        assertEquals(map.getOrDefault("AoAoA", ""), env.get("AoAoA"))
    }
}
