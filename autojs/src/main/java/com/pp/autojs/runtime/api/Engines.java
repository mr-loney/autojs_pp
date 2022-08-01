package com.pp.autojs.runtime.api;

import com.pp.autojs.ScriptEngineService;
import com.pp.autojs.engine.JavaScriptEngine;
import com.pp.autojs.execution.ExecutionConfig;
import com.pp.autojs.execution.ScriptExecution;
import com.pp.autojs.runtime.ScriptRuntime;
import com.pp.autojs.script.AutoFileSource;
import com.pp.autojs.script.JavaScriptFileSource;
import com.pp.autojs.script.StringScriptSource;

/**
 * Created by Stardust on 2017/8/4.
 */

public class Engines {

    private ScriptEngineService mEngineService;
    private JavaScriptEngine mScriptEngine;
    private ScriptRuntime mScriptRuntime;

    public Engines(ScriptEngineService engineService, ScriptRuntime scriptRuntime) {
        mEngineService = engineService;
        mScriptRuntime = scriptRuntime;
    }

    public ScriptExecution execScript(String name, String script, ExecutionConfig config) {
        return mEngineService.execute(new StringScriptSource(name, script), config);
    }

    public ScriptExecution execScriptFile(String path, ExecutionConfig config) {
        return mEngineService.execute(new JavaScriptFileSource(mScriptRuntime.files.path(path)), config);
    }

    public ScriptExecution execAutoFile(String path, ExecutionConfig config) {
        return mEngineService.execute(new AutoFileSource(mScriptRuntime.files.path(path)), config);
    }

    public Object all() {
        return mScriptRuntime.bridges.toArray(mEngineService.getEngines());
    }

    public int stopAll() {
        return mEngineService.stopAll();
    }

    public void stopAllAndToast() {
        mEngineService.stopAllAndToast();
    }


    public void setCurrentEngine(JavaScriptEngine engine) {
        if (mScriptEngine != null)
            throw new IllegalStateException();
        mScriptEngine = engine;
    }

    public JavaScriptEngine myEngine() {
        return mScriptEngine;
    }
}
