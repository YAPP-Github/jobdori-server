package com.jobdori.infrastructure.persistence.support.converter

import com.jobdori.core.support.crypto.StringEncryptor
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.springframework.stereotype.Component

@Component
@Converter(autoApply = false)
class EncryptedStringConverter(
    private val encryptor: StringEncryptor,
) : AttributeConverter<String, String> {

    override fun convertToDatabaseColumn(attribute: String?): String? = attribute?.let(encryptor::encrypt)

    override fun convertToEntityAttribute(dbData: String?): String? = dbData?.let(encryptor::decrypt)

}
