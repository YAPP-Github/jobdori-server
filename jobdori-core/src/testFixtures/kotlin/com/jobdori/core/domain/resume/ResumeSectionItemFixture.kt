package com.jobdori.core.domain.resume

import java.time.LocalDateTime

object ResumeSectionItemFixture {

    fun create(
        id: Long = 0L,
        sectionId: Long = 1L,
        payload: ResumeSectionItemPayload = ResumeBasicInfoPayload(
            name = "홍길동",
            email = "hong@example.com",
            phone = "010-0000-0000",
        ),
        displayOrder: Double = 0.0,
        visible: Boolean = true,
        createdAt: LocalDateTime = LocalDateTime.of(2026, 1, 1, 0, 0),
        updatedAt: LocalDateTime = createdAt,
    ) = ResumeSectionItem(
        id = id,
        sectionId = sectionId,
        payload = payload,
        displayOrder = displayOrder,
        visible = visible,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

}
