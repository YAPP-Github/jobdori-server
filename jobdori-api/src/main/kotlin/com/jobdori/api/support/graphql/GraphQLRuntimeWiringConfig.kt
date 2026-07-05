package com.jobdori.api.support.graphql

import com.jobdori.api.application.experience.dto.response.contents.FreeExperienceContentsResponse
import com.jobdori.api.application.experience.dto.response.contents.StarExperienceContentsResponse
import graphql.schema.TypeResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import kotlin.reflect.KClass

@Configuration
class GraphQLRuntimeWiringConfig {

    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer {
        return RuntimeWiringConfigurer { builder ->
            builder.type("ExperienceContents") { typeWiring ->
                typeWiring.typeResolver(
                    byResponseType(
                        StarExperienceContentsResponse::class to "STAR",
                        FreeExperienceContentsResponse::class to "FREE",
                    ),
                )
            }
        }
    }

    private fun byResponseType(
        vararg mappings: Pair<KClass<*>, String>,
    ): TypeResolver {
        return TypeResolver { environment ->
            val value = environment.getObject<Any>()
            val typeName = mappings
                .firstOrNull { (responseType, _) -> responseType.isInstance(value) }
                ?.second

            typeName?.let { environment.schema.getObjectType(it) }
        }
    }

}
