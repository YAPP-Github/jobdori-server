package com.jobdori.core.application.ai.client

import com.jobdori.core.application.ai.command.AiGenerationRequest

data class DocumentPageImage(
    val pageNumber: Int,
    val mediaType: String,
    val bytes: ByteArray,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentPageImage

        if (pageNumber != other.pageNumber) return false
        if (mediaType != other.mediaType) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pageNumber
        result = 31 * result + mediaType.hashCode()
        result = 31 * result + bytes.contentHashCode()
        return result
    }

}

interface DocumentVisionClient {
    fun extractText(request: AiGenerationRequest, pageImages: List<DocumentPageImage>): String
}
