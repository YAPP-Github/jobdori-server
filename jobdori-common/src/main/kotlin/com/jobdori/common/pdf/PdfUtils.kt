package com.jobdori.common.pdf

import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO

object PdfUtils {

    fun hasPdfSignature(input: ByteArray): Boolean {
        return input.size >= PDF_SIGNATURE.size && PDF_SIGNATURE.indices.all { index -> input[index] == PDF_SIGNATURE[index] }
    }

    fun getPageCount(input: ByteArray): Int {
        return try {
            Loader.loadPDF(input).use { document ->
                document.numberOfPages
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 페이지 수 확인 중 에러가 발생하였습니다.", exception)
        }
    }

    fun getPageCount(input: InputStream): Int {
        return getPageCount(input.readBytes())
    }

    fun getPageCount(file: File): Int {
        return try {
            Loader.loadPDF(file).use { document ->
                document.numberOfPages
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 페이지 수 확인 중 에러가 발생하였습니다. file: (${file.name})", exception)
        }
    }

    fun extractText(input: ByteArray): String {
        return try {
            Loader.loadPDF(input).use { document ->
                normalizeExtractedText(PDFTextStripper().getText(document))
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 텍스트 추출 중 에러가 발생하였습니다.", exception)
        }
    }

    fun extractText(
        input: ByteArray,
        maxPageCount: Int,
        maxTextLength: Int,
    ): String {
        return try {
            Loader.loadPDF(input).use { document ->
                if (document.isEncrypted) {
                    throw IllegalArgumentException("암호화된 PDF는 지원하지 않습니다.")
                }
                if (document.numberOfPages > maxPageCount) {
                    throw IllegalArgumentException("PDF 페이지 수가 제한을 초과했습니다.")
                }

                val text = normalizeExtractedText(PDFTextStripper().getText(document))
                if (text.length > maxTextLength) {
                    throw IllegalArgumentException("PDF 추출 텍스트 길이가 제한을 초과했습니다.")
                }
                text
            }
        } catch (exception: IllegalArgumentException) {
            throw exception
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 텍스트 추출 중 에러가 발생하였습니다.", exception)
        }
    }

    fun extractText(input: InputStream): String {
        return extractText(input.readBytes())
    }

    fun extractText(file: File): String {
        return try {
            Loader.loadPDF(file).use { document ->
                normalizeExtractedText(PDFTextStripper().getText(document))
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 텍스트 추출 중 에러가 발생하였습니다. file: (${file.name})", exception)
        }
    }

    fun renderPagesAsPng(
        input: ByteArray,
        maxPageCount: Int,
        dpi: Float,
    ): List<ByteArray> {
        require(maxPageCount > 0) { "최대 페이지 수는 1 이상이어야 합니다." }
        require(dpi > 0) { "DPI는 0보다 커야 합니다." }

        return try {
            Loader.loadPDF(input).use { document ->
                if (document.isEncrypted) {
                    throw IllegalArgumentException("암호화된 PDF는 지원하지 않습니다.")
                }
                if (document.numberOfPages > maxPageCount) {
                    throw IllegalArgumentException("이미지 변환 가능한 PDF 페이지 수 제한을 초과했습니다.")
                }

                val renderer = PDFRenderer(document)
                (0 until document.numberOfPages).map { pageIndex ->
                    val image = renderer.renderImageWithDPI(pageIndex, dpi, ImageType.RGB)
                    ByteArrayOutputStream().use { output ->
                        check(ImageIO.write(image, "png", output)) { "PDF 페이지 PNG 변환에 실패했습니다." }
                        output.toByteArray()
                    }
                }
            }
        } catch (exception: IllegalArgumentException) {
            throw exception
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 페이지 이미지 변환 중 에러가 발생하였습니다.", exception)
        }
    }

    private fun normalizeExtractedText(text: String): String {
        return text
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace(NOISE_CHARACTERS_REGEX, " ")
            .replace(PRIVATE_USE_CHARACTERS_REGEX, "")
            .lines()
            .joinToString("\n") { line ->
                line
                    .replace(HORIZONTAL_SPACES_REGEX, " ")
                    .trimEnd()
            }
    }

    private val NOISE_CHARACTERS_REGEX = Regex("[!%#]")
    private val PRIVATE_USE_CHARACTERS_REGEX = Regex("[\\uE000-\\uF8FF]")
    private val HORIZONTAL_SPACES_REGEX = Regex("[\\t ]+")
    private val PDF_SIGNATURE = "%PDF-".toByteArray(Charsets.US_ASCII)

}
