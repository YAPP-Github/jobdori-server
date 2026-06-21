package com.jobdori.core.support.jwt

import com.jobdori.common.json.JsonUtils
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class HmacSha256JwtCodec(
    private val properties: JwtCodecProperties,
) : JwtCodec {

    override fun encode(claims: JwtClaims): String {
        require(claims.subject.isNotBlank()) { "JWT subject must not be blank" }
        require(claims.tokenId.isNotBlank()) { "JWT tokenId must not be blank" }
        require(claims.expiresAt.isAfter(claims.issuedAt)) {
            "JWT expiresAt must be after issuedAt"
        }
        require(claims.customClaims.keys.none(REGISTERED_CLAIMS::contains)) {
            "Custom claims must not override registered JWT claims"
        }

        val header = mapOf(
            "alg" to ALGORITHM,
            "typ" to TOKEN_TYPE,
        )
        val payload = buildMap<String, Any> {
            put("sub", claims.subject)
            put("jti", claims.tokenId)
            put("iat", claims.issuedAt.epochSecond)
            put("exp", claims.expiresAt.epochSecond)
            putAll(claims.customClaims)
        }

        val encodedHeader = encodeJson(header)
        val encodedPayload = encodeJson(payload)
        val signingInput = "$encodedHeader.$encodedPayload"
        val signature = BASE64_URL_ENCODER.encodeToString(sign(signingInput))

        return "$signingInput.$signature"
    }

    override fun decode(token: String): JwtClaims {
        val parts = token.split('.')
        if (parts.size != JWT_PART_COUNT || parts.any(String::isBlank)) {
            throw InvalidJwtException(message = "JWT is invalid")
        }

        val signingInput = "${parts[0]}.${parts[1]}"
        val signature = decodeBase64Url(parts[2])
        if (!MessageDigest.isEqual(sign(signingInput), signature)) {
            throw InvalidJwtException(message = "JWT is invalid")
        }

        val header = decodeJson(parts[0])
        if (header["alg"] != ALGORITHM || header["typ"] != TOKEN_TYPE) {
            throw InvalidJwtException(message = "JWT is invalid")
        }

        val payload = decodeJson(parts[1])
        val subject = payload.requiredString("sub")
        val tokenId = payload.requiredString("jti")
        val issuedAt = Instant.ofEpochSecond(payload.requiredLong("iat"))
        val expiresAt = Instant.ofEpochSecond(payload.requiredLong("exp"))

        if (!expiresAt.isAfter(issuedAt)) {
            throw InvalidJwtException(message = "JWT is invalid")
        }
        if (!expiresAt.isAfter(Instant.now())) {
            throw ExpiredJwtException(message = "JWT is expired")
        }

        val customClaims = payload
            .filterKeys { it !in REGISTERED_CLAIMS }
            .mapValues { (_, value) -> value as? String ?: throw InvalidJwtException(message = "JWT is invalid") }

        return JwtClaims(
            subject = subject,
            tokenId = tokenId,
            issuedAt = issuedAt,
            expiresAt = expiresAt,
            customClaims = customClaims,
        )
    }

    private fun encodeJson(value: Any): String =
        BASE64_URL_ENCODER.encodeToString(JsonUtils.toJson(value).toByteArray(UTF_8))

    private fun decodeJson(value: String): Map<String, Any?> {
        val json = decodeBase64Url(value).toString(UTF_8)

        return runCatching {
            @Suppress("UNCHECKED_CAST")
            JsonUtils.toObject(json, Map::class.java) as Map<String, Any?>
        }.getOrElse {
            throw InvalidJwtException(message = "JWT is invalid")
        }
    }

    private fun decodeBase64Url(value: String): ByteArray =
        runCatching {
            BASE64_URL_DECODER.decode(value)
        }.getOrElse {
            throw InvalidJwtException(message = "JWT is invalid")
        }

    private fun sign(value: String): ByteArray =
        Mac.getInstance(HMAC_SHA_256).run {
            init(SecretKeySpec(properties.decodedSecret, HMAC_SHA_256))
            doFinal(value.toByteArray(UTF_8))
        }

    private fun Map<String, Any?>.requiredString(key: String): String =
        (this[key] as? String)
            ?.takeIf(String::isNotBlank)
            ?: throw InvalidJwtException(message = "JWT is invalid")

    private fun Map<String, Any?>.requiredLong(key: String): Long =
        (this[key] as? Number)
            ?.toLong()
            ?: throw InvalidJwtException(message = "JWT is invalid")

    private companion object {
        const val ALGORITHM = "HS256"
        const val TOKEN_TYPE = "JWT"
        const val HMAC_SHA_256 = "HmacSHA256"
        const val JWT_PART_COUNT = 3
        val REGISTERED_CLAIMS = setOf("sub", "jti", "iat", "exp")
        val BASE64_URL_ENCODER: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()
        val BASE64_URL_DECODER: Base64.Decoder = Base64.getUrlDecoder()
    }

}
