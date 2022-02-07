package ru.hse.environment

/**
 * Если переменная не нашлась, то перенаправляет запрос к родителю (если он есть)
 */
class EnvironmentImpl(
    private val parentEnv: Environment?,
    startVariables: Map<String, String> = emptyMap()
) : Environment {
    private val localVariables = HashMap<String, String>(startVariables)

    override fun set(key: String, value: String) {
        localVariables[key] = value
    }

    override fun get(key: String): String {
        return localVariables.getOrElse(key) {
            return when (parentEnv) {
                null -> ""
                else -> parentEnv.get(key)
            }
        }
    }

    override fun getAll(): Map<String, String> {
        return localVariables
    }
}
