package com.jobdori.api.application.resume.dto.request

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionSaveCommand
import jakarta.validation.Valid
import java.math.BigDecimal

data class SaveResumeRequest(
    val targetJdId: Long?,
    val title: String,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    @field:Valid
    val sections: List<SaveResumeSectionRequest>,
) {

    fun toCommand(): ResumeSaveCommand {
        val parsedSections = sections.map { it.parseDisplayOrder() }
        validateSectionDisplayOrders(parsedSections)
        validateSectionIds(parsedSections)
        return ResumeSaveCommand(
            targetJdId = targetJdId,
            title = title,
            template = template,
            status = status.toDomain(),
            sections = parsedSections.map { it.toCommand() },
        )
    }

    private fun validateSectionDisplayOrders(parsedSections: List<ParsedSaveResumeSectionRequest>) {
        val duplicatedDisplayOrders = parsedSections
            .map { it.displayOrderDecimal }
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

    private fun validateSectionIds(parsedSections: List<ParsedSaveResumeSectionRequest>) {
        val sectionIds = parsedSections.mapNotNull { it.sectionId }
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

data class SaveResumeSectionRequest(
    val sectionId: Long?,
    val displayOrder: String,
    val visible: Boolean,
    @field:Valid
    val items: List<SaveResumeSectionItemRequest>,
) {

    fun parseDisplayOrder(): ParsedSaveResumeSectionRequest {
        val displayOrderDecimal = try {
            BigDecimal(displayOrder)
        } catch (e: NumberFormatException) {
            throw InvalidArgumentsException(
                message = "displayOrder는 유효한 숫자여야 합니다.",
                cause = e,
                details = listOf(
                    ErrorDetail(
                        field = "sections.displayOrder",
                        reason = "displayOrder 값 '$displayOrder'는 유효한 숫자가 아닙니다.",
                    ),
                ),
            )
        }
        val parsedItems = items.map { it.parseDisplayOrder() }
        return ParsedSaveResumeSectionRequest(
            sectionId = sectionId,
            displayOrderDecimal = displayOrderDecimal,
            visible = visible,
            parsedItems = parsedItems,
        )
    }

}

data class ParsedSaveResumeSectionRequest(
    val sectionId: Long?,
    val displayOrderDecimal: BigDecimal,
    val visible: Boolean,
    val parsedItems: List<ParsedSaveResumeSectionItemRequest>,
) {

    fun toCommand(): ResumeSectionSaveCommand {
        val itemCommands = parsedItems.map { it.toCommand() }
        if (itemCommands.isEmpty()) {
            throw InvalidArgumentsException("섹션에는 최소 하나 이상의 item이 필요합니다.")
        }
        validateItemDisplayOrders(itemCommands)
        validateItemIds()

        val sectionTypes = itemCommands.map { it.payload.type }.toSet()
        if (sectionTypes.size != 1) {
            throw InvalidArgumentsException("하나의 섹션에는 동일한 타입의 item만 입력할 수 있습니다. [sectionTypes=$sectionTypes]")
        }

        return ResumeSectionSaveCommand(
            sectionId = sectionId,
            type = sectionTypes.single(),
            displayOrder = displayOrderDecimal,
            visible = visible,
            items = itemCommands,
        )
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
        val itemIds = parsedItems.mapNotNull { it.itemId }
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
    val itemId: Long?,
    val displayOrder: String,
    val visible: Boolean,
    @field:Valid
    val payload: ResumeSectionItemPayloadRequest,
) {

    fun parseDisplayOrder(): ParsedSaveResumeSectionItemRequest {
        val displayOrderDecimal = try {
            BigDecimal(displayOrder)
        } catch (e: NumberFormatException) {
            throw InvalidArgumentsException(
                message = "displayOrder는 유효한 숫자여야 합니다.",
                cause = e,
                details = listOf(
                    ErrorDetail(
                        field = "sections.items.displayOrder",
                        reason = "displayOrder 값 '$displayOrder'는 유효한 숫자가 아닙니다.",
                    ),
                ),
            )
        }
        return ParsedSaveResumeSectionItemRequest(
            itemId = itemId,
            displayOrderDecimal = displayOrderDecimal,
            visible = visible,
            payload = payload,
        )
    }

}

data class ParsedSaveResumeSectionItemRequest(
    val itemId: Long?,
    val displayOrderDecimal: BigDecimal,
    val visible: Boolean,
    val payload: ResumeSectionItemPayloadRequest,
) {

    fun toCommand() = ResumeSectionItemSaveCommand(
        itemId = itemId,
        payload = payload.toPayload(),
        displayOrder = displayOrderDecimal,
        visible = visible,
    )

}

private fun List<BigDecimal>.findDuplicateDisplayOrders(): Set<BigDecimal> {
    val seen = mutableSetOf<BigDecimal>()
    return filterNot { seen.add(it.stripTrailingZeros()) }.toSet()
}
