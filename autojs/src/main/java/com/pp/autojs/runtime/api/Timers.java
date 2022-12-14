package com.pp.autojs.runtime.api;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import com.pp.autojs.core.looper.Timer;
import com.pp.autojs.core.looper.TimerThread;
import com.pp.autojs.runtime.ScriptBridges;
import com.pp.autojs.runtime.ScriptRuntime;
import com.pp.concurrent.VolatileBox;

/**
 * Created by Stardust on 2017/7/21.
 */

public class Timers {

    private static final String LOG_TAG = "Timers";

    private VolatileBox<Long> mMaxCallbackUptimeMillisForAllThreads = new VolatileBox<>(0L);
    private Threads mThreads;
    private Timer mMainTimer;
    private Timer mUiTimer;


    public Timers(ScriptRuntime runtime) {
        mMainTimer = new Timer(runtime, mMaxCallbackUptimeMillisForAllThreads);
        mUiTimer = new Timer(runtime, mMaxCallbackUptimeMillisForAllThreads, Looper.getMainLooper());
        mThreads = runtime.threads;
    }

    public Timer getMainTimer() {
        return mMainTimer;
    }

    VolatileBox<Long> getMaxCallbackUptimeMillisForAllThreads() {
        return mMaxCallbackUptimeMillisForAllThreads;
    }

    public Timer getTimerForCurrentThread() {
        return getTimerForThread(Thread.currentThread());
    }

    public Timer getTimerForThread(Thread thread) {
        if (thread == mThreads.getMainThread()) {
            return mMainTimer;
        }
        Timer timer = TimerThread.getTimerForThread(thread);
        if (timer == null && Looper.myLooper() == Looper.getMainLooper()) {
            return mUiTimer;
        }
        return timer;
    }

    public int setTimeout(Object callback, long delay, Object... args) {
        return getTimerForCurrentThread().setTimeout(callback, delay, args);
    }

    public boolean clearTimeout(int id) {
        return getTimerForCurrentThread().clearTimeout(id);
    }

    public int setInterval(Object listener, long interval, Object... args) {
        return getTimerForCurrentThread().setInterval(listener, interval, args);
    }

    public boolean clearInterval(int id) {
        return getTimerForCurrentThread().clearInterval(id);
    }

    public int setImmediate(Object listener, Object... args) {
        return getTimerForCurrentThread().setImmediate(listener, args);
    }

    public boolean clearImmediate(int id) {
        return getTimerForCurrentThread().clearImmediate(id);
    }

    public boolean hasPendingCallbacks() {
        //????????????????????????????????????????????????????????????????????????mFutureCallbackUptimeMillis??????????????????????????????????????????????????????
        if (mThreads.getMainThread() == Thread.currentThread()) {
            return mMaxCallbackUptimeMillisForAllThreads.get() > SystemClock.uptimeMillis();
        }
        // ???????????????????????????????????????
        return getTimerForCurrentThread().hasPendingCallbacks();
    }

    public void recycle() {
        mMainTimer.removeAllCallbacks();
    }

}
