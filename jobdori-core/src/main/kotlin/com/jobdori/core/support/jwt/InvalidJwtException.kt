package com.jobdori.core.support.jwt

data class InvalidJwtException(
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
