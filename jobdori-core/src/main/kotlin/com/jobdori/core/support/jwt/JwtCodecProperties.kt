package com.jobdori.core.support.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Base64

@ConfigurationProperties("jwt.codec")
class JwtCodecProperties(
    secret: String,
) {

    val decodedSecret: ByteArray = runCatching {
        Base64.getDecoder().decode(secret)
    }.getOrElse {
        throw IllegalArgumentException("jwt.codec.secret must be Base64 encoded", it)
    }.also {
        require(it.size >= MIN_SECRET_SIZE) {
            "JWT HMAC secret must be at least $MIN_SECRET_SIZE bytes"
        }
    }

    private companion object {
        const val MIN_SECRET_SIZE = 32
    }

}
