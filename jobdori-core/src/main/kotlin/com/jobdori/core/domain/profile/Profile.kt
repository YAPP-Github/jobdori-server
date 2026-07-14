package com.jobdori.core.domain.profile

data class Profile(
    val id: Long,
    val workspaceId: Long,
    val name: String?,
    val phone: String?,
    val email: String?,
    val coreCompetency: String?,
) {

    companion object {
        fun newInstance(workspaceId: Long) = Profile(
            id = 0L,
            workspaceId = workspaceId,
            name = null,
            phone = null,
            email = null,
            coreCompetency = null,
        )
    }

}
