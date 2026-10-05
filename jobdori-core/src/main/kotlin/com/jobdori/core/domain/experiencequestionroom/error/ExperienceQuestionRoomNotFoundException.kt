package com.jobdori.core.domain.experiencequestionroom.error

import com.jobdori.common.error.BaseException

data class ExperienceQuestionRoomNotFoundException(override val message: String) : BaseException(message, ExperienceQuestionRoomErrorCode.E404_EXPERIENCE_QUESTION_ROOM_NOT_FOUND)
data class ExperienceQuestionRoomMessageNotFoundException(override val message: String) : BaseException(message, ExperienceQuestionRoomErrorCode.E404_EXPERIENCE_QUESTION_ROOM_MESSAGE_NOT_FOUND)
