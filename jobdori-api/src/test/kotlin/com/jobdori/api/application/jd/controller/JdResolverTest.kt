package com.jobdori.api.application.jd.controller

import com.jobdori.api.GraphQLTest
import com.jobdori.api.support.auth.graphql.AuthGraphQlContext
import com.jobdori.api.support.auth.graphql.UserIdArgumentGraphqlResolver
import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.application.auth.AccessTokenService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jd.JdRegisterResult
import com.jobdori.core.application.jd.RegisterJdService
import com.jobdori.core.domain.jd.Jd
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
    private val getJdService: GetJdService,
) : StringSpec({

    beforeTest {
        every { accessTokenService.getUserId("access-token") } returns 1L
    }

    "URL로 등록하면 추출된 단일 JD를 반환한다" {
        every { registerJdService.registerByUrl(1L, "https://example.com/jd") } returns
            JdRegisterResult.Registered(
                graphQlJd(publicId = "jd-pub-1", companyName = "잡도리", positionTitle = "백엔드 개발자"),
            )

        authenticatedTester(graphQlTester)
            .document(
                """
                mutation {
                  registerJd(request: { sourceUrl: "https://example.com/jd" }) {
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

        verify(exactly = 1) { registerJdService.registerByUrl(1L, "https://example.com/jd") }
    }

    "붙여넣기 본문에 여러 공고가 있으면 후보 목록을 반환한다" {
        val multiBody = "여러 공고가 섞인 본문 ".repeat(50)   // @Size(min) 통과용 유효 길이
        every { registerJdService.registerByText(1L, multiBody) } returns
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
                  registerJd(request: { body: "$multiBody" }) {
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

        verify(exactly = 1) { registerJdService.registerByText(1L, multiBody) }
    }

    "publicId로 내 JD 단건을 조회한다" {
        every { getJdService.getJd(1L, "jd-pub-1") } returns graphQlJd(publicId = "jd-pub-1", companyName = "잡도리")

        authenticatedTester(graphQlTester)
            .document(
                """
                query {
                  jd(id: "jd-pub-1") {
                    jdId
                    companyName
                  }
                }
                """.trimIndent(),
            )
            .execute()
            .path("jd.jdId").entity<String>().isEqualTo("jd-pub-1")
            .path("jd.companyName").entity<String>().isEqualTo("잡도리")

        verify(exactly = 1) { getJdService.getJd(1L, "jd-pub-1") }
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
    userId = 1L,
    sourceUrl = "https://example.com/jd",
    companyName = companyName,
    positionTitle = positionTitle,
    companyIntro = "채용 도우미 팀",
    responsibilities = emptyList(),
    requiredExperiences = emptyList(),
    preferredExperiences = emptyList(),
    hiringProcess = emptyList(),
)
