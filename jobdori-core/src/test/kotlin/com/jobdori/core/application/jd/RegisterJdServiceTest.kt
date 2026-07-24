package com.jobdori.core.application.jd

import com.jobdori.core.application.ai.jd.ExtractJdMetaService
import com.jobdori.core.application.ai.jd.ExtractJdStrategyService
import com.jobdori.core.application.ai.jd.SplitJdPostingsService
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPolicy
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSections
import com.jobdori.core.domain.profile.service.ProfileReader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class RegisterJdServiceTest : StringSpec({

    val crawler = mockk<JdCrawlerClient>()
    val splitter = mockk<SplitJdPostingsService>()
    val extractJdMetaService = mockk<ExtractJdMetaService>()
    val extractJdStrategyService = mockk<ExtractJdStrategyService>()
    val profileReader = mockk<ProfileReader>()
    val jdRepository = mockk<JdRepository>()
    val service = RegisterJdService(
        crawler = crawler,
        splitter = splitter,
        extractJdMetaService = extractJdMetaService,
        extractJdStrategyService = extractJdStrategyService,
        profileReader = profileReader,
        jdRepository = jdRepository,
    )
    val body = "가".repeat(JdPolicy.MIN_JD_BODY_LENGTH)
    val profile = Profile(1L, 10L, "홍길동", null, null, null)
    val profileDetail = ProfileDetail(
        profile = profile,
        sections = ProfileSections(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList()),
    )

    beforeTest {
        clearMocks(
            crawler,
            splitter,
            extractJdMetaService,
            extractJdStrategyService,
            profileReader,
            jdRepository,
        )
        every { splitter.split(body) } returns listOf(JdPosting(body = body))
        every { extractJdMetaService.extractFromBody(body) } returns JdMetaResult(
            isJobPosting = true,
            companyName = "잡도리",
            positionTitle = "백엔드 개발자",
            responsibilities = listOf("API 개발"),
        )
        every { profileReader.getOrCreateProfile(10L) } returns profile
        every { profileReader.getDetail(profile) } returns profileDetail
    }

    "JD 등록 시 프로필 기반 지원 전략을 생성해 함께 저장한다" {
        every { extractJdStrategyService.generate(any(), profileDetail) } returns "지원 전략"
        every { jdRepository.save(any()) } answers { firstArg<Jd>().copy(id = 1L) }

        val result = service.registerByText(workspaceId = 10L, body = body)

        (result as JdRegisterResult.Registered).jd.strategy shouldBe "지원 전략"
        verify(exactly = 1) { jdRepository.save(match { it.strategy == "지원 전략" }) }
    }

    "지원 전략 생성에 실패하면 JD를 저장하지 않는다" {
        every {
            extractJdStrategyService.generate(any(), profileDetail)
        } throws IllegalStateException("지원 전략 생성 실패")

        shouldThrow<IllegalStateException> {
            service.registerByText(workspaceId = 10L, body = body)
        }

        verify(exactly = 0) { jdRepository.save(any()) }
    }

})
