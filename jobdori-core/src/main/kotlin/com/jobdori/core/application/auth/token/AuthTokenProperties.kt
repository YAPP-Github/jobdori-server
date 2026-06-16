package com.jobdori.core.application.auth.token

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("auth.token")
class AuthTokenProperties(
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration,
) {

    init {
        require(!accessTokenTtl.isNegative && !accessTokenTtl.isZero) {
            "Access token TTL must be positive"
        }
        require(!refreshTokenTtl.isNegative && !refreshTokenTtl.isZero) {
            "Refresh token TTL must be positive"
        }
        require(refreshTokenTtl > accessTokenTtl) {
            "Refresh token TTL must be longer than access token TTL"
        }
    }

}
