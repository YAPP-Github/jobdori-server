package com.untitled.api.application.member

import jakarta.validation.constraints.NotBlank

data class MemberGetRequest(
    @field:NotBlank
    val memberId: String,
)
