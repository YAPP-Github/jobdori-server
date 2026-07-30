package com.jobdori.api.application.jd.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.jd.CompleteJdService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jd.JdRegisterResult
import com.jobdori.core.application.jd.RegisterJdService
import com.jobdori.core.domain.experience.error.ExperienceErrorCode
import com.jobdori.core.domain.experience.error.ExperienceRequiredException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdSortType
import com.jobdori.core.domain.jd.JdStatus
import com.jobdori.core.domain.workspace.Workspace
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
import org.springframework.graphql.execution.ErrorType
import org.springframework.graphql.test.tester.ExecutionGraphQlServiceTester
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(JdResolver::class)
@Import(UserIdArgumentGraphqlResolver::class)
internal class JdResolverTest(
    private val graphQlTester: GraphQlTester,
    @MockkBean
    private val accessTokenService: AccessTokenService,
    @MockkBean
    private val registerJdService: RegisterJdService,
    @MockkBean
    private val completeJdService: CompleteJdService,
    @MockkBean
    private val getJdService: GetJdService,
    @MockkBean
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
        // workspaceId "ws-1"의 소유자 검증 통과 -> 내부 id 10L로 스코프
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
    }

    "URL로 등록하면 추출된 단일 JD를 반환한다" {
        every { registerJdService.registerByUrl(10L, 1L, "https://example.com/jd") } returns
            JdRegisterResult.Registered(
                graphQlJd(publicId = "jd-pub-1", companyName = "잡도리", positionTitle = "백엔드 개발자"),
            )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  registerJd(workspaceId: "ws-1", request: { sourceUrl: "https://example.com/jd" }) {
                    jd {
                      jdId
                      companyName
                      positionTitle
                    }
                    candidates {
                      title
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("registerJd.jd.jdId").entity<String>().isEqualTo("jd-pub-1")
            .path("registerJd.jd.companyName").entity<String>().isEqualTo("잡도리")
            .path("registerJd.jd.positionTitle").entity<String>().isEqualTo("백엔드 개발자")
            .path("registerJd.candidates").valueIsNull()

        verify(exactly = 1) { registerJdService.registerByUrl(10L, 1L, "https://example.com/jd") }
    }

    "경험이 없으면 JD 등록의 422 에러를 BAD_REQUEST로 매핑한다" {
        every {
            registerJdService.registerByUrl(10L, 1L, "https://example.com/jd")
        } throws ExperienceRequiredException()

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  registerJd(workspaceId: "ws-1", request: { sourceUrl: "https://example.com/jd" }) {
                    jd {
                      jdId
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .errors()
            .satisfy { errors ->
                errors shouldHaveSize 1
                val error = errors.first()
                error.errorType shouldBe ErrorType.BAD_REQUEST
                error.message shouldBe ExperienceErrorCode.E422_EXPERIENCE_REQUIRED.message
                error.extensions shouldContain (
                    "code" to ExperienceErrorCode.E422_EXPERIENCE_REQUIRED.code
                )
            }

        verify(exactly = 1) { registerJdService.registerByUrl(10L, 1L, "https://example.com/jd") }
    }

    "붙여넣기 본문에 여러 공고가 있으면 후보 목록을 반환한다" {
        val multiBody = "여러 공고가 섞인 본문 ".repeat(50)   // @Size(min) 통과용 유효 길이
        every { registerJdService.registerByText(10L, 1L, multiBody) } returns
            JdRegisterResult.MultiplePostings(
                listOf(
                    JdPosting(title = "백엔드 개발자", body = "본문 A"),
                    JdPosting(title = "프론트 개발자", body = "본문 B"),
                ),
            )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  registerJd(workspaceId: "ws-1", request: { body: "$multiBody" }) {
                    jd {
                      jdId
                    }
                    candidates {
                      title
                      body
                    }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("registerJd.jd").valueIsNull()
            .path("registerJd.candidates[0].title").entity<String>().isEqualTo("백엔드 개발자")
            .path("registerJd.candidates[0].body").entity<String>().isEqualTo("본문 A")
            .path("registerJd.candidates[1].title").entity<String>().isEqualTo("프론트 개발자")

        verify(exactly = 1) { registerJdService.registerByText(10L, 1L, multiBody) }
    }

    "publicId로 워크스페이스의 JD 단건을 조회한다" {
        every { getJdService.getJd(10L, "jd-pub-1") } returns graphQlJd(
            publicId = "jd-pub-1",
            companyName = "잡도리",
            keyPoints = "주도적으로 문제를 정의할 사람을 원해요.",
            strategy = "문제 해결 경험을 강조하세요.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jd(workspaceId: "ws-1", id: "jd-pub-1") {
                    jdId
                    companyName
                    coreCompetencies
                    insight { strategy }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jd.jdId").entity<String>().isEqualTo("jd-pub-1")
            .path("jd.companyName").entity<String>().isEqualTo("잡도리")
            .path("jd.coreCompetencies").entityList(String::class.java).containsExactly("데이터 기반 개선", "협업")
            .path("jd.insight.strategy").entity<String>().isEqualTo("문제 해결 경험을 강조하세요.")

        verify(exactly = 1) { getJdService.getJd(10L, "jd-pub-1") }
    }

    "하위 호환 API로 JD 인사이트를 조회한다" {
        every { getJdService.getJd(10L, "jd-pub-1") } returns graphQlJd(
            publicId = "jd-pub-1",
            keyPoints = "주도적으로 문제를 정의할 사람을 원해요.",
            strategy = "문제 해결 경험을 강조하세요.",
        )

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jdInsight(workspaceId: "ws-1", jdId: "jd-pub-1") {
                    strategy
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jdInsight.strategy").entity<String>().isEqualTo("문제 해결 경험을 강조하세요.")

        verify(exactly = 1) { getJdService.getJd(10L, "jd-pub-1") }
    }

    "sort를 생략하면 최신순(LATEST)으로 status 필터 없이 JD 목록을 조회한다" {
        every { getJdService.getJds(10L, JdSortType.LATEST, null) } returns
            listOf(graphQlJd(publicId = "jd-pub-2", companyName = "잡도리"))

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jds(workspaceId: "ws-1") {
                    jdId
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jds[0].jdId").entity<String>().isEqualTo("jd-pub-2")

        verify(exactly = 1) { getJdService.getJds(10L, JdSortType.LATEST, null) }
    }

    "sort로 가나다순(NAME)을 지정해 JD 목록을 조회한다" {
        every { getJdService.getJds(10L, JdSortType.NAME, null) } returns
            listOf(graphQlJd(publicId = "jd-pub-3", companyName = "가나다"))

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jds(workspaceId: "ws-1", sort: NAME) {
                    jdId
                    companyName
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jds[0].companyName").entity<String>().isEqualTo("가나다")

        verify(exactly = 1) { getJdService.getJds(10L, JdSortType.NAME, null) }
    }

    "status로 진행 중(IN_PROGRESS) JD만 필터링해 조회한다" {
        every { getJdService.getJds(10L, JdSortType.LATEST, JdStatus.IN_PROGRESS) } returns
            listOf(graphQlJd(publicId = "jd-pub-4", status = JdStatus.IN_PROGRESS))

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jds(workspaceId: "ws-1", status: IN_PROGRESS) {
                    jdId
                    status
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jds[0].jdId").entity<String>().isEqualTo("jd-pub-4")
            .path("jds[0].status").entity<String>().isEqualTo("IN_PROGRESS")

        verify(exactly = 1) { getJdService.getJds(10L, JdSortType.LATEST, JdStatus.IN_PROGRESS) }
    }

    "JD를 완료 처리하면 COMPLETED 상태의 JD를 반환한다" {
        every { completeJdService.markCompleted(10L, "jd-pub-1") } returns
            graphQlJd(publicId = "jd-pub-1", status = JdStatus.COMPLETED)

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  markJdCompleted(workspaceId: "ws-1", id: "jd-pub-1") {
                    jdId
                    status
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("markJdCompleted.jdId").entity<String>().isEqualTo("jd-pub-1")
            .path("markJdCompleted.status").entity<String>().isEqualTo("COMPLETED")

        verify(exactly = 1) { completeJdService.markCompleted(10L, "jd-pub-1") }
    }

})

private fun authenticatedTester(graphQlTester: GraphQlTester): GraphQlTester {
    val builder = graphQlTester.mutate() as ExecutionGraphQlServiceTester.Builder<*>
    return builder.configureExecutionInput { _, executionInputBuilder ->
        executionInputBuilder.graphQLContext(
            mapOf(AuthGraphQlContext.AUTHORIZATION to "Bearer access-token"),
        ).build()
    }.build()
}

private fun graphQlJd(
    id: Long = 1L,
    publicId: String = "jd-pub",
    companyName: String = "잡도리",
    positionTitle: String = "백엔드 개발자",
    coreCompetencies: List<String> = listOf("데이터 기반 개선", "협업"),
    keyPoints: String = "공고 핵심",
    strategy: String = "지원 전략",
    status: JdStatus = JdStatus.IN_PROGRESS,
) = Jd(
    id = id,
    publicId = publicId,
    workspaceId = 10L,
    sourceUrl = "https://example.com/jd",
    companyName = companyName,
    positionTitle = positionTitle,
    companyIntro = "채용 도우미 팀",
    responsibilities = emptyList(),
    requiredExperiences = emptyList(),
    preferredExperiences = emptyList(),
    hiringProcess = emptyList(),
    coreCompetencies = coreCompetencies,
    keyPoints = keyPoints,
    strategy = strategy,
    status = status,
)
