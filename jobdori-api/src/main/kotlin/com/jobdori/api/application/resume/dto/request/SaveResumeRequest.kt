package com.jobdori.api.application.resume.dto.request

import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.resume.ResumeTemplate
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionItemSaveCommand
import com.jobdori.core.domain.resume.service.command.ResumeSectionSaveCommand
import java.math.BigDecimal

data class SaveResumeRequest(
    val targetJdId: Long?,
    val title: String,
    val template: ResumeTemplate,
    val status: ResumeStatusType,
    val sections: List<SaveResumeSectionRequest>,
) {

    fun toCommand(): ResumeSaveCommand {
        validateSectionDisplayOrders()
        return ResumeSaveCommand(
            targetJdId = targetJdId,
            title = title,
            template = template,
            status = status.toDomain(),
            sections = sections.map { it.toCommand() },
        )
    }

    private fun validateSectionDisplayOrders() {
        val duplicatedDisplayOrders = sections
            .map { BigDecimal(it.displayOrder) }
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

}

data class SaveResumeSectionRequest(
    val sectionId: Long?,
    val displayOrder: String,
    val visible: Boolean,
    val items: List<SaveResumeSectionItemRequest>,
) {

    fun toCommand(): ResumeSectionSaveCommand {
        val itemCommands = items.map { it.toCommand() }
        if (itemCommands.isEmpty()) {
            throw InvalidArgumentsException("섹션에는 최소 하나 이상의 item이 필요합니다.")
        }
        validateItemDisplayOrders(itemCommands)

        val sectionTypes = itemCommands.map { it.payload.type }.toSet()
        if (sectionTypes.size != 1) {
            throw InvalidArgumentsException("하나의 섹션에는 동일한 타입의 item만 입력할 수 있습니다. [sectionTypes=$sectionTypes]")
        }

        return ResumeSectionSaveCommand(
            sectionId = sectionId,
            type = sectionTypes.single(),
            displayOrder = BigDecimal(displayOrder),
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

}

data class SaveResumeSectionItemRequest(
    val itemId: Long?,
    val displayOrder: String,
    val visible: Boolean,
    val payload: ResumeSectionItemPayloadRequest,
) {

    fun toCommand() = ResumeSectionItemSaveCommand(
        itemId = itemId,
        payload = payload.toPayload(),
        displayOrder = BigDecimal(displayOrder),
        visible = visible,
    )

}

private fun List<BigDecimal>.findDuplicateDisplayOrders(): Set<BigDecimal> {
    val seen = mutableSetOf<BigDecimal>()
    return filterNot { seen.add(it.stripTrailingZeros()) }.toSet()
}
