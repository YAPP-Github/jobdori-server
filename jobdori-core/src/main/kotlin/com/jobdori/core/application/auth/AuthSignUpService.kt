package com.jobdori.core.application.auth

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.service.UserCreator
import com.jobdori.core.domain.workspace.service.WorkspaceCreator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthSignUpService(
    private val userCreator: UserCreator,
    private val workspaceCreator: WorkspaceCreator,
) {

    @Transactional
    fun signUp(
        provider: UserIdentityProvider,
        providerUserId: String,
        name: String,
        profileImageUrl: String?,
    ): User {
        val user = userCreator.create(
            provider = provider,
            providerUserId = providerUserId,
            name = name,
            profileImageUrl = profileImageUrl,
        )
        workspaceCreator.create(ownerUserId = user.id)
        return user
    }

}
