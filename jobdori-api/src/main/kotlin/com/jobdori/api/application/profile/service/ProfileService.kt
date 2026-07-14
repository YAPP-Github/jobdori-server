package com.jobdori.api.application.profile.service

import com.jobdori.api.application.profile.dto.request.UpdateProfileRequest
import com.jobdori.api.application.profile.dto.response.ProfileResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.profile.ProfileAiService
import com.jobdori.core.domain.keyword.KeywordType
import com.jobdori.core.domain.keyword.service.KeywordRegistrar
import com.jobdori.core.domain.profile.service.ProfileModifier
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service

@Service
class ProfileService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val profileReader: ProfileReader,
    private val profileModifier: ProfileModifier,
    private val profileAiService: ProfileAiService,
    private val keywordRegistrar: KeywordRegistrar,
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

        registerKeywords(request)

        return ProfileResponse.from(detail)
    }

    // 저장된 키워드를 사전에 등록해 다른 사용자의 자동완성에 재사용한다.
    // 부가 기능이므로 등록 실패가 프로필 저장 응답을 깨지 않게 격리한다
    private fun registerKeywords(request: UpdateProfileRequest) {
        runCatching {
            keywordRegistrar.register(KeywordType.LANGUAGE_TEST, request.languageTests.orEmpty().map { it.testName })
            keywordRegistrar.register(KeywordType.CERTIFICATION, request.certifications.orEmpty().map { it.name })
            keywordRegistrar.register(KeywordType.SKILL, request.skills.orEmpty().map { it.name })
        }.onFailure { e -> log.warn(e) { "키워드 사전 등록 실패 (프로필 저장은 정상 처리됨)" } }
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
