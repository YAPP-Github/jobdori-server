package com.untitled.api.support.graphql

data class GraphQLValidationErrorDetail(
    val field: String,
    val reason: String,
)
