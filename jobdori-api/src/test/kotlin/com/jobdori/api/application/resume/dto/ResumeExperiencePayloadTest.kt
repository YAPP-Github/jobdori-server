package com.jobdori.api.application.resume.dto

import com.jobdori.api.application.resume.dto.request.ResumeExperiencePayloadRequest
import com.jobdori.api.application.resume.dto.response.ResumeExperiencePayloadResponse
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ResumeExperiencePayloadTest : StringSpec({

    "추천 질문방 ID를 경험 입력에서 도메인과 응답으로 전달한다" {
        val request = ResumeExperiencePayloadRequest(
            name = "프로젝트",
            role = null,
            period = null,
            contents = "경험 내용",
            experienceQuestionRoomId = "question-room-id",
        )

        val payload = request.toPayload()

        payload.experienceQuestionRoomId shouldBe "question-room-id"
        ResumeExperiencePayloadResponse.from(payload).experienceQuestionRoomId shouldBe "question-room-id"
    }

    "직접 입력한 경험은 추천 질문방 ID를 생략할 수 있다" {
        val request = ResumeExperiencePayloadRequest(
            name = "프로젝트",
            role = null,
            period = null,
            contents = "경험 내용",
        )

        val payload = request.toPayload()

        payload.experienceQuestionRoomId shouldBe null
        ResumeExperiencePayloadResponse.from(payload).experienceQuestionRoomId shouldBe null
    }

})
