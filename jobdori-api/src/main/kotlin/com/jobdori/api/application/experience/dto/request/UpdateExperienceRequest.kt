package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UpdateExperienceRequest(
    @field:Positive(message = "올바른 프로젝트 ID를 입력해 주세요.")
    val projectId: Long,

    @field:Size(max = 10, message = "태그는 최대 {max}개까지 선택할 수 있어요.")
    val tags: List<String>,

    @field:NotBlank(message = "제목을 입력해 주세요.")
    @field:Size(max = 150, message = "제목은 최대 {max}자까지 입력할 수 있어요.")
    val title: String,

    @field:Valid
    val contents: ExperienceContentsRequest,

    @field:Valid
    val period: PeriodRequest?,

    @field:Size(max = 100, message = "역할은 최대 {max}자까지 입력할 수 있어요.")
    val role: String?,
)
