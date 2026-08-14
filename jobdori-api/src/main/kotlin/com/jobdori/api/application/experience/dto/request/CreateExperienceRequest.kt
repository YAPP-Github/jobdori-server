package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateExperienceRequest(
    @field:Positive(message = "올바른 프로젝트 ID를 입력해 주세요.")
    val projectId: Long,

    @field:Size(max = 10, message = "태그는 최대 {max}개까지 선택할 수 있습니다.")
    val tags: List<String> = emptyList(),

    @field:Size(max = 150, message = "입력 가능한 제목의 최대 길이는 {max}자입니다.")
    val title: String = "",

    @field:Valid
    val contents: ExperienceContentsRequest,

    @field:Valid
    val period: PeriodRequest? = null,

    @field:Size(max = 100, message = "입력 가능한 역할의 최대 길이는 {max}자입니다.")
    val role: String? = null,
)
