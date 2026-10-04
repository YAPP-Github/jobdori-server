package com.jobdori.core.application.experiencequestionroom.result

data class ExperienceQuestionRoomChatResult(
    val block: Block = Block(),
    val feedback: Feedback = Feedback(),
) {
    data class Block(val title: String = "", val bullets: List<String> = emptyList())
    data class Feedback(val fit: String = "", val improvement: String = "")
}
