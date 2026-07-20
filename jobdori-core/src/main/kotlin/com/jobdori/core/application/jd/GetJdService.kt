package com.jobdori.core.application.jd

import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdSortType
import com.jobdori.core.domain.jd.JdStatus
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetJdService(
    private val jdRepository: JdRepository,
) {

    // status가 null이면 전체, 아니면 해당 상태만(AR0001 진행 중/완료 섹션). 목록이 작아 in-memory 필터.
    @Transactional(readOnly = true)
    fun getJds(workspaceId: Long, sort: JdSortType, status: JdStatus? = null): List<Jd> =
        jdRepository.findAllByWorkspaceId(workspaceId)
            .filter { status == null || it.status == status }
            .sortedWith(sort.comparator)

    /** 워크스페이스 밖 JD는 404(리소스 숨김). 워크스페이스 접근 권한은 API 계층에서 검증. */
    @Transactional(readOnly = true)
    fun getJd(workspaceId: Long, publicId: String): Jd =
        jdRepository.findByPublicIdAndWorkspaceId(publicId, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($publicId)입니다")

    @Transactional(readOnly = true)
    fun getJd(workspaceId: Long, id: Long): Jd =
        jdRepository.findByIdAndWorkspaceId(id, workspaceId)
            ?: throw JdNotFoundException("등록되지 않은 JD($id)입니다")

    @Transactional(readOnly = true)
    fun getJds(workspaceId: Long, ids: Collection<Long>): List<Jd> =
        if (ids.isEmpty()) emptyList()
        else jdRepository.findAllByIdsAndWorkspaceId(ids.distinct(), workspaceId)

}
