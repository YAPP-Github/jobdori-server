package com.untitled.domain.domain.sample

import org.springframework.data.jpa.repository.JpaRepository

interface SampleRepository : JpaRepository<SampleEntity, Long>, SampleCustomRepository
