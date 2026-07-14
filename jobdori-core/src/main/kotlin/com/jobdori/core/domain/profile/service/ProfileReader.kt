package com.jobdori.core.domain.profile.service

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.repository.ProfileRepository
import com.jobdori.core.domain.profile.repository.ProfileSectionRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service

@Service
class ProfileReader(
    private val profileRepository: ProfileRepository,
    private val profileSectionRepository: ProfileSectionRepository,
) {

    // 프로필은 워크스페이스당 1개, 최초 접근 시 지연 생성한다 (기존 워크스페이스 마이그레이션 불필요)
    fun getOrCreateProfile(workspaceId: Long): Profile {
        return profileRepository.findByWorkspaceId(workspaceId)
            ?: try {
                profileRepository.save(Profile.newInstance(workspaceId))
            } catch (e: DataIntegrityViolationException) {
                // 동시 최초 접근 경합: workspaceId unique 제약에 걸리면 먼저 생성된 프로필을 재조회
                profileRepository.findByWorkspaceId(workspaceId) ?: throw e
            }
    }

    fun getDetail(profile: Profile): ProfileDetail {
        return ProfileDetail(
            profile = profile,
            sections = profileSectionRepository.findAllByProfileId(profile.id),
        )
    }

}
