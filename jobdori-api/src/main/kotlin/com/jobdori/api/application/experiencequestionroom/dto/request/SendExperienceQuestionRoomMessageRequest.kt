package com.jobdori.api.application.experiencequestionroom.dto.request

import jakarta.validation.constraints.Size

data class SendExperienceQuestionRoomMessageRequest(
    @field:Size(max = 2000, message = "내용은 최대 {max}자까지 입력할 수 있어요.")
    val content: String? = null,

    @field:Size(max = 5, message = "경험은 최대 {max}개까지 선택할 수 있어요.")
    val experienceIds: List<Long> = emptyList(),
)
