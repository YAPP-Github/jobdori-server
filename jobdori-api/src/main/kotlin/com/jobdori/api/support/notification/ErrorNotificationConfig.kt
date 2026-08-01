package com.jobdori.api.support.notification

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@EnableAsync
@Configuration
class ErrorNotificationConfig {

    @Bean(ERROR_NOTIFICATION_EXECUTOR)
    fun errorNotificationExecutor(): Executor = ThreadPoolTaskExecutor().apply {
        corePoolSize = 1
        maxPoolSize = 2
        queueCapacity = 50
        setThreadNamePrefix("discord-notification-")
        setRejectedExecutionHandler(ThreadPoolExecutor.DiscardPolicy())
        setWaitForTasksToCompleteOnShutdown(false)
        initialize()
    }

    companion object {
        const val ERROR_NOTIFICATION_EXECUTOR = "errorNotificationExecutor"
    }
}
