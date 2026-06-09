package com.jobdori.domain.support.jpa

import com.jobdori.domain.DomainRoot
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan(basePackageClasses = [DomainRoot::class])
@EnableJpaRepositories(basePackageClasses = [DomainRoot::class])
@EnableJpaAuditing
@Configuration
class JpaConfig
