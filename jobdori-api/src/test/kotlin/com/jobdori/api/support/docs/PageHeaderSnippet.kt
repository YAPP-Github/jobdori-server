package com.jobdori.api.support.docs

import org.springframework.restdocs.generate.RestDocumentationGenerator
import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.snippet.TemplatedSnippet
import java.util.Collections

class PageHeaderSnippet(description: String) : TemplatedSnippet(
    "request-path",
    Collections.singletonMap<String, Any>("description", description),
) {

    override fun createModel(operation: Operation): Map<String, Any> {
        val model = HashMap<String, Any>()

        model["method"] = operation.request.method
        val path = operation.attributes[RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE] as String
        model["path"] = RestDocsUtils.withApiServletPath(path)

        return model
    }

    companion object {
        fun pageHeaderSnippet(description: String = ""): PageHeaderSnippet {
            return PageHeaderSnippet(description)
        }
    }

}
