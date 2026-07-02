package com.jobdori.api.application.experience.service

import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.pdf.PdfUtils
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class PdfValidationService {

    fun validate(file: MultipartFile, userId: Long): ByteArray {
        validateMetadata(file = file, userId = userId)

        val pdfBytes = file.bytes
        validateBytes(file = file, pdfBytes = pdfBytes, userId = userId)

        return pdfBytes
    }

    private fun validateMetadata(file: MultipartFile, userId: Long) {
        if (file.isEmpty) {
            throw InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]")
        }
        if (file.size > MAX_PDF_FILE_SIZE_BYTES) {
            throw InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]")
        }
        if (file.contentType != "application/pdf") {
            throw InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]")
        }
        if (!file.originalFilename.orEmpty().endsWith(".pdf", ignoreCase = true)) {
            throw InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]")
        }
    }

    private fun validateBytes(file: MultipartFile, pdfBytes: ByteArray, userId: Long) {
        if (pdfBytes.isEmpty()) {
            throw InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]")
        }
        if (!PdfUtils.hasPdfSignature(pdfBytes)) {
            throw InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요 [userId=${userId},originFileName=${file.originalFilename}]")
        }
    }

    companion object {
        private const val MAX_PDF_FILE_SIZE_BYTES = 10L * 1024L * 1024L
    }

}
