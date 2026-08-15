package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateExperienceProjectRequest(
    @field:NotBlank(message = "이름을 입력해 주세요.")
    @field:Size(max = 100, message = "이름은 최대 {max}자까지 입력할 수 있어요.")
    val name: String,

    @field:NotBlank(message = "프로젝트 요약을 입력해 주세요.")
    @field:Size(max = 500, message = "프로젝트 요약은 최대 {max}자까지 입력할 수 있어요.")
    val summary: String,

    @field:Valid
    val period: PeriodRequest?,

    @field:Pattern(regexp = "(?s).*\\S.*", message = "역할을 입력해 주세요.")
    @field:Size(max = 100, message = "역할은 최대 {max}자까지 입력할 수 있어요.")
    val role: String?,
)
