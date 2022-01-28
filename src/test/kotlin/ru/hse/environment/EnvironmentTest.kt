package ru.hse.environment

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnvironmentTest {
    private fun createEnvironment(parentEnv: Map<String, String>): Environment {
        return EnvironmentImpl(EnvironmentImpl(null, parentEnv.toMap()))
    }

    @Test
    fun `test environment get from parent`() {
        val environment = createEnvironment(
            mapOf(
                "a" to "3",
                "b" to "4",
                "__aA1" to "3"
            )
        )
        assertEquals("3", environment.get("a"))
        assertEquals("4", environment.get("b"))
        assertEquals("3", environment.get("__aA1"))
        assertEquals("", environment.get("A"))
        assertEquals("", environment.get("__aA11"))
    }

    @Test
    fun `test environment set`() {
        val environment = createEnvironment(
            mapOf(
                "a" to "3",
                "b" to "4",
                "__aA1" to "3"
            )
        )
        environment.set("a", "0")
        environment.set("b", "1")
        environment.set("__aA1", "2")
        environment.set("new", "new")
        assertEquals("0", environment.get("a"))
        assertEquals("1", environment.get("b"))
        assertEquals("2", environment.get("__aA1"))
        assertEquals("new", environment.get("new"))
        assertEquals("", environment.get("new2"))
        environment.set("a", "00")
        environment.set("b", "10")
        environment.set("__aA1", "20")
        environment.set("new", "new0")
        assertEquals("00", environment.get("a"))
        assertEquals("10", environment.get("b"))
        assertEquals("20", environment.get("__aA1"))
        assertEquals("new0", environment.get("new"))
        assertEquals("", environment.get("new2"))
    }

    @Test
    fun `test environment get all`() {
        val environment = createEnvironment(
            mapOf(
                "a" to "3",
                "b" to "4",
                "__aA1" to "3"
            )
        )
        assertTrue(environment.getAll().isEmpty())
        environment.set("a", "0")
        environment.set("b", "1")
        environment.set("__aA1", "2")
        environment.set("new", "new")
        assertEquals(
            mapOf(
                "a" to "0",
                "b" to "1",
                "__aA1" to "2",
                "new" to "new"
            ),
            environment.getAll()
        )
        environment.set("a", "00")
        environment.set("b", "10")
        environment.set("__aA1", "20")
        environment.set("new", "new0")
        assertEquals(
            mapOf(
                "a" to "00",
                "b" to "10",
                "__aA1" to "20",
                "new" to "new0"
            ),
            environment.getAll()
        )
    }
}
