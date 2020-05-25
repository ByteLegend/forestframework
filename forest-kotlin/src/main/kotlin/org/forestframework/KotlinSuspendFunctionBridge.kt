package org.forestframework

import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class KotlinSuspendFunctionBridge {
    companion object {
        fun <T> invoke(vertx: Vertx, method: Method, instance: Any, vararg args: Any): CompletableFuture<T> =
            GlobalScope.future(vertx.dispatcher()) {
                suspendCoroutineUninterceptedOrReturn<T> {
                    method.invoke(instance, *args, it)
                }
            }
    }
}