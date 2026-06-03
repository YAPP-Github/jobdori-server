package com.untitled.domain.domain.sample

interface SampleCustomRepository {

    fun findByName(name: String): SampleEntity?

}
