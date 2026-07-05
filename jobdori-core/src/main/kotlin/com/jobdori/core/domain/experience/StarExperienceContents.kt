package com.jobdori.core.domain.experience

data class StarExperienceContents(
    val situation: String,
    val task: String,
    val action: String,
    val result: String,
) : ExperienceContents
