package com.untitled.api.application.member

import com.untitled.domain.domain.member.MemberNotFoundException
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Deprecated(message = "테스트용으로 제거 예정")
@Controller
class MemberQueryResolver {

    private val members = listOf(
        MemberResponse(memberId = "member-1", name = "1번 유저"),
        MemberResponse(memberId = "member-2", name = "2번 유저"),
    )

    @QueryMapping
    fun member(
        @Argument @Valid request: MemberGetRequest,
    ): MemberResponse {
        return members.find { it.memberId == request.memberId }
            ?: throw MemberNotFoundException("등록되지 않은 멤버(${request.memberId}) 입니다")
    }

    @QueryMapping
    fun members(): List<MemberResponse> {
        return members
    }

}
