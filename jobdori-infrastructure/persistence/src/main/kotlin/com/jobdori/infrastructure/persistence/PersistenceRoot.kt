package com.jobdori.infrastructure.persistence

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan(basePackageClasses = [PersistenceRoot::class])
@Configuration
class PersistenceRoot
