package com.jobdori.core.domain.experiencequestionroom

data class ExperienceQuestionRoom(
    val id: Long,
    val jdId: Long,
    val cards: List<ExperienceQuestionRoomCard>,
) {
    companion object {
        fun newInstance(jdId: Long, cards: List<ExperienceQuestionRoomCard>) =
            ExperienceQuestionRoom(id = 0L, jdId = jdId, cards = cards)
    }
}
