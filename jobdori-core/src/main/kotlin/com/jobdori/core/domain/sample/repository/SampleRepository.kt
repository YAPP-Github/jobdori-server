package com.jobdori.core.domain.sample.repository

import com.jobdori.core.domain.sample.model.Sample

interface SampleRepository {
    fun save(sample: Sample): Sample
    fun findById(id: Long): Sample?
    fun findByName(name: String): Sample?
    fun findAll(): List<Sample>
    fun deleteById(id: Long): Boolean
}
