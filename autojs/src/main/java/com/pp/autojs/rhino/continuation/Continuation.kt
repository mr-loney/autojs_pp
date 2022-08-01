package com.pp.autojs.rhino.continuation

import com.pp.autojs.core.looper.Timer
import com.pp.autojs.core.looper.TimerThread
import com.pp.autojs.rhino.AutoJsContext
import com.pp.autojs.runtime.ScriptRuntime

import org.mozilla.javascript.Context
import org.mozilla.javascript.ContinuationPending
import org.mozilla.javascript.Scriptable

class Continuation(val context: AutoJsContext, val scope: Scriptable, private val mTimer: Timer) {
    var pending: ContinuationPending? = null
        private set
    private val mThread: Thread = Thread.currentThread()

    class Result(val result: Any?, val error: Any?) {
        companion object {

            fun success(result: Any?): Result {
                return Result(result, null)
            }

            fun failure(error: Any?): Result {
                return Result(null, error)
            }
        }
    }

    fun suspend() {
        if (pending != null) {
            throw IllegalStateException("call suspend twice!")
        }
        context.captureContinuation().let {
            pending = it
            throw it
        }
    }

    fun resumeWith(result: Result) {
        val continuation = pending?.continuation
                ?: throw IllegalStateException("call resume() without suspend()!")
        if (mThread == Thread.currentThread()) {
            context.resumeContinuation(continuation, scope, result)
        } else {
            mTimer.postDelayed({
                context.resumeContinuation(continuation, scope, result)
            }, 0)
        }
    }

    companion object {

        fun create(runtime: ScriptRuntime, scope: Scriptable): Continuation {
            val context = Context.getCurrentContext() as AutoJsContext
            return Continuation(context, scope, runtime.timers.timerForCurrentThread)
        }
    }
}
