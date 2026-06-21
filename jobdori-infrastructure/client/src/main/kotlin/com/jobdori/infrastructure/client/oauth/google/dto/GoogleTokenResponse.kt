package com.jobdori.infrastructure.client.oauth.google.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class GoogleTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
)
