package com.jobdori.core.domain.consent.repository

interface ConsentSheetExposureRepository {

    fun existsByUserId(userId: Long): Boolean

    fun save(userId: Long)
}
