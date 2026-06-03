package com.untitled.domain.member

import com.untitled.domain.IntegrationTest
import com.untitled.domain.domain.member.MemberFixture
import com.untitled.domain.domain.member.MemberRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@IntegrationTest
class MemberRepositoryTest(
    private val memberRepository: MemberRepository,
) : StringSpec({

    afterEach {
        memberRepository.deleteAll()
    }

    "새로운 멤버를 등록합니다" {
        // given
        val member = MemberFixture.entity(name = "무제")

        // when
        memberRepository.save(member)

        // then
        val members = memberRepository.findAll()
        members shouldHaveSize 1
        members[0].name shouldBe "무제"
    }

    "이름으로 멤버를 조회합니다" {
        // given
        memberRepository.save(MemberFixture.entity(name = "무제"))

        // when
        val result = memberRepository.findByName("무제")

        // then
        result shouldNotBe null
        result!!.name shouldBe "무제"
    }

    "존재하지 않는 이름으로 조회하면 null을 반환합니다" {
        // when
        val result = memberRepository.findByName("없는멤버")

        // then
        result.shouldBeNull()
    }

})
