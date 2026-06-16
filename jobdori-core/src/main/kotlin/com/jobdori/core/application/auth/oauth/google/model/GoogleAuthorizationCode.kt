package com.jobdori.core.application.auth.oauth.google.model

@JvmInline
value class GoogleAuthorizationCode(
    val value: String,
) {

    init {
        require(value.isNotBlank()) { "Google authorization code must not be blank" }
    }

}
