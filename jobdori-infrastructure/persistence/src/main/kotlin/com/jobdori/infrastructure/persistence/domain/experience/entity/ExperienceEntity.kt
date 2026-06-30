package com.jobdori.infrastructure.persistence.domain.experience.entity

import com.jobdori.common.json.JsonUtils
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.FreeExperienceContents
import com.jobdori.core.domain.experience.StarExperienceContents
import com.jobdori.infrastructure.persistence.support.jpa.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal

@Table(name = "experience_v1")
@Entity
class ExperienceEntity(
    @Column(nullable = false)
    var workspaceId: Long,

    @Column(nullable = false)
    var projectId: Long,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    var tags: List<String>,

    @Column(nullable = false, length = 150)
    var title: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var contentsType: ExperienceContentsType,

    @Column(nullable = false, columnDefinition = "text")
    var contents: String,

    @Column(nullable = false, precision = 20, scale = 10)
    var displayOrder: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var status: ExperienceStatus,
) : AuditableEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L

    fun update(domain: Experience) {
        workspaceId = domain.workspaceId
        projectId = domain.projectId
        tags = domain.tags
        title = domain.title
        contentsType = domain.contents.type
        contents = toContentsJson(domain.contents)
        displayOrder = domain.displayOrder
        status = domain.status
    }

    fun toDomain() = Experience(
        id = id,
        workspaceId = workspaceId,
        projectId = projectId,
        tags = tags,
        title = title,
        contents = parseContents(contentsType, contents),
        displayOrder = displayOrder,
        status = status,
    )

    companion object {
        fun from(domain: Experience) = ExperienceEntity(
            workspaceId = domain.workspaceId,
            projectId = domain.projectId,
            tags = domain.tags,
            title = domain.title,
            contentsType = domain.contents.type,
            contents = toContentsJson(domain.contents),
            displayOrder = domain.displayOrder,
            status = domain.status,
        ).also { it.id = domain.id }

        private fun toContentsJson(contents: ExperienceContents): String {
            return when (contents.type) {
                ExperienceContentsType.STAR -> JsonUtils.toJson(
                    requireNotNull(contents.star) { "STAR contents require star payload" },
                )

                ExperienceContentsType.FREE -> JsonUtils.toJson(
                    requireNotNull(contents.free) { "FREE contents require free payload" },
                )
            }
        }

        private fun parseContents(
            contentsType: ExperienceContentsType,
            contents: String,
        ): ExperienceContents {
            return when (contentsType) {
                ExperienceContentsType.STAR -> {
                    val star = requireNotNull(JsonUtils.toObject(contents, StarExperienceContents::class.java))
                    ExperienceContents.star(
                        situation = star.situation,
                        task = star.task,
                        action = star.action,
                        result = star.result,
                    )
                }

                ExperienceContentsType.FREE -> {
                    val free = requireNotNull(JsonUtils.toObject(contents, FreeExperienceContents::class.java))
                    ExperienceContents.free(content = free.content)
                }
            }
        }
    }

}
