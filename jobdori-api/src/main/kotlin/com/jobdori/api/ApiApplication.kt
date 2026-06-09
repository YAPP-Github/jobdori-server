package com.jobdori.api

import com.jobdori.common.PackageConst.BASE_PACKAGE
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan(basePackages = [BASE_PACKAGE])
@SpringBootApplication(scanBasePackages = [BASE_PACKAGE])
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
