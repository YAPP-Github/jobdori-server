package com.jobdori.api.application.experience.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.pdf.PdfUtils
import com.jobdori.core.application.ai.client.DocumentPageImage
import com.jobdori.core.application.ai.client.DocumentVisionClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

@Service
class PdfExperienceImportService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val pdfValidationService: PdfValidationService,
    private val experienceTextImportService: ExperienceTextImportService,
    private val documentVisionClient: DocumentVisionClient,
    private val promptTemplateRepository: PromptTemplateRepository,
) {

    fun importExperiences(file: MultipartFile, workspaceId: String, userId: Long) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val pdfBytes = pdfValidationService.validate(file = file, userId = userId)
        val extractedText = try {
            extractTextWithTimeout(pdfBytes)
        } catch (exception: IllegalArgumentException) {
            throw InvalidArgumentsException(
                message = "PDF 파일 추출에 실패하였습니다 [userId=${userId},originFileName=${file.originalFilename}]",
                cause = exception,
            )
        }

        val text = if (isLowQualityText(extractedText)) {
            extractTextFromPageImages(pdfBytes, userId, file.originalFilename)
        } else {
            extractedText
        }

        if (isLowQualityText(text)) throw InvalidArgumentsException(message = "PDF에서 가져올 텍스트가 없습니다 [userId=$userId]")

        experienceTextImportService.import(
            workspaceId = workspace.id,
            text = text,
        )
    }

    private fun extractTextFromPageImages(pdfBytes: ByteArray, userId: Long, originalFilename: String?): String {
        return try {
            val pageImages = PdfUtils.renderPagesAsPng(
                input = pdfBytes,
                maxPageCount = MAX_VISION_PAGE_COUNT,
                dpi = VISION_RENDER_DPI,
            ).mapIndexed { index, bytes ->
                DocumentPageImage(pageNumber = index + 1, mediaType = "image/png", bytes = bytes)
            }
            val prompt = promptTemplateRepository.findByType(PromptType.DOCUMENT_TEXT_EXTRACTION)
                ?: throw AiException(
                    "문서 이미지 텍스트 추출 프롬프트가 없습니다.",
                    AiErrorCode.E500_AI_GENERATION_FAILED,
                )
            documentVisionClient.extractText(
                request = prompt.build(VISION_USER_PROMPT),
                pageImages = pageImages,
            )
        } catch (exception: Exception) {
            throw InvalidArgumentsException(
                message = "PDF 이미지 텍스트 추출에 실패하였습니다 [userId=$userId,originFileName=$originalFilename]",
                cause = exception,
            )
        }
    }

    private fun isLowQualityText(text: String): Boolean {
        val compact = text.filterNot(Char::isWhitespace)
        if (compact.length < MIN_MEANINGFUL_TEXT_LENGTH) return true

        val meaningfulCount = compact.count { it.isLetterOrDigit() }
        val replacementCount = compact.count { it == '\uFFFD' }
        return meaningfulCount.toDouble() / compact.length < MIN_MEANINGFUL_CHARACTER_RATIO ||
            replacementCount.toDouble() / compact.length > MAX_REPLACEMENT_CHARACTER_RATIO
    }

    private fun extractTextWithTimeout(pdfBytes: ByteArray): String {
        return try {
            PdfUtils.extractText(
                input = pdfBytes,
                maxPageCount = MAX_PDF_PAGE_COUNT,
                maxTextLength = MAX_PDF_TEXT_LENGTH,
            )
        } catch (exception: ExecutionException) {
            throw IllegalArgumentException("PDF 텍스트 추출 중 에러가 발생하였습니다.", exception.cause ?: exception)
        } catch (exception: TimeoutException) {
            throw IllegalArgumentException("PDF 텍스트 추출 시간이 제한을 초과했습니다.", exception)
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 텍스트 추출에 실패하였습니다", exception)
        }
    }

    companion object {
        private const val MAX_PDF_PAGE_COUNT = 50
        private const val MAX_PDF_TEXT_LENGTH = 200_000
        private const val MAX_VISION_PAGE_COUNT = 10
        private const val VISION_RENDER_DPI = 150f
        private const val MIN_MEANINGFUL_TEXT_LENGTH = 30
        private const val MIN_MEANINGFUL_CHARACTER_RATIO = 0.5
        private const val MAX_REPLACEMENT_CHARACTER_RATIO = 0.1
        private const val VISION_USER_PROMPT =
            "페이지 순서대로 모든 텍스트를 전사해라. 표와 목록의 의미를 유지하고, 페이지 경계는 [PAGE n]으로 표시해라. 출력은 전사된 원문만 반환해라."
    }

}
