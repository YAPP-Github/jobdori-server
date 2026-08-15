package com.jobdori.api.application.notion.dto.request

import com.jobdori.core.application.notion.command.ConnectNotionCommand
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class ConnectNotionRequest(
    @field:NotBlank(message = "인증 코드를 입력해 주세요.")
    val authorizationCode: String = "",

    @field:URL(message = "올바른 리디렉션 URL 형식으로 입력해 주세요.")
    @field:NotBlank(message = "리디렉션 URL을 입력해 주세요.")
    val redirectUri: String = "",
) {

    fun toCommand(workspaceId: Long) = ConnectNotionCommand(
        workspaceId = workspaceId,
        authorizationCode = authorizationCode,
        redirectUri = redirectUri,
    )

}
