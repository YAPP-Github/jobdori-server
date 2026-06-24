package com.jobdori.infrastructure.client.oauth.google.dto

data class GoogleUserInfoResponse(
    val sub: String,
    val name: String,
    val picture: String?,
)
