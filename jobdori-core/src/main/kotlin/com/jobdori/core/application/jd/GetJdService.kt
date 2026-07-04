package com.jobdori.core.application.jd

import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetJdService(
    private val jdRepository: JdRepository,
) {

    @Transactional(readOnly = true)
    fun getMine(userId: Long): List<Jd> = jdRepository.findAllByUserId(userId)

    /** 소유자 검증 — 없거나 타인 JD는 404(리소스 숨김) */
    @Transactional(readOnly = true)
    fun getJd(userId: Long, publicId: String): Jd =
        jdRepository.findByPublicIdAndUserId(publicId, userId)
            ?: throw JdNotFoundException("등록되지 않은 JD($publicId)입니다")

}
