package com.jobdori.infrastructure.client.notion

import com.jobdori.common.error.InternalServerException
import com.jobdori.common.json.JsonUtils
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.notion.client.NotionOAuthTokenClient
import com.jobdori.core.domain.auth.error.InvalidOAuthAuthorizationCodeException
import com.jobdori.core.domain.notion.NotionOAuthToken
import com.jobdori.core.domain.notion.error.NotionConnectionNeedReconnectException
import com.jobdori.infrastructure.client.notion.dto.NotionOAuthErrorResponse
import com.jobdori.infrastructure.client.notion.dto.NotionTokenResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.time.Duration
import java.util.Base64

@Component
class NotionOAuthTokenClientImpl(
    private val notionProperties: NotionProperties,
) : NotionOAuthTokenClient {

    private val restClient = RestClient.builder()
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(3))
            setReadTimeout(Duration.ofSeconds(5))
        })
        .baseUrl("https://api.notion.com")
        .build()

    override fun exchangeAuthorizationCode(
        authorizationCode: String,
        redirectUri: String,
    ): NotionOAuthToken {
        return requestToken(
            body = mapOf(
                "grant_type" to "authorization_code",
                "code" to authorizationCode,
                "redirect_uri" to redirectUri,
            ),
            failureMessage = "Failed to exchange Notion OAuth authorization code",
        ) { exception, errorResponse ->
            if (errorResponse?.error == "invalid_grant") {
                throw InvalidOAuthAuthorizationCodeException(
                    message = "Invalid Notion OAuth authorization code",
                    cause = exception,
                )
            }
        }
    }

    override fun refresh(refreshToken: String): NotionOAuthToken {
        return requestToken(
            body = mapOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken,
            ),
            failureMessage = "Failed to refresh Notion OAuth token",
        ) { exception, errorResponse ->
            if (errorResponse?.error == "invalid_grant") {
                throw NotionConnectionNeedReconnectException(
                    message = "Notion refresh token이 유효하지 않습니다. [operation=refreshToken, notionError=${errorResponse.error}]",
                    cause = exception,
                )
            }
        }
    }

    private fun requestToken(
        body: Map<String, String>,
        failureMessage: String,
        onError: (RestClientResponseException, NotionOAuthErrorResponse?) -> Unit,
    ): NotionOAuthToken {
        return try {
            val response = restClient.post()
                .uri("/v1/oauth/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic ${basicCredential()}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body<NotionTokenResponse>()
                ?: throw InternalServerException(message = failureMessage)

            response.toDomain()
        } catch (exception: RestClientResponseException) {
            val errorResponse = parseErrorResponse(exception)
            log.warn(exception) {
                """
                |$failureMessage.
                |status=${exception.statusCode},
                |error=${errorResponse?.error},
                |message=${errorResponse?.message},
                """.trimIndent()
            }
            onError(exception, errorResponse)
            throw InternalServerException(message = failureMessage, cause = exception)
        } catch (exception: Exception) {
            throw InternalServerException(message = failureMessage, cause = exception)
        }
    }

    private fun parseErrorResponse(exception: RestClientResponseException): NotionOAuthErrorResponse? {
        return try {
            JsonUtils.toObject(exception.responseBodyAsString, NotionOAuthErrorResponse::class.java)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun basicCredential(): String {
        val rawCredential = "${notionProperties.clientId}:${notionProperties.clientSecret}"
        return Base64.getEncoder().encodeToString(rawCredential.toByteArray())
    }

    private fun NotionTokenResponse.toDomain() = NotionOAuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
        botId = botId,
        notionWorkspaceId = workspaceId,
        workspaceName = workspaceName,
        workspaceIcon = workspaceIcon,
    )

}
