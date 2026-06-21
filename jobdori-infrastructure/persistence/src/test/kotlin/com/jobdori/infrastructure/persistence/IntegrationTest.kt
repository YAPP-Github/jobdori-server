package com.jobdori.infrastructure.persistence

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "spring.config.import=classpath:rdb.yml"
    ]
)
annotation class IntegrationTest
