package com.jobdori.infrastructure.persistence.domain.notion.repository

import com.jobdori.core.domain.notion.NotionConnection
import com.jobdori.core.domain.notion.repository.NotionConnectionRepository
import com.jobdori.infrastructure.persistence.domain.notion.entity.NotionConnectionEntity
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class NotionConnectionRepositoryImpl(
    private val jpaRepository: NotionConnectionJpaRepository,
) : NotionConnectionRepository {

    @Transactional
    override fun save(connection: NotionConnection): NotionConnection {
        val entity = if (connection.id == 0L) {
            jpaRepository.findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
                workspaceId = connection.workspaceId,
                notionWorkspaceId = connection.notionWorkspaceId,
                botId = connection.botId,
            )?.also {
                it.update(connection)
            } ?: NotionConnectionEntity.from(connection)
        } else {
            jpaRepository.findByIdAndWorkspaceId(connection.id, connection.workspaceId)?.also {
                it.update(connection)
            } ?: NotionConnectionEntity.from(connection)
        }

        return jpaRepository.save(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): NotionConnection? {
        return jpaRepository.findByIdAndWorkspaceId(id, workspaceId)?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAllByWorkspaceId(workspaceId: Long, cursorId: Long?, size: Int): List<NotionConnection> {
        val pageable = PageRequest.of(0, size)
        val entities = if (cursorId == null) {
            jpaRepository.findAllByWorkspaceIdOrderByIdDesc(workspaceId, pageable)
        } else {
            jpaRepository.findAllByWorkspaceIdAndIdLessThanOrderByIdDesc(
                workspaceId = workspaceId,
                id = cursorId,
                pageable = pageable,
            )
        }
        return entities
            .map { it.toDomain() }
    }

    @Transactional(readOnly = true)
    override fun findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
        workspaceId: Long,
        notionWorkspaceId: String,
        botId: String,
    ): NotionConnection? {
        return jpaRepository.findByWorkspaceIdAndNotionWorkspaceIdAndBotId(
            workspaceId = workspaceId,
            notionWorkspaceId = notionWorkspaceId,
            botId = botId,
        )?.toDomain()
    }

    @Transactional
    override fun deleteByIdAndWorkspaceId(id: Long, workspaceId: Long) {
        jpaRepository.deleteByIdAndWorkspaceId(id, workspaceId)
    }

}
