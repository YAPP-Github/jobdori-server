package com.jobdori.core.domain.profile.repository

import com.jobdori.core.domain.profile.Profile

interface ProfileRepository {

    fun save(profile: Profile): Profile

    fun findByWorkspaceId(workspaceId: Long): Profile?

}
