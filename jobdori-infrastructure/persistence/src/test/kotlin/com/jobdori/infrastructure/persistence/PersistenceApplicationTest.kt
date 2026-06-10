package com.jobdori.infrastructure.persistence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationPropertiesScan(basePackageClasses = [PersistenceRoot::class])
@SpringBootApplication(scanBasePackageClasses = [PersistenceRoot::class])
internal class PersistenceApplicationTest
