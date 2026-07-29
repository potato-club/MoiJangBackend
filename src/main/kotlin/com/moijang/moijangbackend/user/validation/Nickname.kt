package com.moijang.moijangbackend.user.validation

fun isValidNickname(nickname: String): Boolean {
    return nickname.isNotBlank() && nickname.length > 3
}
