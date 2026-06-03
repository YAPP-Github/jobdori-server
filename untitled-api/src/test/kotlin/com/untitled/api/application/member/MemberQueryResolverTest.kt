package com.untitled.api.application.member

import com.untitled.api.GraphQLTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.graphql.test.tester.entity

@GraphQLTest(MemberQueryResolver::class)
internal class MemberQueryResolverTest(
    private val graphQlTester: GraphQlTester,
) : StringSpec({

    "memberId로 회원을 조회한다" {
        graphQlTester.documentName("member")
            .variable("request", mapOf("memberId" to "member-1"))
            .execute()
            .path("member.memberId").entity<String>().isEqualTo("member-1")
            .path("member.name").entity<String>().isEqualTo("1번 유저")
    }

    "등록된 회원 목록을 조회한다" {
        graphQlTester.documentName("members")
            .execute()
            .path("members[0].memberId").entity<String>().isEqualTo("member-1")
            .path("members[0].name").entity<String>().isEqualTo("1번 유저")
            .path("members[1].memberId").entity<String>().isEqualTo("member-2")
            .path("members[1].name").entity<String>().isEqualTo("2번 유저")
    }

    "등록되지 않은 memberId이면 GraphQL error를 반환한다" {
        graphQlTester.documentName("member")
            .variable("request", mapOf("memberId" to "member-3"))
            .execute()
            .errors()
            .satisfy { errors ->
                errors shouldHaveSize 1
                val error = errors.first()
                error.path shouldBe "member"
                error.extensions shouldContain ("code" to "member_not_found")
            }
    }

})
