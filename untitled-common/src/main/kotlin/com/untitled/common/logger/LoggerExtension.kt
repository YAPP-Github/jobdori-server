package com.untitled.common.logger

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

object LoggerExtension {

    val log: KLogger
        inline get() = KotlinLogging.logger {}

}
