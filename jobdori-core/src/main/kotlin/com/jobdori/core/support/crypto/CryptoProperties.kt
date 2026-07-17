package com.jobdori.core.support.crypto

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Base64

@ConfigurationProperties(prefix = "crypto")
class CryptoProperties(
    secret: String,
) {

    val key: ByteArray = try {
        Base64.getDecoder().decode(secret)
    } catch (exception: IllegalArgumentException) {
        throw IllegalArgumentException("crypto.secret must be Base64 encoded", exception)
    }.also {
        require(it.size >= MIN_KEY_SIZE) {
            "crypto.secret must be at least $MIN_KEY_SIZE bytes"
        }
    }

    companion object {
        private const val MIN_KEY_SIZE = 32
    }

}
