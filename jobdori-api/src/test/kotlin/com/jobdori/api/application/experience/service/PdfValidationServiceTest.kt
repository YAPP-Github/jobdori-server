package com.jobdori.api.application.experience.service

import com.jobdori.common.error.InvalidArgumentsException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayOutputStream

internal class PdfValidationServiceTest : StringSpec({

    val service = PdfValidationService()

    "PDF 콘텐츠 타입이 아니면 예외를 던진다" {
        val file = MockMultipartFile(
            "file",
            "resume.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            samplePdfBytes("Hello Jobdori"),
        )

        val exception = shouldThrow<InvalidArgumentsException> {
            service.validate(file = file, userId = 1L)
        }

        exception.message shouldBe "유효한 PDF 파일을 첨부해 주세요 [userId=1,originFileName=resume.pdf]"
    }

    "PDF 시그니처가 없으면 예외를 던진다" {
        val file = MockMultipartFile(
            "file",
            "resume.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "not pdf".toByteArray(),
        )

        val exception = shouldThrow<InvalidArgumentsException> {
            service.validate(file = file, userId = 1L)
        }

        exception.message shouldBe "유효한 PDF 파일을 첨부해 주세요 [userId=1,originFileName=resume.pdf]"
    }

    "PDF 확장자가 아니면 예외를 던진다" {
        val file = MockMultipartFile(
            "file",
            "resume.txt",
            MediaType.APPLICATION_PDF_VALUE,
            samplePdfBytes("Hello Jobdori"),
        )

        val exception = shouldThrow<InvalidArgumentsException> {
            service.validate(file = file, userId = 1L)
        }

        exception.message shouldBe "유효한 PDF 파일을 첨부해 주세요 [userId=1,originFileName=resume.txt]"
    }

})

private fun samplePdfBytes(text: String): ByteArray {
    return PDDocument().use { document ->
        val page = PDPage()
        document.addPage(page)

        PDPageContentStream(document, page).use { contentStream ->
            contentStream.beginText()
            contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
            contentStream.newLineAtOffset(50f, 750f)
            contentStream.showText(text)
            contentStream.endText()
        }

        ByteArrayOutputStream().use { outputStream ->
            document.save(outputStream)
            outputStream.toByteArray()
        }
    }
}
