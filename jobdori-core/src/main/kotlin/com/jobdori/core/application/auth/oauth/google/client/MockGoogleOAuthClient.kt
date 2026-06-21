package com.jobdori.core.application.auth.oauth.google.client

import com.jobdori.core.application.auth.oauth.google.model.GoogleAccessToken
import com.jobdori.core.application.auth.oauth.google.model.GoogleAuthorizationCode
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserId
import org.springframework.stereotype.Component

@Component
class MockGoogleOAuthClient : GoogleOAuthTokenClient, GoogleOAuthUserClient {

    override fun exchangeAuthorizationCode(
        authorizationCode: GoogleAuthorizationCode,
        redirectUrl: String,
    ): GoogleAccessToken {
        // TODO: 구현해야함
        return GoogleAccessToken("dummy-google-access-token")
    }

    override fun getUserId(accessToken: GoogleAccessToken): GoogleUserId {
        // TODO: 구현해야함
        return GoogleUserId("dummy-google-user-id")
    }

}
