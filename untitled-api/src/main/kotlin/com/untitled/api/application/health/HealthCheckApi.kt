package com.untitled.api.application.health

import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.LivenessState
import org.springframework.boot.availability.ReadinessState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckApi(
    private val applicationAvailability: ApplicationAvailability,
) {

    @GetMapping("/health/liveness")
    fun livenessProbe(): ResponseEntity<Nothing> {
        if (applicationAvailability.livenessState != LivenessState.CORRECT) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }
        return ResponseEntity.ok().build()
    }

    @GetMapping("/health/readiness")
    fun readinessProbe(): ResponseEntity<Nothing> {
        if (applicationAvailability.readinessState != ReadinessState.ACCEPTING_TRAFFIC) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
        }
        return ResponseEntity.ok().build()
    }

}
