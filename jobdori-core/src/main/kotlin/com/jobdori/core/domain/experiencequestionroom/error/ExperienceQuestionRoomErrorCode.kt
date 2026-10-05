package com.jobdori.core.domain.experiencequestionroom.error

import com.jobdori.common.error.ErrorCode

enum class ExperienceQuestionRoomErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
) : ErrorCode {
    E404_EXPERIENCE_QUESTION_ROOM_NOT_FOUND(404, "experience_question_room_not_found", "경험 질문 대화방을 찾지 못했어요. 목록에서 다시 확인해 주세요.", "대화방을 찾을 수 없는 경우"),
    E404_EXPERIENCE_QUESTION_ROOM_MESSAGE_NOT_FOUND(404, "experience_question_room_message_not_found", "대화방 메시지를 찾지 못했어요. 다시 확인해 주세요.", "AI 메시지를 찾을 수 없는 경우"),
}
