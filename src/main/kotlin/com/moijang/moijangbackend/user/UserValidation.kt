package com.moijang.moijangbackend.user

object UserValidation {
    fun isValidNickname(nickname: String): Boolean {
        return nickname.isNotBlank() && nickname.length > 3
    }

//    fun isValidGoogleUserData(data: Googl): Boolean {
//        val attributes = requireMap(data)
//
//        return true
//    }
}