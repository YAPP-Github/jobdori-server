package com.untitled.api.libs

import com.untitled.api.DocsTest
import com.untitled.common.error.CommonErrorCode
import io.kotest.core.spec.style.StringSpec
import java.io.File

@DocsTest
internal class ErrorCodeApiDocsTest : StringSpec({

    "에러코드 Asciidoctor 생성" {
        val file = File("src/docs/asciidoc/restapi/common/error.adoc")
        if (!file.exists()) {
            file.createNewFile()
        }
        var asciidoctorText =
            """
                [cols="10%,20%,60%", options="header"]
                |===
                | Status Code | Error Code | Description

                """.trimIndent()

        CommonErrorCode.entries.forEach { errorCode ->
            asciidoctorText +=
                """
                        | ${errorCode.httpStatusCode} | ${errorCode.code} | ${errorCode.description}

                        """.trimIndent()
        }

        asciidoctorText += "|===\n".trim()

        file.printWriter().use { out -> out.println(asciidoctorText) }
    }

})
