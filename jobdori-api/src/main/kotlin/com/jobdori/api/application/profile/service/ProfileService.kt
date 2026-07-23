package com.jobdori.api.application.profile.service

import com.jobdori.api.application.profile.dto.request.PolishProfileTextRequest
import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.GenerateCoreCompetencyResponse
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
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

    // 결과만 반환하고 저장하지 않는다. 저장은 FE가 updateProfile로 수행
    fun generateCoreCompetency(userId: Long, workspaceId: String, jdId: String?): GenerateCoreCompetencyResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val profile = profileReader.getOrCreateProfile(workspace.id)
        val generation = profileAiService.generateCoreCompetency(profileReader.getDetail(profile), workspace.id, jdId)

        return GenerateCoreCompetencyResponse(
            coreCompetency = generation.coreCompetency,
            strategy = generation.strategy,
        )
    }

    // 결과만 반환하고 저장하지 않는다. jdId가 있으면 워크스페이스 검증 후 해당 JD의 지원 전략을 첨삭 기준으로 반영
    fun polishProfileText(userId: Long, workspaceId: String?, request: PolishProfileTextRequest): String {
        val workspace = request.jdId?.let {
            val id = workspaceId
                ?: throw InvalidArgumentsException("jdId로 지원 전략을 반영하려면 workspaceId가 필요합니다.")
            workspaceAccessValidationService.validateAccessible(workspaceId = id, userId = userId)
        }

        return profileAiService.polish(
            text = request.text,
            kind = request.kind,
            structure = request.structure,
            instruction = request.instruction,
            title = request.title,
            workspaceId = workspace?.id,
            jdPublicId = request.jdId,
        )
    }

}
