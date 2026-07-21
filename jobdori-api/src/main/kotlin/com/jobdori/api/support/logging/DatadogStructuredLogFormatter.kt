package com.jobdori.api.support.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import org.springframework.boot.logging.structured.StructuredLogFormatter
import org.springframework.core.env.Environment
import tools.jackson.databind.ObjectMapper
import java.time.Instant

/**
 * 필드 이름이 Datadog 예약 속성과 정확히 일치해야 한다. status/service가 없거나
 * error.kind/error.stack가 다른 이름(ECS의 error.type/error.stack_trace 등)이면
 * Error Tracking이 로그를 집계하지 않는다.
 */
class DatadogStructuredLogFormatter(environment: Environment) : StructuredLogFormatter<ILoggingEvent> {

    private val serviceName = environment.getProperty("spring.application.name") ?: "unknown"

    override fun format(event: ILoggingEvent): String {
        val fields = linkedMapOf<String, Any?>()

        // MDC(dd.trace_id 등)와 payload를 먼저 넣어 예약 속성이 덮어쓰이지 않게 한다
        event.mdcPropertyMap.forEach { (key, value) -> fields[key] = value }
        event.keyValuePairs?.forEach { pair -> fields[pair.key] = pair.value }

        fields["timestamp"] = Instant.ofEpochMilli(event.timeStamp).toString()
        fields["status"] = event.level.toString().lowercase()
        fields["service"] = serviceName
        fields["logger"] = event.loggerName
        fields["thread"] = event.threadName
        fields["message"] = event.formattedMessage

        event.throwableProxy?.let { proxy ->
            fields["error"] = mapOf(
                "kind" to proxy.className,
                "message" to proxy.message,
                "stack" to ThrowableProxyUtil.asString(proxy),
            )
        }

        return MAPPER.writeValueAsString(fields) + "\n"
    }

    private companion object {
        private val MAPPER = ObjectMapper()
    }
}
