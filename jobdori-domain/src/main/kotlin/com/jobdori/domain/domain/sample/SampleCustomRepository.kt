package com.jobdori.domain.domain.sample

interface SampleCustomRepository {

    fun findByName(name: String): SampleEntity?

}
