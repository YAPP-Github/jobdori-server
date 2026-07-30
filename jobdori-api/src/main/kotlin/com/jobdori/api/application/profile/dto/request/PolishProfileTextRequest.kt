package com.jobdori.api.application.profile.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.application.profile.PolishStructure
import com.jobdori.core.application.profile.ProfilePolishKind
import com.jobdori.core.domain.profile.ProfilePolicy
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PolishProfileTextRequest(
    @field:NotBlank
    @field:Size(max = ProfilePolicy.MAX_CONTENTS_LENGTH)
    val description: String = "",

    @field:Size(max = ProfilePolicy.MAX_TITLE_LENGTH)
    val title: String? = null,

    val kind: ProfilePolishKind = ProfilePolishKind.CORE_COMPETENCY,

    val structure: PolishStructure? = null,

    @field:Size(max = 200)
    val instruction: String? = null,

    val jdId: String? = null,
) {

    @JsonIgnore
    @AssertTrue(message = "경험 첨삭에는 title이 필요합니다")
    fun isExperienceTitleValid(): Boolean =
        kind != ProfilePolishKind.EXPERIENCE || !title.isNullOrBlank()

}
