package com.jobdori.infrastructure.client.notion

import com.jobdori.common.error.InternalServerException
import com.jobdori.core.application.notion.client.NotionClient
import com.jobdori.core.domain.notion.NotionBlock
import com.jobdori.core.domain.notion.NotionPageContent
import com.jobdori.core.domain.notion.NotionPageSummary
import com.jobdori.core.domain.notion.NotionPages
import com.jobdori.core.domain.notion.error.NotionApiRequestFailedException
import com.jobdori.core.domain.notion.error.NotionPageAccessDeniedException
import com.jobdori.core.domain.notion.error.NotionUnauthorizedException
import com.jobdori.infrastructure.client.notion.dto.NotionBlockChildrenResponse
import com.jobdori.infrastructure.client.notion.dto.NotionBlockResponse
import com.jobdori.infrastructure.client.notion.dto.NotionPageResponse
import com.jobdori.infrastructure.client.notion.dto.NotionRichTextBlockResponse
import com.jobdori.infrastructure.client.notion.dto.NotionSearchResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import java.time.Duration
import java.time.OffsetDateTime

@Component
class NotionClientImpl(
    private val notionProperties: NotionProperties,
) : NotionClient {

    private val restClient = RestClient.builder()
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(3))
            setReadTimeout(Duration.ofSeconds(10))
        })
        .baseUrl("https://api.notion.com")
        .build()

    override fun searchPages(
        accessToken: String,
        query: String?,
        startCursor: String?,
        pageSize: Int,
    ): NotionPages {
        val body = buildMap {
            put("page_size", pageSize)
            put("filter", mapOf("property" to "object", "value" to "page"))
            if (!query.isNullOrBlank()) {
                put("query", query)
            }
            if (!startCursor.isNullOrBlank()) {
                put("start_cursor", startCursor)
            }
        }

        val response = executeNotionRequest(
            operation = "searchPages",
            contextValues = mapOf(
                "query" to query,
                "startCursor" to startCursor,
                "pageSize" to pageSize,
            ),
        ) {
            restClient.post()
                .uri("/v1/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .notionHeaders(accessToken)
                .retrieve()
                .body<NotionSearchResponse>()
                ?: throw InternalServerException(message = "Empty Notion search response")
        }

        return NotionPages(
            pages = response.results.map { it.toSummary() },
            nextCursor = response.nextCursor,
            hasMore = response.hasMore,
        )
    }

    override fun getPageContent(
        accessToken: String,
        pageId: String,
    ): NotionPageContent {
        val page = retrievePage(accessToken, pageId)
        val blocks = retrieveBlockChildrenRecursively(accessToken, pageId)
        return NotionPageContent(
            page = page,
            plainText = blocks.joinToString("\n") { it.toPlainText() }.trim(),
            blocks = blocks,
        )
    }

    private fun retrievePage(accessToken: String, pageId: String): NotionPageSummary {
        return executeNotionRequest(
            operation = "retrievePage",
            contextValues = mapOf("pageId" to pageId),
        ) {
            restClient.get()
                .uri("/v1/pages/{pageId}", pageId)
                .notionHeaders(accessToken)
                .retrieve()
                .body<NotionPageResponse>()
                ?.toSummary()
                ?: throw InternalServerException(message = "Empty Notion page response")
        }
    }

    private fun retrieveBlockChildrenRecursively(accessToken: String, blockId: String): List<NotionBlock> {
        return retrieveBlockChildren(accessToken, blockId).map { block ->
            val children = if (block.hasChildren && block.canRetrieveChildren()) {
                retrieveBlockChildrenRecursively(accessToken, block.id)
            } else {
                emptyList()
            }
            block.toDomain(children)
        }
    }

    private fun retrieveBlockChildren(accessToken: String, blockId: String): List<NotionBlockResponse> {
        val blocks = mutableListOf<NotionBlockResponse>()
        var cursor: String? = null
        do {
            val startCursor = cursor
            val response = try {
                executeNotionRequest(
                    operation = "retrieveBlockChildren",
                    contextValues = mapOf(
                        "blockId" to blockId,
                        "startCursor" to startCursor,
                    ),
                ) {
                    restClient.get()
                        .uri { builder ->
                            builder.path("/v1/blocks/{blockId}/children")
                                .queryParam("page_size", 100)
                                .apply {
                                    if (startCursor != null) {
                                        queryParam("start_cursor", startCursor)
                                    }
                                }
                                .build(blockId)
                        }
                        .notionHeaders(accessToken)
                        .retrieve()
                        .body<NotionBlockChildrenResponse>()
                        ?: throw InternalServerException(message = "Empty Notion block children response")
                }
            } catch (exception: NotionApiRequestFailedException) {
                if (exception.isUnsupportedBlockChildrenRequest()) {
                    return blocks
                }
                throw exception
            }
            blocks += response.results
            cursor = response.nextCursor
        } while (response.hasMore && cursor != null)

        return blocks
    }

    private fun <T> executeNotionRequest(
        operation: String,
        contextValues: Map<String, Any?> = emptyMap(),
        block: () -> T,
    ): T {
        return try {
            block()
        } catch (exception: RestClientResponseException) {
            val context = contextValues + mapOf(
                "operation" to operation,
                "httpStatus" to exception.statusCode.value(),
            )
            when (exception.statusCode.value()) {
                401 -> throw NotionUnauthorizedException(
                    message = "Notion API 인증에 실패했습니다. ${context.toMessageContext()}",
                    cause = exception,
                )

                403, 404 -> throw NotionPageAccessDeniedException(
                    message = "Notion 페이지 접근 권한이 없거나 공유되지 않았습니다. ${context.toMessageContext()}",
                    cause = exception,
                )

                else -> throw NotionApiRequestFailedException(
                    message = "Notion API 요청에 실패했습니다. ${context.toMessageContext()}",
                    cause = exception,
                )
            }
        } catch (exception: NotionUnauthorizedException) {
            throw exception
        } catch (exception: NotionPageAccessDeniedException) {
            throw exception
        } catch (exception: Exception) {
            throw NotionApiRequestFailedException(
                message = "Notion API 요청 처리 중 오류가 발생했습니다. ${(contextValues + mapOf("operation" to operation)).toMessageContext()}",
                cause = exception,
            )
        }
    }

    private fun Map<String, Any?>.toMessageContext(): String {
        return entries
            .filter { (_, value) -> value != null }
            .joinToString(prefix = "[", postfix = "]") { (key, value) -> "$key=$value" }
    }

    private fun RestClient.RequestHeadersSpec<*>.notionHeaders(accessToken: String): RestClient.RequestHeadersSpec<*> {
        return header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
            .header("Notion-Version", notionProperties.apiVersion)
            .accept(MediaType.APPLICATION_JSON)
    }

    private fun NotionPageResponse.toSummary() = NotionPageSummary(
        id = id,
        title = properties.values.firstOrNull { it.type == "title" }
            ?.title
            ?.joinToString("") { it.plainText.orEmpty() }
            ?.takeIf { it.isNotBlank() }
            ?: "Untitled",
        url = url,
        lastEditedTime = lastEditedTime?.let { OffsetDateTime.parse(it).toLocalDateTime() },
    )

    private fun NotionBlockResponse.toDomain(children: List<NotionBlock>) = NotionBlock(
        id = id,
        type = type,
        plainText = plainText(),
        children = children,
    )

    private fun NotionBlockResponse.plainText(): String {
        return when (type) {
            "paragraph" -> paragraph.richTextPlainText()
            "heading_1" -> heading1.richTextPlainText()
            "heading_2" -> heading2.richTextPlainText()
            "heading_3" -> heading3.richTextPlainText()
            "bulleted_list_item" -> bulletedListItem.richTextPlainText()
            "numbered_list_item" -> numberedListItem.richTextPlainText()
            "quote" -> quote.richTextPlainText()
            "callout" -> callout.richTextPlainText()
            "to_do" -> toDo.richTextPlainText()
            "toggle" -> toggle.richTextPlainText()
            "code" -> code.richTextPlainText()
            "child_page" -> childPage?.title.orEmpty()
            else -> ""
        }
    }

    private fun NotionBlockResponse.canRetrieveChildren(): Boolean {
        return type !in UNSUPPORTED_CHILDREN_BLOCK_TYPES
    }

    private fun NotionApiRequestFailedException.isUnsupportedBlockChildrenRequest(): Boolean {
        val responseException = cause as? RestClientResponseException ?: return false
        return responseException.statusCode.value() == 400 &&
            responseException.responseBodyAsString.contains("Block type ai_block is not supported")
    }

    private fun NotionRichTextBlockResponse?.richTextPlainText(): String {
        return this?.richText?.joinToString("") { it.plainText.orEmpty() }.orEmpty()
    }

    private fun NotionBlock.toPlainText(): String {
        return listOf(plainText, children.joinToString("\n") { it.toPlainText() })
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    companion object {
        private val UNSUPPORTED_CHILDREN_BLOCK_TYPES = setOf("ai_block")
    }

}
