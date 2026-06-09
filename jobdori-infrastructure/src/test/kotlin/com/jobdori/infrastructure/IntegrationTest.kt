package com.jobdori.infrastructure

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

@ActiveProfiles("test")
@TestPropertySource(properties = ["openai.api-key=test"])
@SpringBootTest
annotation class IntegrationTest
