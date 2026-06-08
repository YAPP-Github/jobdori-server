package com.jobdori.api

import io.kotest.core.annotation.Tags
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Tags("docs-test")
@AutoConfigureRestDocs
internal annotation class DocsTest
