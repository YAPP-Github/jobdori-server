package com.jobdori.core.application.auth.oauth.google

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.client.GoogleOAuthTokenClient
import com.jobdori.core.application.auth.oauth.google.client.GoogleOAuthUserClient
import com.jobdori.core.application.auth.oauth.google.model.GoogleAuthorizationCode
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserInfo
import org.springframework.stereotype.Component

@Component
class GoogleAuthProcessor(
    private val googleOAuthTokenClient: GoogleOAuthTokenClient,
    private val googleOAuthUserClient: GoogleOAuthUserClient,
) {

    fun getGoogleUserInfo(command: AuthCommand): GoogleUserInfo {
        val accessToken = googleOAuthTokenClient.exchangeAuthorizationCode(
            GoogleAuthorizationCode(
                value = command.authorizationCode,
            ),
            redirectUrl = command.redirectUri,
        )
        return googleOAuthUserClient.getUserInfo(accessToken)
    }

}
