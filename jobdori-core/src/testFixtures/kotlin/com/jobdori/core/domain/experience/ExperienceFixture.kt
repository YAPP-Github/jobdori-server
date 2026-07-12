package com.jobdori.core.domain.experience

object ExperienceFixture {

    fun create(
        id: Long = 0L,
        workspaceId: Long = 1L,
        projectId: Long = 1L,
        tags: List<String> = listOf("Kotlin", "Spring"),
        title: String = "경험",
        contents: ExperienceContents = ExperienceContents.free("경험 내용"),
        displayOrder: Double = 0.0,
        status: ExperienceStatus = ExperienceStatus.ACTIVE,
    ) = Experience(
        id = id,
        workspaceId = workspaceId,
        projectId = projectId,
        tags = tags,
        title = title,
        contents = contents,
        displayOrder = displayOrder,
        status = status,
    )

}
