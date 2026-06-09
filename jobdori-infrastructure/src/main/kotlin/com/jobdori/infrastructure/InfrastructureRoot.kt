package com.jobdori.infrastructure

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan(basePackageClasses = [InfrastructureRoot::class])
@Configuration
class InfrastructureRoot
