package ru.hse.validator

class VarNameValidatorImpl : VarNameValidator {
    private val validNameRegex = "[_a-zA-Z][_a-zA-Z0-9]*".toRegex()

    override fun check(token: String): Boolean =
        validNameRegex.matches(token)

    override fun nameFromBeginningIn(token: String): String {
        val match = validNameRegex.find(token)
        if (match == null || match.range.first > 0) {
            return ""
        }
        return match.value
    }
}
