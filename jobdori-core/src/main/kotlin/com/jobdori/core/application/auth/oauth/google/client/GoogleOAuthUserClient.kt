package com.jobdori.core.application.auth.oauth.google.client

import com.jobdori.core.application.auth.oauth.google.model.GoogleAccessToken
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserInfo

interface GoogleOAuthUserClient {

    fun getUserInfo(accessToken: GoogleAccessToken): GoogleUserInfo

}
