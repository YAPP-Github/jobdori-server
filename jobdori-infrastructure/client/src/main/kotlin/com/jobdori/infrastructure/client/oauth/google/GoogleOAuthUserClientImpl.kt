package com.jobdori.infrastructure.client.oauth.google

import com.jobdori.common.error.InternalServerException
import com.jobdori.common.json.JsonUtils
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.auth.oauth.google.client.GoogleOAuthUserClient
import com.jobdori.core.application.auth.oauth.google.model.GoogleAccessToken
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserId
import com.jobdori.infrastructure.client.oauth.google.dto.GoogleUserInfoResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class GoogleOAuthUserClientImpl : GoogleOAuthUserClient {

    private val restClient = RestClient.builder()
        .baseUrl("https://openidconnect.googleapis.com")
        .build()

    override fun getUserId(accessToken: GoogleAccessToken): GoogleUserId {
        return try {
            val response = restClient.get()
                .uri("/v1/userinfo")
                .header("Authorization", "Bearer ${accessToken.value}")
                .retrieve()
                .body<GoogleUserInfoResponse>()
                ?: throw InternalServerException(message = "Failed to fetch Google OAuth user info (access_token: ${accessToken.value})")

            GoogleUserId(response.sub)
        } catch (exception: RestClientResponseException) {
            log.warn(exception) {
                """
                |Google OAuth user info request failed (access_token: ${accessToken.value})
                |status=${exception.statusCode},
                |responseBody=${JsonUtils.toJson(exception.responseBodyAsString)}
                """.trimIndent()
            }

            throw InternalServerException(
                message = "Failed to fetch Google OAuth user info (access_token: ${accessToken.value})",
                cause = exception
            )
        } catch (exception: Exception) {
            throw InternalServerException(
                message = "Failed to fetch Google OAuth user info (access_token: ${accessToken.value})",
                cause = exception
            )
        }
    }

}
