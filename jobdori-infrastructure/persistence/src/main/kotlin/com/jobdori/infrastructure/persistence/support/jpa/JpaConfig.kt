package com.jobdori.infrastructure.persistence.support.jpa

import com.jobdori.infrastructure.persistence.PersistenceRoot
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan(basePackageClasses = [PersistenceRoot::class])
@EnableJpaRepositories(basePackageClasses = [PersistenceRoot::class])
@EnableJpaAuditing
@Configuration
class JpaConfig
