package com.jobdori.common.time

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toInstantAtSystemDefaultZone(): Instant {
    return atZone(ZoneId.systemDefault()).toInstant()
}
