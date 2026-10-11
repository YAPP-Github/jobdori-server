package com.jobdori.infrastructure.persistence.domain.resume

import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeSectionItemFixture
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeSectionItemEntity
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class ResumeSectionItemEntityTest : StringSpec({

    val encryptor = mockk<StringEncryptor>()

    "경험 payload를 JSON으로 저장하고 읽을 때 추천 질문방 ID를 유지한다" {
        val payload = ResumeExperiencePayload(
            name = "프로젝트",
            role = null,
            period = null,
            contents = "경험 내용",
            experienceQuestionRoomId = "question-room-id",
        )
        val item = ResumeSectionItemFixture.create(payload = payload)

        val entity = ResumeSectionItemEntity.from(item, encryptor)

        entity.toDomain(encryptor).payload shouldBe payload
    }

    "추천 질문방 ID가 없는 기존 경험 JSON도 읽을 수 있다" {
        val entity = ResumeSectionItemEntity(
            sectionId = 1L,
            payloadType = ResumeSectionType.EXPERIENCE,
            payload = """{"name":"프로젝트","contents":"경험 내용","type":"EXPERIENCE"}""",
            displayOrder = 1.0,
            visible = true,
        )

        val payload = entity.toDomain(encryptor).payload as ResumeExperiencePayload

        payload.name shouldBe "프로젝트"
        payload.experienceQuestionRoomId shouldBe null
    }

})
