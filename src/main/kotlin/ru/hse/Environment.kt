package ru.hse

interface Environment {
    fun set(key: String, value: String)
    fun get(key: String): String
    fun getAll(): Collection<Pair<String, String>>
}