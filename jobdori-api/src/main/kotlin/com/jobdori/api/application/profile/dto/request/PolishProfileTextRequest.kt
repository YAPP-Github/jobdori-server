package com.jobdori.api.application.profile.dto.request

import com.fasterxml.jackson.annotation.JsonIgnore
import com.jobdori.core.application.profile.PolishStructure
import com.jobdori.core.application.profile.ProfilePolishKind
import com.jobdori.core.domain.profile.ProfilePolicy
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class PolishProfileTextRequest(
    @field:NotBlank(message = "다듬을 내용을 입력해 주세요.")
    @field:Size(max = ProfilePolicy.MAX_CONTENTS_LENGTH, message = "세부 내용은 최대 {max}자까지 입력할 수 있어요.")
    val description: String = "",

    @field:Size(max = ProfilePolicy.MAX_TITLE_LENGTH, message = "제목은 최대 {max}자까지 입력할 수 있어요.")
    val title: String? = null,

    val kind: ProfilePolishKind = ProfilePolishKind.CORE_COMPETENCY,

    val structure: PolishStructure? = null,

    @field:Size(max = 200, message = "입력 가능한 추가 요청 사항의 최대 길이는 {max}자입니다.")
    val instruction: String? = null,

    val jdId: String? = null,
) {

    @JsonIgnore
    @AssertTrue(message = "경험을 다듬으려면 제목을 입력해 주세요.")
    fun isExperienceTitleValid(): Boolean =
        kind != ProfilePolishKind.EXPERIENCE || !title.isNullOrBlank()

}
