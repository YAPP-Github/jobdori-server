package com.jobdori.api.application.profile.service

import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.application.profile.ProfileAiService
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val profileReader: ProfileReader,
    private val profileModifier: ProfileModifier,
    private val profileAiService: ProfileAiService,
) {

    fun getProfile(userId: Long, workspaceId: String): ProfileResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val profile = profileReader.getOrCreateProfile(workspace.id)

        return ProfileResponse.from(profileReader.getDetail(profile))
    }

    fun updateProfile(userId: Long, workspaceId: String, request: UpdateProfileRequest): ProfileResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val profile = profileReader.getOrCreateProfile(workspace.id)
        val detail = profileModifier.modify(profile, request.toCommand())

        return ProfileResponse.from(detail)
    }

    // 결과 문자열만 반환하고 저장하지 않는다. 저장은 FE가 updateProfile로 수행
    fun generateCoreCompetency(userId: Long, workspaceId: String): String {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val profile = profileReader.getOrCreateProfile(workspace.id)

        return profileAiService.generateCoreCompetency(profileReader.getDetail(profile))
    }

}
