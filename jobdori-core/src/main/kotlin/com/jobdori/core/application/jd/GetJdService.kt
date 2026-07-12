package com.jobdori.core.application.jd

import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdSortType
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetJdService(
    private val jdRepository: JdRepository,
) {

    @Transactional(readOnly = true)
    fun getJds(workspaceId: Long, sort: JdSortType): List<Jd> =
        jdRepository.findAllByWorkspaceId(workspaceId).sortedWith(sort.comparator)

    /** 워크스페이스 밖 JD는 404(리소스 숨김). 워크스페이스 접근 권한은 API 계층에서 검증. */
    @Transactional(readOnly = true)
    fun getJd(workspaceId: Long, publicId: String): Jd =
        jdRepository.findByPublicIdAndWorkspaceId(publicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($publicId)입니다")

}
