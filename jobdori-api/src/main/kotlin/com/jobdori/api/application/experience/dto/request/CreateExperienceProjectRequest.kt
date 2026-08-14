package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.PeriodRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateExperienceProjectRequest(
    @field:NotBlank(message = "이름을 입력해 주세요.")
    @field:Size(max = 100, message = "입력 가능한 이름의 최대 길이는 {max}자입니다.")
    val name: String = "",

    @field:NotBlank(message = "프로젝트 요약을 입력해 주세요.")
    @field:Size(max = 500, message = "입력 가능한 프로젝트 요약의 최대 길이는 {max}자입니다.")
    val summary: String = "",

    @field:Valid
    val period: PeriodRequest?,

    @field:Pattern(regexp = "(?s).*\\S.*", message = "역할을 입력해 주세요.")
    @field:Size(max = 100, message = "입력 가능한 역할의 최대 길이는 {max}자입니다.")
    val role: String?,
)
