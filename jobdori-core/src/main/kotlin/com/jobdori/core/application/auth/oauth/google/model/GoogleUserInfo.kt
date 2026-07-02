package com.jobdori.core.application.auth.oauth.google.model

data class GoogleUserInfo(
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
) {

    init {
        require(id.isNotBlank()) { "Google user ID must not be blank" }
        require(email.isNotBlank()) { "Google user email must not be blank" }
        require(name.isNotBlank()) { "Google user name must not be blank" }
    }

}
