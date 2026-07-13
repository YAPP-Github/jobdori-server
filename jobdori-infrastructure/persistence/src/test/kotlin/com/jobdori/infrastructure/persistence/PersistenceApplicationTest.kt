package com.jobdori.infrastructure.persistence

import com.jobdori.common.PackageConst
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationPropertiesScan(basePackages = [PackageConst.BASE_PACKAGE])
@SpringBootApplication(scanBasePackageClasses = [PersistenceRoot::class])
internal class PersistenceApplicationTest
