package com.pp.app.autojs;

import static com.pp.app.Pref.ACTION_SCRIPT_EXECUTION_FINISHED;
import static com.pp.app.Pref.ACTION_SCRIPT_LOG;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.pp.app.tool.AccessibilityServiceTool;
import com.pp.app.GlobalAppContext;
import com.pp.autojs.core.console.GlobalConsole;
import com.pp.autojs.runtime.ScriptRuntime;
import com.pp.autojs.runtime.accessibility.AccessibilityConfig;
import com.pp.autojs.runtime.api.AppUtils;
import com.pp.autojs.runtime.exception.ScriptException;
import com.pp.autojs.runtime.exception.ScriptInterruptedException;

import com.pp.app.Pref;
import com.pp.app.external.fileprovider.AppFileProvider;

import com.pp.view.accessibility.AccessibilityService;
import com.pp.view.accessibility.LayoutInspector;
import com.pp.view.accessibility.NodeInfo;


/**
 * Created by Stardust on 2017/4/2.
 */

public class AutoJs extends com.pp.autojs.AutoJs {

    private static AutoJs instance;

    public static AutoJs getInstance() {
        return instance;
    }


    public synchronized static void initInstance(Application application) {
        if (instance != null) {
            return;
        }
        instance = new AutoJs(application);
    }

    private AutoJs(final Application application) {
        super(application);
        getScriptEngineService().registerGlobalScriptExecutionListener(new ScriptExecutionGlobalListener());
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(LayoutBoundsFloatyWindow.class.getName());
//        intentFilter.addAction(LayoutHierarchyFloatyWindow.class.getName());
//        LocalBroadcastManager.getInstance(application).registerReceiver(mLayoutInspectBroadcastReceiver, intentFilter);
    }

    @Override
    protected AppUtils createAppUtils(Context context) {
        return new AppUtils(context, AppFileProvider.AUTHORITY);
    }

    @Override
    protected GlobalConsole createGlobalConsole() {
        return new GlobalConsole(getUiHandler()) {
            @Override
            public String println(int level, CharSequence charSequence) {
                String log = super.println(level, charSequence);
//                DevPluginService.getInstance().log(log);
                Intent it = new Intent(ACTION_SCRIPT_LOG);
                it.putExtra("level", log);
                it.putExtra("data", log);
                GlobalAppContext.get().sendBroadcast(it);
                return log;
            }
        };
    }

    public void ensureAccessibilityServiceEnabled() {
        if (AccessibilityService.Companion.getInstance() != null) {
            return;
        }
        String errorMessage = null;
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(GlobalAppContext.get())) {
            errorMessage = "???????????????????????????????????????????????????????????????BUG??????????????????????????????????????????????????????";
        } else {
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = "??????Root?????????????????????????????????";
                }
            } else {
                errorMessage = "????????????????????????";
            }
        }
        if (errorMessage != null) {
            AccessibilityServiceTool.goToAccessibilitySetting();
            throw new ScriptException(errorMessage);
        }
    }

    @Override
    public void waitForAccessibilityServiceEnabled() {
        if (AccessibilityService.Companion.getInstance() != null) {
            return;
        }
        String errorMessage = null;
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(GlobalAppContext.get())) {
            errorMessage = "???????????????????????????????????????????????????????????????BUG??????????????????????????????????????????????????????";
        } else {
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = "??????Root?????????????????????????????????";
                }
            } else {
                errorMessage = "????????????????????????";
            }
        }
        if (errorMessage != null) {
            AccessibilityServiceTool.goToAccessibilitySetting();
            if (!AccessibilityService.Companion.waitForEnabled(-1)) {
                throw new ScriptInterruptedException();
            }
        }
    }

    @Override
    protected AccessibilityConfig createAccessibilityConfig() {
        AccessibilityConfig config = super.createAccessibilityConfig();
        return config;
    }

    @Override
    protected ScriptRuntime createRuntime() {
        ScriptRuntime runtime = super.createRuntime();
//        runtime.putProperty("class.settings", SettingsActivity_.class);
//        runtime.putProperty("class.console", LogActivity_.class);
//        runtime.putProperty("broadcast.inspect_layout_bounds", LayoutBoundsFloatyWindow.class.getName());
//        runtime.putProperty("broadcast.inspect_layout_hierarchy", LayoutHierarchyFloatyWindow.class.getName());
        return runtime;
    }

}
