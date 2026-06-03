package com.untitled.api.application.health

import com.untitled.api.support.rest.ApiResponse
import com.untitled.common.error.CommonErrorCode
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
    fun livenessProbe(): ResponseEntity<ApiResponse<Nothing?>> {
        if (applicationAvailability.livenessState != LivenessState.CORRECT) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.Companion.fail(CommonErrorCode.E503_SERVICE_UNAVAILABLE))
        }
        return ResponseEntity.ok(ApiResponse.OK)
    }

    @GetMapping("/health/readiness")
    fun readinessProbe(): ResponseEntity<ApiResponse<Nothing?>> {
        if (applicationAvailability.readinessState != ReadinessState.ACCEPTING_TRAFFIC) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.Companion.fail(CommonErrorCode.E503_SERVICE_UNAVAILABLE))
        }
        return ResponseEntity.ok(ApiResponse.OK)
    }

}
