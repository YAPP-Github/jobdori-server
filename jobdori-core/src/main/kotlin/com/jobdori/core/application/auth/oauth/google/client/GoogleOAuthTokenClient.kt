package com.jobdori.core.application.auth.oauth.google.client

import com.jobdori.core.application.auth.oauth.google.model.GoogleAccessToken
import com.jobdori.core.application.auth.oauth.google.model.GoogleAuthorizationCode

interface GoogleOAuthTokenClient {

    fun exchangeAuthorizationCode(
        authorizationCode: GoogleAuthorizationCode,
        redirectUrl: String,
    ): GoogleAccessToken

}
