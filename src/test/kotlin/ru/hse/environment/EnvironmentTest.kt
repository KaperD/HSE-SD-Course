package ru.hse.environment


import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnvironmentTest {
    private fun createEnvironment(parentEnv: Collection<Pair<String, String>>): Environment = EnvironmentImpl(EnvironmentImpl(null, parentEnv.toMap()))

    @Test
    fun `test environment get from parent`() {
        val environment = createEnvironment(
            listOf(
                Pair("a", "3"),
                Pair("b", "4"),
                Pair("__aA1", "3")
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
            listOf(
                Pair("a", "3"),
                Pair("b", "4"),
                Pair("__aA1", "3")
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
            listOf(
                Pair("a", "3"),
                Pair("b", "4"),
                Pair("__aA1", "3")
            )
        )
        assertEquals(
            emptySet(),
            HashSet(environment.getAll())
        )
        environment.set("a", "0")
        environment.set("b", "1")
        environment.set("__aA1", "2")
        environment.set("new", "new")
        assertEquals(
            setOf(
                Pair("a", "0"),
                Pair("b", "1"),
                Pair("__aA1", "2"),
                Pair("new", "new")
            ),
            HashSet(environment.getAll())
        )
        environment.set("a", "00")
        environment.set("b", "10")
        environment.set("__aA1", "20")
        environment.set("new", "new0")
        assertEquals(
            setOf(
                Pair("a", "00"),
                Pair("b", "10"),
                Pair("__aA1", "20"),
                Pair("new", "new0")
            ),
            HashSet(environment.getAll())
        )
    }
}
