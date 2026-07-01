package com.jobdori.core.application.auth

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.service.UserCreator
import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.service.WorkspaceCreator
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder

class AuthSignUpServiceTest : StringSpec({

    val userCreator = mockk<UserCreator>()
    val workspaceCreator = mockk<WorkspaceCreator>()
    val service = AuthSignUpService(
        userCreator = userCreator,
        workspaceCreator = workspaceCreator,
    )

    "신규 가입 유저와 워크스페이스를 생성한다" {
        // given
        val user = User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        )
        every {
            userCreator.create(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
                name = "홍길동",
                profileImageUrl = "https://lh3.googleusercontent.com/profile",
            )
        } returns user
        every { workspaceCreator.create(ownerUserId = 10L) } returns Workspace(
            id = 20L,
            publicId = "8f13f49e-132a-47b7-b704-d7eec18fd44b",
            ownerUserId = 10L,
        )

        // when & then
        service.signUp(
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "google-user-id",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        ) shouldBe user

        verifyOrder {
            userCreator.create(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
                name = "홍길동",
                profileImageUrl = "https://lh3.googleusercontent.com/profile",
            )
            workspaceCreator.create(ownerUserId = 10L)
        }
    }

})
