package com.jobdori.common.pdf

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files

internal class PdfUtilsTest : StringSpec({

    "hasPdfSignature는 PDF 시그니처 여부를 반환한다" {
        PdfUtils.hasPdfSignature("%PDF-1.4 sample".toByteArray()) shouldBe true
        PdfUtils.hasPdfSignature("not pdf".toByteArray()) shouldBe false
    }

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

    "extractText는 PDF 추출 노이즈 문자를 공백으로 정규화한다" {
        // given
        val input = samplePdfBytes("Walk!Mission!&!Reward!System#Ops", "Startup%Backend%Engineer")

        // when
        val result = PdfUtils.extractText(input)

        // then
        result shouldBe "Walk Mission & Reward System Ops\nStartup Backend Engineer\n"
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

    "extractText는 제한 페이지 수를 초과하면 IllegalArgumentException을 던진다" {
        // given
        val input = samplePdfBytes("First Page", "Second Page")

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            PdfUtils.extractText(input = input, maxPageCount = 1, maxTextLength = 1_000)
        }

        // then
        exception.message shouldBe "PDF 페이지 수가 제한을 초과했습니다."
    }

    "extractText는 제한 텍스트 길이를 초과하면 IllegalArgumentException을 던진다" {
        // given
        val input = samplePdfBytes("Hello Jobdori")

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            PdfUtils.extractText(input = input, maxPageCount = 10, maxTextLength = 5)
        }

        // then
        exception.message shouldBe "PDF 추출 텍스트 길이가 제한을 초과했습니다."
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

    "renderPagesAsPng는 과도하게 큰 CropBox 페이지를 거부한다" {
        // given: Create a PDF with an oversized CropBox (e.g., 20000x20000 points at 150 DPI = ~125M pixels)
        val input = pdfBytesWithOversizedCropBox(width = 20000f, height = 20000f)

        // when
        val exception = shouldThrow<IllegalArgumentException> {
            PdfUtils.renderPagesAsPng(input = input, maxPageCount = 10, dpi = 150f)
        }

        // then
        exception.message!! shouldContain "PDF 페이지 픽셀 수가 제한을 초과했습니다"
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

private fun pdfBytesWithOversizedCropBox(width: Float, height: Float): ByteArray {
    return PDDocument().use { document ->
        val page = PDPage()
        document.addPage(page)

        // Set an oversized CropBox
        val cropBox = org.apache.pdfbox.pdmodel.common.PDRectangle(width, height)
        page.cropBox = cropBox

        PDPageContentStream(document, page).use { contentStream ->
            contentStream.beginText()
            contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
            contentStream.newLineAtOffset(50f, height - 50f)
            contentStream.showText("Oversized page")
            contentStream.endText()
        }

        ByteArrayOutputStream().use { outputStream ->
            document.save(outputStream)
            outputStream.toByteArray()
        }
    }
}
