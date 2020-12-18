package io.forestframework

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.forestframework.utils.ReflectionUtils
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class KotlinSuspendFunctionBridge {
    companion object {
        /**
         * Invoke a Kotlin suspend function reflectively. Note that the passed args size equals the suspend function's
         * real bytecode parameter list length, i.e. the last elements of args is {@code null}, which will be assigned with
         * {@link Continuation} instance.
         */
        @Suppress("UNCHECKED_CAST")
        @SuppressFBWarnings("SE_BAD_FIELD")
        fun <T> invoke(vertx: Vertx, method: Method, instance: Any, vararg args: Any): CompletableFuture<T> =
            GlobalScope.future(vertx.dispatcher()) {
                suspendCoroutineUninterceptedOrReturn<T> {
                    (args as Array<Any>)[args.size - 1] = it
                    ReflectionUtils.invoke(method, instance, args)
                }
            }
    }
}
