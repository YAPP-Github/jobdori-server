package com.jobdori.api.application.jdrecommendation.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.core.application.jdrecommendation.GetJdRecommendationService
import com.jobdori.core.domain.jdrecommendation.JdRecommendation
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag
import com.jobdori.common.model.SliceResult
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity
import java.time.LocalDateTime

@GraphQLTest(JdRecommendationQueryResolver::class)
internal class JdRecommendationQueryResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val getJdRecommendationService: GetJdRecommendationService,
) : StringSpec({

    "추천 공고 태그 목록과 이름을 조회한다" {
        graphQlTester
            .document(
                """
                query {
                  jdRecommendationTags {
                    tag
                    name
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jdRecommendationTags[0].tag").entity<String>().isEqualTo("POPULAR")
            .path("jdRecommendationTags[0].name").entity<String>().isEqualTo("인기 공고")
            .path("jdRecommendationTags[2].tag").entity<String>().isEqualTo("TECH_COMPANY")
            .path("jdRecommendationTags[2].name").entity<String>().isEqualTo("네카라쿠배")
    }

    "tags 기본값이 빈 배열이면 전체 추천 공고를 조회한다" {
        every {
            getJdRecommendationService.getRecommendations(emptyList(), null, 10)
        } returns SliceResult(
            items = listOf(recommendation(tags = null)),
            nextCursor = null,
        )

        graphQlTester
            .document(
                """
                query {
                  jdRecommendations {
                    recommendations {
                      id
                      tags
                      companyName
                      recruitmentStartAt
                      recruitmentEndAt
                    }
                    cursor { hasNext nextCursor }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jdRecommendations.recommendations[0].companyName")
            .entity<String>().isEqualTo("잡도리")
            .path("jdRecommendations.recommendations[0].tags")
            .valueIsNull()
            .path("jdRecommendations.cursor.hasNext")
            .entity<Boolean>().isEqualTo(false)

        verify(exactly = 1) {
            getJdRecommendationService.getRecommendations(emptyList(), null, 10)
        }
    }

    "여러 tags와 cursor, size를 서비스에 전달한다" {
        val tags = listOf(JdRecommendationTag.POPULAR, JdRecommendationTag.TECH_COMPANY)
        every {
            getJdRecommendationService.getRecommendations(tags, "1.5:4", 2)
        } returns SliceResult(
            items = listOf(recommendation(tags = tags, id = 5L, displayOrder = 2.0)),
            nextCursor = "2.0:5",
        )

        val result = graphQlTester
            .document(
                """
                query {
                  jdRecommendations(tags: [POPULAR, TECH_COMPANY], cursor: "1.5:4", size: 2) {
                    recommendations { tags }
                    cursor { hasNext nextCursor }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jdRecommendations.recommendations[0].tags")
            .entityList(JdRecommendationTag::class.java).containsExactly(*tags.toTypedArray())

        result
            .path("jdRecommendations.cursor.nextCursor")
            .entity<String>().isEqualTo("2.0:5")

        verify(exactly = 1) {
            getJdRecommendationService.getRecommendations(tags, "1.5:4", 2)
        }
    }
})

private fun recommendation(
    id: Long = 1L,
    tags: List<JdRecommendationTag>?,
    displayOrder: Double = 1.0,
) = JdRecommendation(
    id = id,
    tags = tags,
    companyName = "잡도리",
    title = "백엔드 개발자",
    recruitmentStartAt = LocalDateTime.of(2026, 9, 9, 0, 0),
    recruitmentEndAt = LocalDateTime.of(2026, 9, 27, 23, 59),
    sourceUrl = "https://example.com/jobs/1",
    displayOrder = displayOrder,
    isActive = true,
    createdAt = null,
)
