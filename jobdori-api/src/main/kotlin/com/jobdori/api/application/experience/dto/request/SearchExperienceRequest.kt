package com.jobdori.api.application.experience.dto.request

import com.jobdori.api.application.common.dto.request.CursorRequest
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class SearchExperienceRequest(
    @field:NotBlank(message = "검색어를 입력해 주세요.")
    @field:Size(max = 100, message = "검색어는 최대 {max}자까지 입력할 수 있어요.")
    val keyword: String,

    override val cursor: String? = null,

    @field:Max(value = 30, message = "페이지당 조회 개수의 최댓값은 {value}입니다.")
    @field:Positive(message = "페이지당 조회 개수에 0보다 큰 값을 입력해 주세요.")
    override val size: Int = 10,
) : CursorRequest
