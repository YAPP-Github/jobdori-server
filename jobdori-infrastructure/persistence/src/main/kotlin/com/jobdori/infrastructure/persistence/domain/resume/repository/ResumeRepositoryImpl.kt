package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeDetailSection
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.repository.ResumeRepository
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeEntity
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeSectionEntity
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeSectionItemEntity
import org.springframework.stereotype.Repository
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

@Repository
class ResumeRepositoryImpl(
    private val resumeJpaRepository: ResumeJpaRepository,
    private val sectionJpaRepository: ResumeSectionJpaRepository,
    private val sectionItemJpaRepository: ResumeSectionItemJpaRepository,
    private val encryptor: StringEncryptor,
) : ResumeRepository {

    @Transactional
    override fun save(resume: Resume): Resume {
        return resumeJpaRepository.save(ResumeEntity.from(resume)).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAllByWorkspaceIdAndStatuses(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
        cursorId: Long?,
        size: Int,
    ): List<Resume> {
        if (statuses.isEmpty()) {
            return emptyList()
        }

        val pageable = PageRequest.of(0, size)
        val entities = if (cursorId == null) {
            resumeJpaRepository.findAllByWorkspaceIdAndStatusInOrderByIdDesc(
                workspaceId = workspaceId,
                statuses = statuses,
                pageable = pageable,
            )
        } else {
            resumeJpaRepository.findAllByWorkspaceIdAndStatusInAndIdLessThanOrderByIdDesc(
                workspaceId = workspaceId,
                statuses = statuses,
                id = cursorId,
                pageable = pageable,
            )
        }
        return entities.map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun countByWorkspaceIdAndStatuses(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): Map<ResumeStatus, Long> {
        if (statuses.isEmpty()) {
            return emptyMap()
        }

        return resumeJpaRepository.countByWorkspaceIdAndStatusIn(
            workspaceId = workspaceId,
            statuses = statuses,
        )
    }

    @Transactional(readOnly = true)
    override fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): Resume? {
        return resumeJpaRepository.findByIdAndWorkspaceIdAndStatusIn(
            id = id,
            workspaceId = workspaceId,
            statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
        )?.toDomain()
    }

    @Transactional
    override fun markCoreCompetencyGenerated(id: Long, workspaceId: Long): Boolean {
        return resumeJpaRepository.markCoreCompetencyGenerated(
            id = id,
            workspaceId = workspaceId,
            statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
        ) == 1
    }

    @Transactional
    override fun resetCoreCompetencyGenerated(id: Long, workspaceId: Long) {
        resumeJpaRepository.resetCoreCompetencyGenerated(
            id = id,
            workspaceId = workspaceId,
        )
    }

    @Transactional(readOnly = true)
    override fun findSectionsByIdAndWorkspaceId(id: Long, workspaceId: Long): ResumeDetail? {
        val resume = findByIdAndWorkspaceId(
            id = id,
            workspaceId = workspaceId,
        ) ?: return null

        val sections = sectionJpaRepository.findAllByResumeIdOrderByDisplayOrderAscIdAsc(
            resumeId = resume.id,
        ).map { it.toDomain() }

        return ResumeDetail(
            resume = resume,
            sections = sections.map { section ->
                ResumeDetailSection(
                    section = section,
                    items = emptyList(),
                )
            },
        )
    }

    @Transactional(readOnly = true)
    override fun findDetailByIdAndWorkspaceId(id: Long, workspaceId: Long): ResumeDetail? {
        val resume = findByIdAndWorkspaceId(
            id = id,
            workspaceId = workspaceId,
        ) ?: return null

        val sections = sectionJpaRepository.findAllByResumeIdOrderByDisplayOrderAscIdAsc(
            resumeId = resume.id,
        ).map { it.toDomain() }

        val sectionIds = sections.map { it.id }
        val itemsBySectionId = if (sectionIds.isEmpty()) {
            emptyMap()
        } else {
            sectionItemJpaRepository.findAllBySectionIdInOrderBySectionIdAscDisplayOrderAscIdAsc(
                sectionIds = sectionIds,
            ).map { it.toDomain(encryptor) }
                .groupBy { it.sectionId }
        }

        return ResumeDetail(
            resume = resume,
            sections = sections.map { section ->
                ResumeDetailSection(
                    section = section,
                    items = itemsBySectionId[section.id].orEmpty(),
                )
            },
        )
    }

    @Transactional
    override fun createDetail(
        workspaceId: Long,
        command: ResumeSaveCommand,
    ): ResumeDetail {
        validateResumeStatus(command.status)
        val savedResume = resumeJpaRepository.save(
            ResumeEntity(
                workspaceId = workspaceId,
                targetJdId = command.targetJdId,
                template = command.template,
                status = command.status,
            ),
        )

        saveSectionsAndItems(
            resumeId = savedResume.id,
            command = command,
        )

        return requireNotNull(findDetailByIdAndWorkspaceId(id = savedResume.id, workspaceId = workspaceId))
    }

    @Transactional
    override fun modifyDetail(
        id: Long,
        workspaceId: Long,
        command: ResumeSaveCommand,
    ): ResumeDetail? {
        validateResumeStatus(command.status)
        val resumeEntity = resumeJpaRepository.findByIdAndWorkspaceIdAndStatusIn(
            id = id,
            workspaceId = workspaceId,
            statuses = listOf(ResumeStatus.COMPLETED, ResumeStatus.DRAFT),
        ) ?: return null

        resumeEntity.targetJdId = command.targetJdId
        resumeEntity.template = command.template
        resumeEntity.status = command.status
        val savedResume = resumeJpaRepository.save(resumeEntity)

        saveSectionsAndItems(
            resumeId = savedResume.id,
            command = command,
        )

        return findDetailByIdAndWorkspaceId(id = savedResume.id, workspaceId = workspaceId)
    }

    private fun validateResumeStatus(status: ResumeStatus) {
        if (status == ResumeStatus.DELETED) {
            throw InvalidArgumentsException(
                message = "이력서를 DELETED 상태로 생성하거나 수정할 수 없습니다.",
            )
        }
    }

    private fun saveSectionsAndItems(
        resumeId: Long,
        command: ResumeSaveCommand,
    ) {
        val existingSections = sectionJpaRepository.findAllByResumeId(
            resumeId = resumeId,
        )
        val existingSectionsById = existingSections.associateBy { it.id }
        val requestedSectionIds = command.sections.mapNotNull { it.sectionId }.toSet()

        val sectionsToDelete = existingSections.filter { it.id !in requestedSectionIds }

        val sectionPairs = command.sections.map { sectionCommand ->
            val section = sectionCommand.sectionId
                ?.let { existingSectionsById[it] }
                ?.also {
                    it.resumeId = resumeId
                    it.type = sectionCommand.type
                    it.displayOrder = sectionCommand.displayOrder
                    it.visible = sectionCommand.visible
                }
                ?: ResumeSectionEntity(
                    resumeId = resumeId,
                    type = sectionCommand.type,
                    displayOrder = sectionCommand.displayOrder,
                    visible = sectionCommand.visible,
                )

            sectionCommand to section
        }

        val savedSectionPairs = sectionPairs.map { (sectionCommand, section) ->
            sectionCommand to sectionJpaRepository.save(section)
        }

        val savedSectionIds = savedSectionPairs.map { (_, section) -> section.id }
        val allSectionIds = (existingSections.map { it.id } + savedSectionIds).toSet()
        val existingItems = if (allSectionIds.isEmpty()) {
            emptyList()
        } else {
            sectionItemJpaRepository.findAllBySectionIdIn(
                sectionIds = allSectionIds,
            )
        }
        val existingItemsById = existingItems.associateBy { it.id }
        val requestedItemIds = command.sections
            .flatMap { it.items }
            .mapNotNull { it.itemId }
            .toSet()

        val itemsToDelete = existingItems.filter { it.id !in requestedItemIds }

        val itemsToSave = savedSectionPairs.flatMap { (sectionCommand, section) ->
            sectionCommand.items.map { itemCommand ->
                itemCommand.itemId
                    ?.let { existingItemsById[it] }
                    ?.also {
                        it.sectionId = section.id
                        it.payloadType = itemCommand.payload.type
                        it.payload = ResumeSectionItemEntity.serializePayload(itemCommand.payload, encryptor)
                        it.displayOrder = itemCommand.displayOrder
                        it.visible = itemCommand.visible
                    }
                    ?: ResumeSectionItemEntity(
                        sectionId = section.id,
                        payloadType = itemCommand.payload.type,
                        payload = ResumeSectionItemEntity.serializePayload(itemCommand.payload, encryptor),
                        displayOrder = itemCommand.displayOrder,
                        visible = itemCommand.visible,
                    )
            }
        }

        if (itemsToDelete.isNotEmpty()) {
            sectionItemJpaRepository.deleteAll(itemsToDelete)
        }
        if (sectionsToDelete.isNotEmpty()) {
            sectionJpaRepository.deleteAll(sectionsToDelete)
        }
        if (itemsToSave.isNotEmpty()) {
            sectionItemJpaRepository.saveAll(itemsToSave)
        }
    }

}
