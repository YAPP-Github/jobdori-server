package com.jobdori.infrastructure.persistence.support.converter

import com.jobdori.core.support.crypto.StringEncryptor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class EncryptedStringConverterTest : StringSpec({

    val encryptor = mockk<StringEncryptor>()
    val converter = EncryptedStringConverter(encryptor)

    beforeTest { clearMocks(encryptor) }

    "엔티티 속성을 암호화하여 데이터베이스 컬럼 값으로 변환한다" {
        every { encryptor.encrypt("plain-text") } returns "encrypted-text"

        converter.convertToDatabaseColumn("plain-text") shouldBe "encrypted-text"

        verify(exactly = 1) { encryptor.encrypt("plain-text") }
    }

    "데이터베이스 컬럼 값을 복호화하여 엔티티 속성으로 변환한다" {
        every { encryptor.decrypt("encrypted-text") } returns "plain-text"

        converter.convertToEntityAttribute("encrypted-text") shouldBe "plain-text"

        verify(exactly = 1) { encryptor.decrypt("encrypted-text") }
    }

    "엔티티 속성이 null이면 암호화하지 않고 null을 반환한다" {
        converter.convertToDatabaseColumn(null).shouldBeNull()

        verify(exactly = 0) { encryptor.encrypt(any()) }
    }

    "데이터베이스 컬럼 값이 null이면 복호화하지 않고 null을 반환한다" {
        converter.convertToEntityAttribute(null).shouldBeNull()

        verify(exactly = 0) { encryptor.decrypt(any()) }
    }

})
