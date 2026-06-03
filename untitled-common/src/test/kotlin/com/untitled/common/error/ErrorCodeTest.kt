package com.untitled.common.error

import io.kotest.core.spec.style.StringSpec
import org.junit.jupiter.api.Assertions.fail
import java.util.regex.Pattern

class ErrorCodeTest : StringSpec({

    "에러코드는 중복되지 않아야 한다" {
        // given
        val errorCodes = mutableSetOf<String>()

        // when & then
        for (errorCode in ErrorCode.entries) {
            if (errorCodes.contains(errorCode.code)) {
                fail<String>("중복되는 에러($errorCode)의 코드(${errorCode.code})가 존재합니다")
            }
            errorCodes += errorCode.code
        }
    }

    "에러코드는 소문자와 언더바로 이루어져야 한다" {
        // given
        val pattern = Pattern.compile("^[a-z][a-z_]*[a-z]$")

        // when & then
        for (errorCode in ErrorCode.entries) {
            if (!pattern.matcher(errorCode.code).matches()) {
                fail<String>("사용할 수 없는 에러($errorCode)의 코드(${errorCode.code})가 존재합니다. 에러 코드는 소문자와 언더바(_)로만 구성되어야 합니다")
            }
        }
    }

})
