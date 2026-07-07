package com.jobdori.api.application.experience.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.pdf.PdfUtils
import com.jobdori.core.application.experience.ExperienceAiExtractionService
import com.jobdori.core.application.experience.ExperienceImportService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Service
class ExperiencePdfImportService(
    private val experienceImportService: ExperienceImportService,
    private val experienceAiExtractionService: ExperienceAiExtractionService,
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val pdfValidationService: PdfValidationService,
) {

    fun importExperiences(file: MultipartFile, workspaceId: String, userId: Long) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val pdfBytes = pdfValidationService.validate(file = file, userId = userId)
        val text = try {
            extractTextWithTimeout(pdfBytes)
        } catch (exception: IllegalArgumentException) {
            throw InvalidArgumentsException(
                message = "PDF 파일 추출에 실패하였습니다 [userId=${userId},originFileName=${file.originalFilename}]",
                cause = exception,
            )
        }

        val commandGroups = experienceAiExtractionService.extract(text)

        experienceImportService.saveAll(
            workspaceId = workspace.id,
            groups = commandGroups,
        )
    }

    private fun extractTextWithTimeout(pdfBytes: ByteArray): String {
        return try {
            CompletableFuture.supplyAsync {
                PdfUtils.extractText(
                    input = pdfBytes,
                    maxPageCount = MAX_PDF_PAGE_COUNT,
                    maxTextLength = MAX_PDF_TEXT_LENGTH,
                )
            }.get(PDF_PARSE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        } catch (exception: ExecutionException) {
            throw IllegalArgumentException("PDF 텍스트 추출 중 에러가 발생하였습니다.", exception.cause ?: exception)
        } catch (exception: TimeoutException) {
            throw IllegalArgumentException("PDF 텍스트 추출 시간이 제한을 초과했습니다.", exception)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalArgumentException("PDF 텍스트 추출이 중단되었습니다.", exception)
        }
    }

    companion object {
        private const val MAX_PDF_PAGE_COUNT = 50
        private const val MAX_PDF_TEXT_LENGTH = 200_000
        private const val PDF_PARSE_TIMEOUT_SECONDS = 10L
    }

}
