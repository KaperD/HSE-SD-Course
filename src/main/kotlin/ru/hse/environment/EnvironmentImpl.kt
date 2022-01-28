package ru.hse.environment

class EnvironmentImpl(
    private val parentEnv: Environment?,
    startVariables: Map<String, String> = emptyMap()
) : Environment {
    private val localVariables = HashMap<String, String>(startVariables)

    override fun set(key: String, value: String) {
        localVariables[key] = value
    }

    override fun get(key: String): String {
        return localVariables.getOrElse(key) { parentEnv?.get(key) ?: "" }
    }

    override fun getAll(): Collection<Pair<String, String>> {
        return localVariables.entries.map { entry ->
            Pair(entry.key, entry.value)
        }
    }
}
