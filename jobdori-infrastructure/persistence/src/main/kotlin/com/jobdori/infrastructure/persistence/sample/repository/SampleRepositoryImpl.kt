package com.jobdori.infrastructure.persistence.sample.repository

import com.jobdori.core.domain.sample.repository.SampleRepository
import com.jobdori.core.domain.sample.Sample
import com.jobdori.infrastructure.persistence.sample.mapper.SamplePersistenceMapper
import org.springframework.stereotype.Repository

@Repository
class SampleRepositoryImpl(
    private val jpaRepository: SampleJpaRepository,
    private val mapper: SamplePersistenceMapper,
    private val sampleCustomRepository: SampleCustomRepository
): SampleRepository {
    override fun save(sample: Sample): Sample {
        val entity = if (sample.id == 0L) {
            // 신규: 새 엔티티 저장 → INSERT
            mapper.toEntity(sample)
        } else {
            // 수정: 영속 엔티티를 조회해 필드만 갱신 → dirty checking으로 UPDATE.
            // (새 엔티티를 merge하지 않으므로 createdAt 등 감사 필드가 보존된다)
            jpaRepository.findById(sample.id)
                .orElseThrow { IllegalStateException("저장 대상 샘플(${sample.id})이 존재하지 않습니다") }
                .apply { name = sample.name.value }
        }
        return mapper.toDomain(jpaRepository.save(entity))
    }
    override fun findById(id: Long): Sample? = jpaRepository.findById(id).map(mapper::toDomain).orElse(null)
    override fun findByName(name: String): Sample? = sampleCustomRepository.findByName(name)?.let(mapper::toDomain)
    override fun findAll(): List<Sample> = jpaRepository.findAll().map(mapper::toDomain)
    override fun deleteById(id: Long): Boolean {
        if (!jpaRepository.existsById(id)) return false
        jpaRepository.deleteById(id); return true
    }
}
