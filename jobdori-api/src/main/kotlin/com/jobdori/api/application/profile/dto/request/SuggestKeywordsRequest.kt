package com.jobdori.api.application.profile.dto.request

import com.jobdori.core.domain.keyword.KeywordType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class SuggestKeywordsRequest(
    val type: KeywordType = KeywordType.SKILL,

    @field:NotBlank
    @field:Size(max = 50)
    val keyword: String = "",

    @field:Max(value = 20)
    @field:Positive
    val size: Int = 10,
)
