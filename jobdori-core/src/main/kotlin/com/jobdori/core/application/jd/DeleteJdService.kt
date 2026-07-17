package com.jobdori.core.application.jd

import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteJdService(
    private val jdRepository: JdRepository,
    private val jdExperienceRecommendationRepository: JdExperienceRecommendationRepository,
) {

    // JD 하드 삭제(status와 무관한 영구 삭제). 경험추천은 jdId 캐시라 함께 제거한다.
    // 워크스페이스 접근 권한은 API 계층에서 검증. 밖 JD는 404(리소스 숨김).
    @Transactional
    fun deleteJd(workspaceId: Long, publicId: String) {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(publicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($publicId)입니다")
        jdExperienceRecommendationRepository.deleteByJdId(jd.id)
        jdRepository.deleteById(jd.id)
    }

}
