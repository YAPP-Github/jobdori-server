package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeSectionItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResumeSectionItemJpaRepository : JpaRepository<ResumeSectionItemEntity, Long> {

    fun findAllBySectionIdInOrderBySectionIdAscDisplayOrderAscIdAsc(
        sectionIds: Collection<Long>,
    ): List<ResumeSectionItemEntity>

    fun findAllBySectionIdIn(
        sectionIds: Collection<Long>,
    ): List<ResumeSectionItemEntity>

}
