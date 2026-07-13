package com.jobdori.infrastructure.persistence

import com.jobdori.core.support.crypto.StringEncryptor
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@Import(StringEncryptor::class)
@ActiveProfiles("test")
@SpringBootTest(
    properties = [
        "spring.config.import=classpath:core.yml,classpath:rdb.yml",
    ]
)
annotation class IntegrationTest
