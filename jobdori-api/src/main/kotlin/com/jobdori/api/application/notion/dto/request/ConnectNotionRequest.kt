package com.jobdori.api.application.notion.dto.request

import com.jobdori.core.application.notion.command.ConnectNotionCommand
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL

data class ConnectNotionRequest(
    @field:NotBlank
    val authorizationCode: String = "",

    @field:URL
    @field:NotBlank
    val redirectUri: String = "",
) {

    fun toCommand(workspaceId: Long) = ConnectNotionCommand(
        workspaceId = workspaceId,
        authorizationCode = authorizationCode,
        redirectUri = redirectUri,
    )

}
