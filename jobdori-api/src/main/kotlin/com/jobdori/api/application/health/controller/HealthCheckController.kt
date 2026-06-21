package com.jobdori.api.application.health.controller

import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.common.error.CommonErrorCode
import org.springframework.boot.availability.ApplicationAvailability
import org.springframework.boot.availability.ReadinessState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthCheckController(
    private val applicationAvailability: ApplicationAvailability,
) {

    @GetMapping("/health")
    fun readinessProbe(): ResponseEntity<ApiResponse<Nothing?>> {
        if (applicationAvailability.readinessState != ReadinessState.ACCEPTING_TRAFFIC) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.fail(CommonErrorCode.E503_SERVICE_UNAVAILABLE))
        }
        return ResponseEntity.ok(ApiResponse.OK)
    }

}
