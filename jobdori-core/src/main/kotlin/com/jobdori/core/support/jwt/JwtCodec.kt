package com.jobdori.core.support.jwt

interface JwtCodec {

    fun encode(claims: JwtClaims): String

    fun decode(token: String): JwtClaims

}
