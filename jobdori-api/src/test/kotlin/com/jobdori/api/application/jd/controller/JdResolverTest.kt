package com.jobdori.api.application.jd.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.jd.AnalyzeGuestJdService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jd.GuestJdAnalysisResult
import com.jobdori.core.application.jd.JdRegisterResult
import com.jobdori.core.application.jd.RegisterJdService
import com.jobdori.core.application.jdinsight.GetJdInsightService
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jdinsight.JdInsight
import com.jobdori.core.domain.workspace.Workspace
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.verify
import org.springframework.context.annotation.Import
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
    private val analyzeGuestJdService: AnalyzeGuestJdService,
    @MockkBean
    @MockkBean
    private val getJdService: GetJdService,
    @MockkBean
    private val getJdInsightService: GetJdInsightService,
    @MockkBean
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
        // workspaceId "ws-1"의 소유자 검증 통과 → 내부 id 10L로 스코프
        every { workspaceAccessValidationService.validateAccessible("ws-1", 1L) } returns
            Workspace(id = 10L, publicId = "ws-1", ownerUserId = 1L)
    }

    "URL로 등록하면 추출된 단일 JD를 반환한다" {
        every { registerJdService.registerByUrl(10L, "https://example.com/jd") } returns
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

        verify(exactly = 1) { registerJdService.registerByUrl(10L, "https://example.com/jd") }
    }

    "붙여넣기 본문에 여러 공고가 있으면 후보 목록을 반환한다" {
        val multiBody = "여러 공고가 섞인 본문 ".repeat(50)   // @Size(min) 통과용 유효 길이
        every { registerJdService.registerByText(10L, multiBody) } returns
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

        verify(exactly = 1) { registerJdService.registerByText(10L, multiBody) }
    }

    "비로그인 상태로 URL을 분석하면 추출 결과와 인사이트를 저장 없이 반환한다" {
        every { analyzeGuestJdService.analyzeByUrl("https://example.com/jd") } returns
            GuestJdAnalysisResult.Analyzed(
                jd = graphQlJd(companyName = "잡도리", positionTitle = "백엔드 개발자"),
                insight = JdInsight(
                    id = 0L,
                    jdId = 0L,
                    keyPoints = "주도적으로 문제를 정의할 사람을 원해요.",
                    strategy = "정의·해결 사례를 강조하세요.",
                ),
            )

        // 인증 헤더 없이(비로그인) 호출한다
        graphQlTester
            .document(
                """
                mutation {
                  analyzeGuestJd(request: { sourceUrl: "https://example.com/jd" }) {
                    analysis {
                      companyName
                      positionTitle
                      insight { keyPoints strategy }
                    }
                    candidates { title }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("analyzeGuestJd.analysis.companyName").entity<String>().isEqualTo("잡도리")
            .path("analyzeGuestJd.analysis.insight.keyPoints").entity<String>().isEqualTo("주도적으로 문제를 정의할 사람을 원해요.")
            .path("analyzeGuestJd.candidates").valueIsNull()

        verify(exactly = 1) { analyzeGuestJdService.analyzeByUrl("https://example.com/jd") }
    }

    "비로그인 분석 본문에 여러 공고가 있으면 후보 목록을 반환한다" {
        val multiBody = "여러 공고가 섞인 본문 ".repeat(50)
        every { analyzeGuestJdService.analyzeByText(multiBody) } returns
            GuestJdAnalysisResult.MultiplePostings(
                listOf(JdPosting(title = "백엔드 개발자", body = "본문 A")),
            )

        graphQlTester
            .document(
                """
                mutation {
                  analyzeGuestJd(request: { body: "$multiBody" }) {
                    analysis { companyName }
                    candidates { title body }
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("analyzeGuestJd.analysis").valueIsNull()
            .path("analyzeGuestJd.candidates[0].title").entity<String>().isEqualTo("백엔드 개발자")

        verify(exactly = 1) { analyzeGuestJdService.analyzeByText(multiBody) }
    }

    "publicId로 워크스페이스의 JD 단건을 조회한다" {
        every { getJdService.getJd(10L, "jd-pub-1") } returns graphQlJd(publicId = "jd-pub-1", companyName = "잡도리")

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jd(workspaceId: "ws-1", id: "jd-pub-1") {
                    jdId
                    companyName
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jd.jdId").entity<String>().isEqualTo("jd-pub-1")
            .path("jd.companyName").entity<String>().isEqualTo("잡도리")

        verify(exactly = 1) { getJdService.getJd(10L, "jd-pub-1") }
    }

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
    }

    "JD의 AI 인사이트(공고 핵심·지원 전략)를 조회한다" {
        every { getJdInsightService.getOrGenerate(10L, "jd-pub-1") } returns
            JdInsight(
                id = 1L,
                jdId = 100L,
                keyPoints = "사용자 경험을 주도적으로 이끌 사람을 원해요.",
                strategy = "사용자 중심 문제를 정의·해결한 사례를 강조하면 좋겠어요.",
            )

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jdInsight(workspaceId: "ws-1", jdId: "jd-pub-1") {
                    keyPoints
                    strategy
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jdInsight.keyPoints").entity<String>().isEqualTo("사용자 경험을 주도적으로 이끌 사람을 원해요.")
            .path("jdInsight.strategy").entity<String>().isEqualTo("사용자 중심 문제를 정의·해결한 사례를 강조하면 좋겠어요.")

        verify(exactly = 1) { getJdInsightService.getOrGenerate(10L, "jd-pub-1") }
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
)
