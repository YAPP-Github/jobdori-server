package com.jobdori.core.support.concurrent

import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.ConcurrentHashMap

// 같은 키의 동시 실행을 하나로 합친다. 먼저 등록한 요청만 block을 실행하고 나머지는 그 결과나 예외를 함께 받는다.
// ponytail: 프로세스 안에서만 합친다(prod 인스턴스 1대). 2대 이상이 되면 DB lease 행 + fencing token으로 바꾼다.
@Component
class SingleFlight {

    private val inFlight = ConcurrentHashMap<String, CompletableFuture<Any?>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> execute(key: String, block: () -> T): T {
        val mine = CompletableFuture<Any?>()
        val running = inFlight.putIfAbsent(key, mine)
        if (running != null) {
            return try {
                running.join() as T
            } catch (e: CompletionException) {
                throw e.cause ?: e
            }
        }
        // 완료 후 바로 지워 실패를 남기지 않는다. 다음 요청은 저장된 결과를 읽거나 다시 생성한다.
        try {
            return block().also { mine.complete(it) }
        } catch (e: Throwable) {
            mine.completeExceptionally(e)
            throw e
        } finally {
            inFlight.remove(key, mine)
        }
    }
}
