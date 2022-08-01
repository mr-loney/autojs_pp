package com.pp.autojs.autojs;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.pp.autojs.tool.AccessibilityServiceTool;
import com.pp.app.GlobalAppContext;
import com.pp.autojs.core.console.GlobalConsole;
import com.pp.autojs.runtime.ScriptRuntime;
import com.pp.autojs.runtime.accessibility.AccessibilityConfig;
import com.pp.autojs.runtime.api.AppUtils;
import com.pp.autojs.runtime.exception.ScriptException;
import com.pp.autojs.runtime.exception.ScriptInterruptedException;

import com.pp.autojs.BuildConfig;
import com.pp.autojs.Pref;
import com.pp.autojs.R;
import com.pp.autojs.external.fileprovider.AppFileProvider;

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
        IntentFilter intentFilter = new IntentFilter();
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
                //log?
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
            errorMessage = "无障碍服务已启用但并未运行，这可能是安卓的BUG，您可能需要重启手机或重启无障碍服务";
        } else {
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = "使用Root权限启动无障碍服务超时";
                }
            } else {
                errorMessage = "无障碍服务未启动";
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
            errorMessage = "无障碍服务已启用但并未运行，这可能是安卓的BUG，您可能需要重启手机或重启无障碍服务";
        } else {
            if (Pref.shouldEnableAccessibilityServiceByRoot()) {
                if (!AccessibilityServiceTool.enableAccessibilityServiceByRootAndWaitFor(2000)) {
                    errorMessage = "使用Root权限启动无障碍服务超时";
                }
            } else {
                errorMessage = "无障碍服务未启动";
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
        if (BuildConfig.CHANNEL.equals("coolapk")) {
            config.addWhiteList("com.coolapk.market");
        }
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
