package com.jobdori.api.application.user.controller

import com.jobdori.api.application.user.service.UserService
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/v1/users/me")
    @Authenticated
    fun getMyUser(
        @UserId userId: Long,
    ) = ApiResponse.ok(userService.getMyUser(userId))

}
