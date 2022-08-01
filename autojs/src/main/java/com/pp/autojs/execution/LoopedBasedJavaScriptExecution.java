package com.pp.autojs.execution;

import com.pp.autojs.engine.LoopBasedJavaScriptEngine;
import com.pp.autojs.engine.ScriptEngine;
import com.pp.autojs.engine.ScriptEngineManager;
import com.pp.autojs.core.looper.Loopers;
import com.pp.autojs.script.JavaScriptSource;

/**
 * Created by Stardust on 2017/10/27.
 */

public class LoopedBasedJavaScriptExecution extends RunnableScriptExecution {

    public LoopedBasedJavaScriptExecution(ScriptEngineManager manager, ScriptExecutionTask task) {
        super(manager, task);
    }


    protected Object doExecution(final ScriptEngine engine) {
        engine.setTag(ScriptEngine.TAG_SOURCE, getSource());
        getListener().onStart(this);
        long delay = getConfig().getDelay();
        sleep(delay);
        final LoopBasedJavaScriptEngine javaScriptEngine = (LoopBasedJavaScriptEngine) engine;
        final long interval = getConfig().getInterval();
        javaScriptEngine.getRuntime().loopers.setMainLooperQuitHandler(new Loopers.LooperQuitHandler() {
            long times = getConfig().getLoopTimes() == 0 ? Integer.MAX_VALUE : getConfig().getLoopTimes();

            @Override
            public boolean shouldQuit() {
                times--;
                if (times > 0) {
                    sleep(interval);
                    javaScriptEngine.execute(getSource());
                    return false;
                }
                javaScriptEngine.getRuntime().loopers.setMainLooperQuitHandler(null);
                return true;
            }
        });
        javaScriptEngine.execute(getSource());
        return null;
    }

    @Override
    public JavaScriptSource getSource() {
        return (JavaScriptSource) super.getSource();
    }

}
