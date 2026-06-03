package com.untitled.domain

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan(basePackageClasses = [DomainRoot::class])
@Configuration
class DomainRoot
