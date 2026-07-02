package com.moijang.moijangbackend.util

object Validator {
    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    fun isEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        return EMAIL_REGEX.matches(email)
    }
}