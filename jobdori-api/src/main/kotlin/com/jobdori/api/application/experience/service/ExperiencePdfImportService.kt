package com.jobdori.api.application.experience.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.model.Period
import com.jobdori.common.pdf.PdfUtils
import com.jobdori.core.application.experience.command.ImportedExperienceCommandGroup
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import com.jobdori.core.application.experience.ExperienceImportService as CoreExperienceImportService

@Service
class ExperiencePdfImportService(
    private val experienceImportService: CoreExperienceImportService,
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val pdfValidationService: PdfValidationService,
) {

    fun importExperiencesByPdf(file: MultipartFile, workspaceId: String, userId: Long) {
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

        // TODO: AI로 변경
        val commandGroups = mockImportedExperienceCommandGroups()

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

    private fun mockImportedExperienceCommandGroups(): List<ImportedExperienceCommandGroup> {
        return listOf(
            ImportedExperienceCommandGroup(
                project = ExperienceProjectCreateCommand(
                    name = "채용 플랫폼 지원 자동화",
                    summary = "공고 탐색부터 지원 현황 관리까지 이어지는 채용 지원 워크플로우를 개선한 프로젝트",
                    period = Period(
                        startAt = LocalDate.of(2025, 1, 1),
                        endAt = LocalDate.of(2025, 4, 30),
                    ),
                    role = "백엔드 개발",
                ),
                experiences = listOf(
                    ExperienceCreateCommand(
                        tags = listOf("Kotlin", "Spring Boot", "PostgreSQL"),
                        title = "지원 현황 관리 API 설계",
                        contents = ExperienceContents.star(
                            situation = "사용자가 여러 채용 공고에 지원하면서 진행 상태와 메모가 흩어져 관리되고 있었습니다.",
                            task = "프로젝트 단위로 지원 경험을 기록하고 상태 변경 이력을 안정적으로 관리해야 했습니다.",
                            action = "경험 프로젝트와 개별 경험을 분리한 도메인 모델을 설계하고 커서 기반 목록 API를 구현했습니다.",
                            result = "지원 현황 조회와 상태 변경 흐름이 단순해졌고, 이후 GraphQL 조회 기능도 같은 도메인 모델로 확장했습니다.",
                        ),
                    ),
                    ExperienceCreateCommand(
                        tags = listOf("REST Docs", "Kotest", "SpringMockK"),
                        title = "문서화 테스트 기반 API 검증",
                        contents = ExperienceContents.free(
                            "REST Docs 테스트를 작성해 요청/응답 스펙과 인증 헤더를 함께 검증했습니다. " + "컨트롤러 변경 시 문서와 테스트가 같이 깨지도록 만들어 API 계약을 유지했습니다.",
                        ),
                    ),
                ),
            ),
            ImportedExperienceCommandGroup(
                project = ExperienceProjectCreateCommand(
                    name = "이력서 PDF 경험 추출",
                    summary = "이력서 PDF에서 텍스트를 추출하고 경험 후보를 구조화하는 import 기능",
                    period = Period(
                        startAt = LocalDate.of(2025, 5, 1),
                        endAt = LocalDate.of(2025, 7, 31),
                    ),
                    role = "서비스 개발",
                ),
                experiences = listOf(
                    ExperienceCreateCommand(
                        tags = listOf("PDF", "AI", "Import"),
                        title = "PDF 텍스트 추출 실패 처리",
                        contents = ExperienceContents.star(
                            situation = "사용자가 잘못된 파일을 업로드하면 내부 예외가 그대로 노출될 가능성이 있었습니다.",
                            task = "유효하지 않은 PDF를 명확한 사용자 오류로 변환하고 필드 단위 사유를 제공해야 했습니다.",
                            action = "PDF 파싱 예외를 InvalidArgumentsException으로 감싸고 file 필드의 상세 사유를 내려주도록 처리했습니다.",
                            result = "잘못된 입력에 대해 일관된 에러 응답을 제공하고, 로그에는 사용자와 파일명을 남겨 추적 가능성을 높였습니다.",
                        ),
                    ),
                    ExperienceCreateCommand(
                        tags = listOf("Kotlin", "Command", "Domain"),
                        title = "추출 결과를 생성 커맨드로 정규화",
                        contents = ExperienceContents.free(
                            "AI 추출 결과를 바로 저장 모델에 묶지 않고 ExperienceProjectCreateCommand와 " + "ExperienceCreateCommand 조합으로 정규화해 저장 계층과 추출 계층의 결합을 줄였습니다.",
                        ),
                    ),
                    ExperienceCreateCommand(
                        tags = listOf("Logging", "Observability"),
                        title = "Import 처리 로그 추가",
                        contents = ExperienceContents.free(
                            "PDF 추출 텍스트와 생성된 프로젝트/경험 개수를 로그로 남겨 import 결과를 빠르게 확인할 수 있게 했습니다.",
                        ),
                    ),
                ),
            ),
            ImportedExperienceCommandGroup(
                project = ExperienceProjectCreateCommand(
                    name = "워크스페이스 권한 검증 고도화",
                    summary = "사용자별 워크스페이스 접근 권한을 서비스 진입점에서 일관되게 검증한 작업",
                    period = Period(
                        startAt = LocalDate.of(2024, 10, 1),
                        endAt = LocalDate.of(2024, 12, 31),
                    ),
                    role = "백엔드 개발",
                ),
                experiences = listOf(
                    ExperienceCreateCommand(
                        tags = listOf("Authorization", "Workspace", "Service"),
                        title = "서비스 레이어 권한 검증 공통화",
                        contents = ExperienceContents.star(
                            situation = "각 API가 워크스페이스 접근 권한을 개별적으로 검증하면서 누락 위험이 있었습니다.",
                            task = "경험, 프로젝트 조회와 변경 흐름에서 같은 방식으로 접근 가능 여부를 확인해야 했습니다.",
                            action = "워크스페이스 접근 검증 서비스를 도입하고 경험 서비스의 진입점에서 먼저 검증하도록 정리했습니다.",
                            result = "권한 검증 위치가 명확해졌고, 도메인 서비스는 워크스페이스 식별자 기준의 비즈니스 처리에 집중할 수 있게 됐습니다.",
                        ),
                    ),
                    ExperienceCreateCommand(
                        tags = listOf("GraphQL", "REST", "API"),
                        title = "REST와 GraphQL 응답 모델 정합성 유지",
                        contents = ExperienceContents.free(
                            "경험 조회 응답에서 프로젝트 포함 여부를 옵션화하고, REST와 GraphQL이 같은 도메인 조회 흐름을 사용하도록 구성했습니다.",
                        ),
                    ),
                ),
            ),
        )
    }

    companion object {
        private const val MAX_PDF_PAGE_COUNT = 50
        private const val MAX_PDF_TEXT_LENGTH = 200_000
        private const val PDF_PARSE_TIMEOUT_SECONDS = 10L
    }

}
