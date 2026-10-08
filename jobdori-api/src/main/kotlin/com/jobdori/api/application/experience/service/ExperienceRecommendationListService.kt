package com.jobdori.api.application.experience.service

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.request.ExperienceListTab
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.common.error.ErrorDetail
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.experiencerecommendation.GetExperienceRecommendationService
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencequestionroom.error.ExperienceQuestionRoomNotFoundException
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.core.domain.jd.error.JdNotFoundException
import org.springframework.stereotype.Service

@Service
class ExperienceRecommendationListService(
    private val getExperienceRecommendationService: GetExperienceRecommendationService,
    private val experienceReader: ExperienceReader,
    private val experienceProjectReader: ExperienceProjectReader,
) {

    // null이면 호출부의 기본 목록 응답을 그대로 쓴다(jdId 없음, MANUAL 탭).
    fun getList(
        workspaceId: Long,
        jdId: String?,
        questionRoomId: String?,
        tab: ExperienceListTab?,
        projectId: Long?,
        cursor: String?,
        size: Int,
        includeProjects: Boolean,
    ): ExperienceListResponse? {
        if (jdId == null) return null
        if (tab == null) throw invalidArgument("tab", "JD 매칭 조회에는 tab이 필요합니다.")
        if (tab == ExperienceListTab.MANUAL) return null
        if (questionRoomId == null) throw invalidArgument("questionRoomId", "AI_RECOMMENDATION 탭에는 questionRoomId가 필요합니다.")
        val offset = cursor?.let { it.toIntOrNull()?.takeIf { value -> value >= 0 } }
            ?: if (cursor == null) 0 else throw invalidArgument("cursor", "AI_RECOMMENDATION 탭의 cursor는 0 이상의 정수여야 합니다.")

        val recommendation = try {
            getExperienceRecommendationService.getOrRefresh(workspaceId, jdId, questionRoomId)
        } catch (e: JdNotFoundException) {
            throw e
        } catch (e: ExperienceQuestionRoomNotFoundException) {
            throw e
        } catch (e: Exception) {
            log.warn(e) { "질문 카드 매칭 조회 실패, 매칭 없이 응답: jdId=$jdId, questionRoomId=$questionRoomId" }
            null
        }

        val ranked: List<Pair<Experience, RecommendedExperience?>>
        val hasNext: Boolean
        if (recommendation == null) {
            // 대체 응답도 offset 커서를 유지한다. id 커서를 섞으면 같은 탭에서 다음 페이지 요청이 어긋난다.
            val limit = (offset.toLong() + size).coerceAtMost(Int.MAX_VALUE - 1L).toInt()
            val fetched = experienceReader.getExperiences(workspaceId, projectId, null, limit)
            ranked = fetched.items.map { it to null }
            hasNext = fetched.nextCursor != null
        } else {
            val experienceById = recommendation.experiences.associateBy { it.id }
            ranked = recommendation.items
                .sortedWith(
                    compareByDescending<RecommendedExperience> { it.matchRate }
                        .thenByDescending { it.experienceId },
                )
                .mapNotNull { match -> experienceById[match.experienceId]?.let { it to match } }
                .filter { (experience, _) -> projectId == null || experience.projectId == projectId }
            hasNext = ranked.size - offset > size
        }
        val page = ranked.drop(offset).take(size)

        val projects = if (includeProjects) {
            experienceProjectReader.getProjects(
                workspaceId = workspaceId,
                projectIds = page.map { it.first.projectId },
            ).mapValues { (_, project) -> ExperienceProjectResponse.from(project) }
        } else {
            emptyMap()
        }

        return ExperienceListResponse(
            experiences = page.map { (experience, match) ->
                ExperienceResponse.from(
                    experience = experience,
                    project = projects[experience.projectId],
                    matchRate = match?.matchRate,
                    reason = match?.reason,
                )
            },
            cursor = CursorResponse(nextCursor = if (hasNext) (offset + size).toString() else null),
        )
    }

    private fun invalidArgument(field: String, reason: String) =
        InvalidArgumentsException(message = reason, details = listOf(ErrorDetail(field = field, reason = reason)))

}
