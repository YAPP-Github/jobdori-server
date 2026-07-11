package com.jobdori.core.application.notion.client

import com.jobdori.core.domain.notion.NotionPageContent
import com.jobdori.core.domain.notion.NotionPages

interface NotionClient {

    fun searchPages(
        accessToken: String,
        query: String?,
        startCursor: String?,
        pageSize: Int,
    ): NotionPages

    fun getPageContent(
        accessToken: String,
        pageId: String,
    ): NotionPageContent

}
