package com.jobdori.core.application.auth.oauth.google.model

@JvmInline
value class GoogleUserId(
    val value: String,
) {

    init {
        require(value.isNotBlank()) { "Google user ID must not be blank" }
    }

}
