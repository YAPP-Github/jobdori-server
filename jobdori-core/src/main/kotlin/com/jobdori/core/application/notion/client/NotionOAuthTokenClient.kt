package com.jobdori.core.application.notion.client

import com.jobdori.core.domain.notion.NotionOAuthToken

interface NotionOAuthTokenClient {

    fun exchangeAuthorizationCode(
        authorizationCode: String,
        redirectUri: String,
    ): NotionOAuthToken

    fun refresh(refreshToken: String): NotionOAuthToken

}
