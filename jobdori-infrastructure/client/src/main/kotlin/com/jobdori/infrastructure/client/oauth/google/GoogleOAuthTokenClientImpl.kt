package com.jobdori.infrastructure.client.oauth.google

import com.jobdori.common.error.InternalServerException
import com.jobdori.common.json.JsonUtils
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.auth.oauth.google.client.GoogleOAuthTokenClient
import com.jobdori.core.application.auth.oauth.google.model.GoogleAccessToken
import com.jobdori.core.application.auth.oauth.google.model.GoogleAuthorizationCode
import com.jobdori.core.domain.auth.error.InvalidOAuthAuthorizationCodeException
import com.jobdori.infrastructure.client.oauth.google.dto.GoogleOAuthErrorResponse
import com.jobdori.infrastructure.client.oauth.google.dto.GoogleTokenResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body

@Component
class GoogleOAuthTokenClientImpl(
    private val googleOAuthProperties: GoogleOAuthProperties,
) : GoogleOAuthTokenClient {

    private val restClient = RestClient.builder()
        .baseUrl("https://oauth2.googleapis.com")
        .build()

    override fun exchangeAuthorizationCode(
        authorizationCode: GoogleAuthorizationCode,
        redirectUrl: String,
    ): GoogleAccessToken {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("code", authorizationCode.value)
            add("client_id", googleOAuthProperties.clientId)
            add("client_secret", googleOAuthProperties.clientSecret)
            add("redirect_uri", redirectUrl)
            add("grant_type", "authorization_code")
        }

        return try {
            val response = restClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body<GoogleTokenResponse>()
                ?: throw InternalServerException(message = "Failed to exchange Google OAuth authorization code")
            GoogleAccessToken(response.accessToken)
        } catch (exception: RestClientResponseException) {
            val errorResponse = try {
                JsonUtils.toObject(exception.responseBodyAsString, GoogleOAuthErrorResponse::class.java)
            } catch (_: IllegalArgumentException) {
                null
            }

            log.warn(exception) {
                """
                |Google OAuth token exchange failed.
                |status=${exception.statusCode},
                |error=${errorResponse?.error},
                |errorDescription=${errorResponse?.errorDescription},
                |responseBody=${JsonUtils.toJson(exception.responseBodyAsString)},
                """.trimIndent()
            }

            if (errorResponse?.error == "invalid_grant") {
                throw InvalidOAuthAuthorizationCodeException(
                    message = "Invalid OAuth authorization code ($authorizationCode)",
                    cause = exception
                )
            }

            throw InternalServerException(
                message = "Failed to exchange Google OAuth authorization code ($authorizationCode)",
                cause = exception,
            )
        } catch (exception: Exception) {
            throw InternalServerException(
                message = "Failed to exchange Google OAuth authorization code ($authorizationCode)",
                cause = exception
            )
        }
    }

}
