package com.jobdori.infrastructure.client.oauth.google.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GoogleOAuthErrorResponse(
    val error: String,

    @JsonProperty("error_description")
    val errorDescription: String? = null,
)
