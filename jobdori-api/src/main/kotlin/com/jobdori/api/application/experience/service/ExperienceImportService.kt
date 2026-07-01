package com.jobdori.api.application.experience.service

import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.common.pdf.PdfUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class ExperienceImportService {

    fun importExperiencesByPdf(file: MultipartFile, userId: Long) {
        val pdfBytes = file.bytes
        val text = try {
            PdfUtils.extractText(pdfBytes)
        } catch (exception: IllegalArgumentException) {
            throw InvalidArgumentsException(
                message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]",
                cause = exception,
                details = listOf(
                    ErrorDetail(
                        field = "file",
                        reason = "유효한 PDF 파일이 아닙니다"
                    )
                )
            )
        }

        log.info { "[EXPERIENCE_PDF_IMPORT] PDF로부터 텍스트를 추출합니다 [text=$text]" }

        // TODO: AI 돌려서 경험 추출 -> 경험 저장

    }

}
