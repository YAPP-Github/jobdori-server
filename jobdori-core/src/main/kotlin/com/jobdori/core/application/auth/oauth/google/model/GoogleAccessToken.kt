package com.jobdori.core.application.auth.oauth.google.model

@JvmInline
value class GoogleAccessToken(
    val value: String,
) {

    init {
        require(value.isNotBlank()) { "Google access token must not be blank" }
    }

}
