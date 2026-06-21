package com.jobdori.core.application.auth.oauth.google.client

import com.jobdori.core.application.auth.oauth.google.model.GoogleAccessToken
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserId

interface GoogleOAuthUserClient {

    fun getUserId(accessToken: GoogleAccessToken): GoogleUserId

}
