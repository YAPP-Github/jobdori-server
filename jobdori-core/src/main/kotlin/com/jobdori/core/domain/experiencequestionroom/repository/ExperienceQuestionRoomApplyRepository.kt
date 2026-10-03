package com.jobdori.core.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomApply

interface ExperienceQuestionRoomApplyRepository {
    // 같은 이력서에 대한 적용을 줄 세우기 위해 이력서 행을 비관적 락으로 잡는다. 호출자의 트랜잭션 안에서 불러야 한다.
    fun lockResume(resumeId: Long, workspaceId: Long): Boolean

    fun findByQuestionRoomIdAndResumeId(questionRoomId: String, resumeId: Long): ExperienceQuestionRoomApply?

    fun findAppliedQuestionRoomIds(questionRoomIds: Collection<String>): Set<String>

    fun save(apply: ExperienceQuestionRoomApply): ExperienceQuestionRoomApply
}
