package com.jobdori.api.application.resume.dto.request

import com.jobdori.api.application.resume.dto.ResumeOptimizationMode
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.json.JsonUtils
import com.jobdori.core.domain.resume.ResumeSectionType
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.error.ResumeSectionItemRequiredException
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionSaveCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive

data class SaveResumeRequest(
    val targetJdId: String?,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    @field:Valid
    val sections: List<SaveResumeSectionRequest>,
    val optimizationMode: ResumeOptimizationMode = ResumeOptimizationMode.NONE,
) {

    fun toCommand(resolvedTargetJdId: Long? = null): ResumeSaveCommand {
        validateSectionDisplayOrders()
        validateSectionIds()
        return ResumeSaveCommand(
            targetJdId = resolvedTargetJdId,
            template = template,
            status = status.toDomain(),
            sections = sections.map { it.toCommand() },
        )
    }

    private fun validateSectionDisplayOrders() {
        val duplicatedDisplayOrders = sections
            .map { it.displayOrder }
            .findDuplicateDisplayOrders()

        if (duplicatedDisplayOrders.isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "섹션 displayOrder는 중복될 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.displayOrder",
                        reason = "같은 이력서 안에서 섹션 displayOrder는 중복될 수 없습니다.",
                    ),
                ),
            )
        }
    }

    private fun validateSectionIds() {
        val sectionIds = sections.mapNotNull { it.sectionId }
        val duplicateSectionIds = sectionIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

        if (duplicateSectionIds.isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "섹션 sectionId는 중복될 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.sectionId",
                        reason = "같은 이력서 안에서 섹션 sectionId는 중복될 수 없습니다. [duplicateSectionIds=$duplicateSectionIds]",
                    ),
                ),
            )
        }
    }

}

data class CreateResumeRequest(
    val targetJdId: String?,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    @field:Valid
    val sections: List<SaveResumeSectionRequest>,
    val optimizationMode: ResumeOptimizationMode = ResumeOptimizationMode.JOB_SPECIFIC,
) {

    fun toCommand(resolvedTargetJdId: Long? = null): ResumeSaveCommand {
        if (sections.map { it.displayOrder }.findDuplicateDisplayOrders().isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "섹션 displayOrder는 중복될 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.displayOrder",
                        reason = "같은 이력서 안에서 섹션 displayOrder는 중복될 수 없습니다.",
                    ),
                ),
            )
        }

        return ResumeSaveCommand(
            targetJdId = resolvedTargetJdId,
            template = template,
            status = status.toDomain(),
            sections = sections.map { it.toCommand(allowDefaultItems = true) },
        )
    }
}

data class SaveResumeSectionRequest(
    @field:Positive
    val sectionId: Long?,
    val type: ResumeSectionType,
    val displayOrder: Double,
    val visible: Boolean,
    @field:Valid
    val items: List<SaveResumeSectionItemRequest>,
    val useDefaultItems: Boolean = false,
) {

    fun toCommand(allowDefaultItems: Boolean = false): ResumeSectionSaveCommand {
        if (useDefaultItems && !allowDefaultItems) {
            throw InvalidArgumentsException(
                message = "기본 아이템 생성은 이력서 생성 시에만 사용할 수 있습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.useDefaultItems",
                        reason = "이력서 수정 요청에서는 useDefaultItems를 사용할 수 없습니다.",
                    ),
                ),
            )
        }
        if (useDefaultItems && items.isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "기본 아이템을 생성할 때 items를 함께 지정할 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.items",
                        reason = "useDefaultItems가 true이면 items는 비어 있어야 합니다.",
                    ),
                ),
            )
        }
        if (useDefaultItems && type == ResumeSectionType.EXPERIENCE) {
            throw InvalidArgumentsException(
                message = "경험 섹션은 기본 아이템을 생성할 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.useDefaultItems",
                        reason = "EXPERIENCE 섹션에는 기본 아이템 생성 규칙이 없습니다.",
                    ),
                ),
            )
        }

        val itemCommands = items.map { it.toCommand() }
        if (itemCommands.isEmpty() && visible && !useDefaultItems) {
            throw ResumeSectionItemRequiredException("이력서 섹션 내의 아이템이 비어있습니다 [${JsonUtils.toJson(this)}}}")
        }
        validateItemDisplayOrders(itemCommands)
        validateItemIds()
        validateItemPayloadTypes(itemCommands)

        return ResumeSectionSaveCommand(
            sectionId = sectionId,
            type = type,
            displayOrder = displayOrder,
            visible = visible,
            items = itemCommands,
        )
    }

    private fun validateItemPayloadTypes(itemCommands: List<ResumeSectionItemSaveCommand>) {
        val mismatchedTypes = itemCommands
            .map { it.payload.type }
            .filter { it != type }
            .toSet()

        if (mismatchedTypes.isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "섹션 타입과 아이템 payload 타입이 일치하지 않습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.items.payload",
                        reason = "섹션 타입과 아이템 payload 타입은 같아야 합니다. [sectionType=$type, itemTypes=$mismatchedTypes]",
                    ),
                ),
            )
        }
    }

    private fun validateItemDisplayOrders(itemCommands: List<ResumeSectionItemSaveCommand>) {
        val duplicatedDisplayOrders = itemCommands
            .map { it.displayOrder }
            .findDuplicateDisplayOrders()

        if (duplicatedDisplayOrders.isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "섹션 아이템 displayOrder는 중복될 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.items.displayOrder",
                        reason = "같은 섹션 안에서 아이템 displayOrder는 중복될 수 없습니다.",
                    ),
                ),
            )
        }
    }

    private fun validateItemIds() {
        val itemIds = items.mapNotNull { it.itemId }
        val duplicateItemIds = itemIds.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

        if (duplicateItemIds.isNotEmpty()) {
            throw InvalidArgumentsException(
                message = "섹션 아이템 itemId는 중복될 수 없습니다.",
                details = listOf(
                    ErrorDetail(
                        field = "sections.items.itemId",
                        reason = "같은 섹션 안에서 아이템 itemId는 중복될 수 없습니다. [duplicateItemIds=$duplicateItemIds]",
                    ),
                ),
            )
        }
    }

}

data class SaveResumeSectionItemRequest(
    @field:Positive
    val itemId: Long?,
    val displayOrder: Double,
    val visible: Boolean,
    @field:Valid
    val payload: ResumeSectionItemPayloadRequest,
) {

    fun toCommand() = ResumeSectionItemSaveCommand(
        itemId = itemId,
        payload = payload.toPayload(),
        displayOrder = displayOrder,
        visible = visible,
    )

}

private fun List<Double>.findDuplicateDisplayOrders(): Set<Double> {
    val seen = mutableSetOf<Double>()
    return filterNot { seen.add(it) }.toSet()
}
