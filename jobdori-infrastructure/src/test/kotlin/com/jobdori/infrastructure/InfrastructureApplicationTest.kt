package com.jobdori.infrastructure

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationPropertiesScan(basePackageClasses = [InfrastructureRoot::class])
@SpringBootApplication(scanBasePackageClasses = [InfrastructureRoot::class])
internal class InfrastructureApplicationTest
