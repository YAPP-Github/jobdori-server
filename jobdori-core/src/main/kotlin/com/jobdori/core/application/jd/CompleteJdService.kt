package com.jobdori.core.application.jd

import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdStatus
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 이력서 생성 완료 시 JD 상태를 COMPLETED로 전환(AR0001 진행 중 → 완료). Resume 모듈이 완료 시점에 호출한다.
@Service
class CompleteJdService(
    private val jdRepository: JdRepository,
) {

    // 워크스페이스 접근 권한은 API 계층에서 검증. 밖 JD는 404(리소스 숨김). 멱등: 이미 완료여도 그대로 반환.
    @Transactional
    fun markCompleted(workspaceId: Long, publicId: String): Jd {
        val jd = jdRepository.findByPublicIdAndWorkspaceId(publicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($publicId)입니다")
        if (jd.status == JdStatus.COMPLETED) return jd
        return jdRepository.save(jd.copy(status = JdStatus.COMPLETED))
    }

}
