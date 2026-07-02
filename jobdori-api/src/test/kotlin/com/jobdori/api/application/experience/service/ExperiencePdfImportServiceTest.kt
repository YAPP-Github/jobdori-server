package com.jobdori.api.application.experience.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.experience.ExperienceImportService
import com.jobdori.core.domain.workspace.Workspace
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import java.io.ByteArrayOutputStream

internal class ExperiencePdfImportServiceTest : StringSpec({

    val experienceImportService = mockk<ExperienceImportService>()
    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val pdfValidationService = mockk<PdfValidationService>()
    val service = ExperiencePdfImportService(
        experienceImportService = experienceImportService,
        workspaceAccessValidationService = workspaceAccessValidationService,
        pdfValidationService = pdfValidationService,
    )

    beforeTest {
        clearMocks(experienceImportService, workspaceAccessValidationService, pdfValidationService)
        every {
            workspaceAccessValidationService.validateAccessible(
                workspaceId = "workspace-id",
                userId = 1L,
            )
        } returns Workspace(id = 1L, publicId = "workspace-id", ownerUserId = 1L)
    }

    "유효한 PDF는 파일 스캔 후 가져온 경험을 저장한다" {
        val pdfBytes = samplePdfBytes("Hello Jobdori")
        val file = MockMultipartFile(
            "file",
            "resume.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            pdfBytes,
        )

        every { pdfValidationService.validate(file = file, userId = 1L) } returns pdfBytes
        every { experienceImportService.saveAll(workspaceId = 1L, groups = any()) } returns Unit

        service.importExperiencesByPdf(file = file, workspaceId = "workspace-id", userId = 1L)

        verify(exactly = 1) { pdfValidationService.validate(file = file, userId = 1L) }
        verify(exactly = 1) { experienceImportService.saveAll(workspaceId = 1L, groups = any()) }
    }

    "PDF 검증에 실패하면 가져온 경험을 저장하지 않는다" {
        val file = MockMultipartFile(
            "file",
            "resume.pdf",
            MediaType.TEXT_PLAIN_VALUE,
            samplePdfBytes("Hello Jobdori"),
        )

        every {
            pdfValidationService.validate(file = file, userId = 1L)
        } throws InvalidArgumentsException(message = "유효한 PDF 파일을 첨부해 주세요")

        val exception = shouldThrow<InvalidArgumentsException> {
            service.importExperiencesByPdf(file = file, workspaceId = "workspace-id", userId = 1L)
        }

        exception.message shouldBe "유효한 PDF 파일을 첨부해 주세요"
        verify(exactly = 1) { pdfValidationService.validate(file = file, userId = 1L) }
        verify(exactly = 0) { experienceImportService.saveAll(workspaceId = any(), groups = any()) }
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
