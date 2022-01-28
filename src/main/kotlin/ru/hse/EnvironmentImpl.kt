package ru.hse

class EnvironmentImpl(
    private val parentEnv: Map<String, String>
) : Environment {
    private val localEnv = HashMap<String, String>()

    override fun set(key: String, value: String) {
        localEnv[key] = value
    }

    override fun get(key: String): String {
        return localEnv.getOrDefault(key, parentEnv.getOrDefault(key, ""))
    }

    override fun getAll(): Collection<Pair<String, String>> {
        return localEnv.entries.map { entry ->
            Pair(entry.key, entry.value)
        }
    }
}
