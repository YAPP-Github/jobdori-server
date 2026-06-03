package com.untitled.common.error

interface ErrorCode {

    val httpStatusCode: Int
    val code: String
    val name: String
    val description: String

}
