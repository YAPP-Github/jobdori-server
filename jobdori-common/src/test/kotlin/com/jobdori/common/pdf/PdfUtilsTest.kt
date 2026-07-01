package com.jobdori.common.pdf

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files

internal class PdfUtilsTest : StringSpec({

    "getPageCount는 ByteArray 타입 PDF의 페이지 수를 반환한다" {
        // given
        val input = samplePdfBytes("First Page", "Second Page")

        // when
        val result = PdfUtils.getPageCount(input)

        // then
        result shouldBe 2
    }

    "getPageCount는 InputStream 타입 PDF의 페이지 수를 반환한다" {
        // given
        val input = ByteArrayInputStream(samplePdfBytes("First Page", "Second Page"))

        // when
        val result = PdfUtils.getPageCount(input)

        // then
        result shouldBe 2
    }

    "getPageCount는 File 타입 PDF의 페이지 수를 반환한다" {
        // given
        val tempFile = Files.createTempFile("jobdori-pdf-utils-", ".pdf").toFile()
        tempFile.writeBytes(samplePdfBytes("First Page", "Second Page"))

        // when
        val result = PdfUtils.getPageCount(tempFile)

        // then
        result shouldBe 2
    }

    "extractText는 ByteArray 타입 PDF에서 텍스트를 추출한다" {
        // given
        val input = samplePdfBytes("Hello Jobdori")

        // when
        val result = PdfUtils.extractText(input)

        // then
        result.trim() shouldBe "Hello Jobdori"
    }

    "extractText는 InputStream 타입 PDF에서 텍스트를 추출한다" {
        // given
        val input = ByteArrayInputStream(samplePdfBytes("InputStream PDF"))

        // when
        val result = PdfUtils.extractText(input)

        // then
        result.trim() shouldBe "InputStream PDF"
    }

    "extractText는 File 타입 PDF에서 텍스트를 추출한다" {
        // given
        val tempFile = Files.createTempFile("jobdori-pdf-utils-", ".pdf").toFile()
        tempFile.writeBytes(samplePdfBytes("File PDF"))

        // when
        val result = PdfUtils.extractText(tempFile)

        // then
        result.trim() shouldBe "File PDF"
    }

    "extractText는 PDF 파싱 실패 시 IllegalArgumentException을 던진다" {
        // given
        val input = "not pdf".toByteArray()

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            PdfUtils.extractText(input)
        }

        // then
        exception.message shouldBe "PDF 텍스트 추출 중 에러가 발생하였습니다."
    }

    "getPageCount는 PDF 파싱 실패 시 IllegalArgumentException을 던진다" {
        // given
        val input = "not pdf".toByteArray()

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            PdfUtils.getPageCount(input)
        }

        // then
        exception.message shouldBe "PDF 페이지 수 확인 중 에러가 발생하였습니다."
    }

})

private fun samplePdfBytes(vararg texts: String): ByteArray {
    return PDDocument().use { document ->
        texts.forEach { text ->
            val page = PDPage()
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                contentStream.beginText()
                contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
                contentStream.newLineAtOffset(50f, 750f)
                contentStream.showText(text)
                contentStream.endText()
            }
        }

        ByteArrayOutputStream().use { outputStream ->
            document.save(outputStream)
            outputStream.toByteArray()
        }
    }
}
