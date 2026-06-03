package com.untitled.domain.support.spring

import com.untitled.common.json.JsonUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper

@Configuration
class JsonConfig {

    @Bean
    fun jsonMapper(): JsonMapper {
        return JsonUtils.DEFAULT_JSON_MAPPER
    }

}
