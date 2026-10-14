package com.jobdori.api.application.resume.service

import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.experience.ExtractedCareer
import com.jobdori.core.application.experience.ExtractedPeriod
import com.jobdori.core.application.experience.ResumeImportExperience
import com.jobdori.core.application.experience.ResumeImportExtractionResult
import com.jobdori.core.application.experience.ResumeImportProject
import com.jobdori.core.domain.resume.ResumeCareerPayload
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeSectionType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PdfResumeImportServiceTest : StringSpec({

    "원문 경험 JSON을 읽어 bullet과 줄바꿈을 바꾸지 않고 경력과 별도 섹션에 저장한다" {
        val content = "  - 연간 5조 원 규모의 결제를 안정적으로 처리하는 데 기여했습니다.\n- Java, Spring, JPA를 활용했습니다.\n"
        val extraction = ResumeImportExtractionResult(
            careers = listOf(ExtractedCareer(
                company = "무신사",
                position = "백엔드 개발자",
                period = ExtractedPeriod(startYear = 2022, startMonth = 1, endYear = 2024, endMonth = 12),
            )),
            projects = listOf(ResumeImportProject(
                name = "결제 시스템",
                period = ExtractedPeriod(startYear = 2023, startMonth = 1, endYear = 2023, endMonth = 12),
                role = "프로젝트 공통 역할",
                experiences = listOf(ResumeImportExperience(
                    title = "무신사 및 계열 서비스 결제 시스템 개발 및 유지보수 경험",
                    role = "백엔드 개발자",
                    content = content,
                    period = ExtractedPeriod(startYear = 2023, startMonth = 3, endYear = 2023, endMonth = 6),
                )),
            )),
        )
        val json = JsonUtils.toJson(extraction)
        val decoded = JsonUtils.toObject(json, ResumeImportExtractionResult::class.java)!!
        val command = decoded.toResumeSaveCommand(targetJdId = 42)
        val experience = command.sections.single { it.type == ResumeSectionType.EXPERIENCE }
            .items.single().payload as ResumeExperiencePayload
        val career = command.sections.single { it.type == ResumeSectionType.CAREER }
            .items.single().payload as ResumeCareerPayload

        command.targetJdId shouldBe 42
        experience.name shouldBe "무신사 및 계열 서비스 결제 시스템 개발 및 유지보수 경험"
        experience.role shouldBe "백엔드 개발자"
        experience.contents shouldBe content
        experience.period?.startAt shouldBe LocalDate.of(2023, 3, 1)
        experience.period?.endAt shouldBe LocalDate.of(2023, 6, 30)
        career.companyName shouldBe "무신사"
        career.period?.startAt shouldBe LocalDate.of(2022, 1, 1)
        career.period?.endAt shouldBe LocalDate.of(2024, 12, 31)
        json.contains("situation") shouldBe false
        json.contains("action") shouldBe false
    }

    "경험에 없는 역할 기간 내용을 프로젝트 값으로 채우지 않는다" {
        val extraction = ResumeImportExtractionResult(
            projects = listOf(ResumeImportProject(
                name = "결제 시스템",
                summary = "프로젝트 전체 소개",
                role = "프로젝트 역할",
                period = ExtractedPeriod(startYear = 2023, startMonth = 1),
                experiences = listOf(ResumeImportExperience(title = "경험명")),
            )),
        )

        val payload = extraction.toResumeSaveCommand(null).sections.single()
            .items.single().payload as ResumeExperiencePayload

        payload.name shouldBe "경험명"
        payload.role shouldBe null
        payload.period shouldBe null
        payload.contents shouldBe null
    }

})
