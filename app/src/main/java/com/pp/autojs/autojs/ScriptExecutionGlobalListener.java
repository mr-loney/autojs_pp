package com.pp.autojs.autojs;

import static com.pp.autojs.Pref.ACTION_SCRIPT_EXECUTION_EXCEPTION;
import static com.pp.autojs.Pref.ACTION_SCRIPT_EXECUTION_FINISHED;

import android.util.Log;

import android.content.Intent;
import com.pp.app.GlobalAppContext;
import com.pp.autojs.engine.JavaScriptEngine;
import com.pp.autojs.execution.ScriptExecution;
import com.pp.autojs.execution.ScriptExecutionListener;
import com.pp.autojs.App;
import com.pp.autojs.R;

/**
 * Created by Stardust on 2017/5/3.
 */

public class ScriptExecutionGlobalListener implements ScriptExecutionListener {
    private static final String ENGINE_TAG_START_TIME = "com.pp.autojs.autojs.Goodbye, World";

    @Override
    public void onStart(ScriptExecution execution) {
        execution.getEngine().setTag(ENGINE_TAG_START_TIME, System.currentTimeMillis());
    }

    @Override
    public void onSuccess(ScriptExecution execution, Object result) {
        onFinish(execution);
    }

    private void onFinish(ScriptExecution execution) {
        Long millis = (Long) execution.getEngine().getTag(ENGINE_TAG_START_TIME);
        if (millis == null)
            return;
        double seconds = (System.currentTimeMillis() - millis) / 1000.0;
        AutoJs.getInstance().getScriptEngineService().getGlobalConsole()
                .verbose("\\n------------\\n[%s]运行结束，用时%f秒", execution.getSource().toString(), seconds);

        Intent it = new Intent(ACTION_SCRIPT_EXECUTION_FINISHED);
        it.putExtra("duration", seconds);
        GlobalAppContext.get().sendBroadcast(it);
    }

    @Override
    public void onException(ScriptExecution execution, Throwable e) {
        Intent it = new Intent(ACTION_SCRIPT_EXECUTION_EXCEPTION);
        it.putExtra("exception", e.toString());
        GlobalAppContext.get().sendBroadcast(it);

        onFinish(execution);
    }

}
