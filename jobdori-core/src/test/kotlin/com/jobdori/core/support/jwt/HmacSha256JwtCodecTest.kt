package com.jobdori.core.support.jwt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64

class HmacSha256JwtCodecTest : StringSpec({

    val codec = HmacSha256JwtCodec(
        properties = JwtCodecProperties(
            secret = Base64.getEncoder().encodeToString(ByteArray(32) { it.toByte() }),
        ),
    )

    "JWT를 인코딩하고 claim을 복원한다" {
        // given
        val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
        val claims = JwtClaims(
            subject = "100",
            tokenId = "token-id",
            issuedAt = now.minusSeconds(10),
            expiresAt = now.plusSeconds(600),
            customClaims = mapOf("token_type" to "ACCESS"),
        )

        // when & then
        codec.decode(codec.encode(claims)) shouldBe claims
    }

    "서명이 변조된 JWT를 거부한다" {
        // given
        val claims = JwtClaims(
            subject = "100",
            tokenId = "token-id",
            issuedAt = Instant.now().minusSeconds(10),
            expiresAt = Instant.now().plusSeconds(600),
        )
        val token = codec.encode(claims)
        val signature = token.substringAfterLast(".")
        val tamperedSignature = (if (signature.first() == 'A') "B" else "A") + signature.drop(1)
        val tampered = token.substringBeforeLast(".") + ".$tamperedSignature"

        // when & then
        shouldThrow<InvalidJwtException> {
            codec.decode(tampered)
        }
    }

    "만료된 JWT를 거부한다" {
        // given
        val claims = JwtClaims(
            subject = "100",
            tokenId = "token-id",
            issuedAt = Instant.now().minusSeconds(600),
            expiresAt = Instant.now().minusSeconds(1),
        )

        // when & then
        shouldThrow<ExpiredJwtException> {
            codec.decode(codec.encode(claims))
        }
    }

    "등록 claim을 custom claim으로 덮어쓸 수 없다" {
        // given
        val claims = JwtClaims(
            subject = "100",
            tokenId = "token-id",
            issuedAt = Instant.now(),
            expiresAt = Instant.now().plusSeconds(600),
            customClaims = mapOf("sub" to "200"),
        )

        // when & then
        shouldThrow<IllegalArgumentException> {
            codec.encode(claims)
        }
    }

    "32바이트보다 짧은 비밀키를 거부한다" {
        // when & then
        shouldThrow<IllegalArgumentException> {
            JwtCodecProperties(
                secret = Base64.getEncoder().encodeToString(ByteArray(31)),
            )
        }
    }

})
