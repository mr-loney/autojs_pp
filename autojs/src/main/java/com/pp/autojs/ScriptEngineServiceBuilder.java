package com.pp.autojs;

import com.pp.autojs.engine.ScriptEngineManager;
import com.pp.autojs.runtime.ScriptRuntime;
import com.pp.autojs.runtime.api.Console;
import com.pp.util.Supplier;
import com.pp.util.UiHandler;

/**
 * Created by Stardust on 2017/4/2.
 */

public class ScriptEngineServiceBuilder {

    ScriptEngineManager mScriptEngineManager;
    Console mGlobalConsole;
    UiHandler mUiHandler;

    public ScriptEngineServiceBuilder() {

    }

    public ScriptEngineServiceBuilder uiHandler(UiHandler uiHandler) {
        mUiHandler = uiHandler;
        return this;
    }

    public ScriptEngineServiceBuilder engineManger(ScriptEngineManager manager) {
        mScriptEngineManager = manager;
        return this;
    }

    public ScriptEngineServiceBuilder globalConsole(Console console) {
        mGlobalConsole = console;
        return this;
    }

    public ScriptEngineService build() {
        return new ScriptEngineService(this);
    }


}
