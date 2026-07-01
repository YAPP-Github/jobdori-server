package com.jobdori.common.pdf

import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.InputStream

object PdfUtils {

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
                PDFTextStripper().getText(document)
            }
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
                PDFTextStripper().getText(document)
            }
        } catch (exception: Exception) {
            throw IllegalArgumentException("PDF 텍스트 추출 중 에러가 발생하였습니다. file: (${file.name})", exception)
        }
    }

}
