package com.jobdori.api.application.profile.service

import com.jobdori.api.application.profile.dto.request.PolishExperienceRequest
import com.jobdori.api.application.profile.dto.request.PolishProfileTextRequest
import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.GenerateCoreCompetencyResponse
import com.jobdori.api.application.profile.dto.response.PolishedExperienceResponse
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.profile.ProfileAiService
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import com.jobdori.core.domain.resume.service.ResumeReader
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val profileReader: ProfileReader,
    private val profileModifier: ProfileModifier,
    private val profileAiService: ProfileAiService,
    private val resumeReader: ResumeReader,
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

    // 생성 결과는 저장하지 않고 응답으로만 반환하며, 이력서에 선택된 경험은 생성 근거로만 사용한다.
    fun generateCoreCompetency(
        userId: Long,
        workspaceId: String,
        resumeId: Long,
        jdId: String?,
    ): GenerateCoreCompetencyResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val profile = profileReader.getOrCreateProfile(workspace.id)
        val resumeDetail = resumeReader.getDetail(workspaceId = workspace.id, resumeId = resumeId)
        val generation = profileAiService.generateCoreCompetency(
            detail = profileReader.getDetail(profile),
            resumeDetail = resumeDetail,
            workspaceId = workspace.id,
            jdPublicId = jdId,
        )

        return GenerateCoreCompetencyResponse(
            coreCompetency = generation.coreCompetency,
            strategy = generation.strategy,
        )
    }

    // 결과만 반환하고 저장하지 않는다. jdId가 있으면 워크스페이스 검증 후 해당 JD의 지원 전략을 첨삭 기준으로 반영
    fun polishProfileText(userId: Long, workspaceId: String?, request: PolishProfileTextRequest): String {
        val validatedWorkspaceId = validateWorkspaceForJd(userId, workspaceId, request.jdId)

        return profileAiService.polish(
            text = request.text,
            kind = request.kind,
            structure = request.structure,
            instruction = request.instruction,
            workspaceId = validatedWorkspaceId,
            jdPublicId = request.jdId,
        )
    }

    fun polishExperience(
        userId: Long,
        workspaceId: String?,
        request: PolishExperienceRequest,
    ): PolishedExperienceResponse {
        val validatedWorkspaceId = validateWorkspaceForJd(userId, workspaceId, request.jdId)
        val polished = profileAiService.polishExperience(
            title = request.title,
            description = request.description,
            structure = request.structure,
            instruction = request.instruction,
            workspaceId = validatedWorkspaceId,
            jdPublicId = request.jdId,
        )

        return PolishedExperienceResponse(
            title = polished.title,
            description = polished.description,
        )
    }

    private fun validateWorkspaceForJd(userId: Long, workspaceId: String?, jdId: String?): Long? {
        return jdId?.let {
            val id = workspaceId
                ?: throw InvalidArgumentsException("jdId로 지원 전략을 반영하려면 workspaceId가 필요합니다.")
            workspaceAccessValidationService.validateAccessible(workspaceId = id, userId = userId).id
        }
    }

}
