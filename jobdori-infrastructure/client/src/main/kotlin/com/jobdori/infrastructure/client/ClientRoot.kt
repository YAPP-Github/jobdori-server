package com.jobdori.infrastructure.client

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan(basePackageClasses = [ClientRoot::class])
@Configuration
class ClientRoot
