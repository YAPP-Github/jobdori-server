package com.jobdori.infrastructure.support.jpa

import com.jobdori.infrastructure.InfrastructureRoot
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan(basePackageClasses = [InfrastructureRoot::class])
@EnableJpaRepositories(basePackageClasses = [InfrastructureRoot::class])
@EnableJpaAuditing
@Configuration
class JpaConfig
