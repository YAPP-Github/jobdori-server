package com.untitled.domain

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationPropertiesScan(basePackageClasses = [DomainRoot::class])
@SpringBootApplication(scanBasePackageClasses = [DomainRoot::class])
internal class DomainApplicationTest
