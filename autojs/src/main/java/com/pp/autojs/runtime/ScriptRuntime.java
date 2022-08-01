package com.pp.autojs.runtime;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.pp.app.GlobalAppContext;
import com.pp.autojs.R;
import com.pp.autojs.ScriptEngineService;
import com.pp.autojs.annotation.ScriptVariable;
import com.pp.autojs.core.accessibility.AccessibilityBridge;
import com.pp.autojs.core.image.Colors;
import com.pp.autojs.core.permission.Permissions;
import com.pp.autojs.rhino.AndroidClassLoader;
import com.pp.autojs.rhino.TopLevelScope;
import com.pp.autojs.rhino.continuation.Continuation;
import com.pp.autojs.runtime.api.AbstractShell;
import com.pp.autojs.runtime.api.AppUtils;
import com.pp.autojs.runtime.api.Console;
import com.pp.autojs.runtime.api.Device;
import com.pp.autojs.runtime.api.Engines;
import com.pp.autojs.runtime.api.Events;
import com.pp.autojs.runtime.api.Files;
import com.pp.autojs.runtime.api.Floaty;
import com.pp.autojs.core.looper.Loopers;
import com.pp.autojs.runtime.api.Media;
import com.pp.autojs.runtime.api.Plugins;
import com.pp.autojs.runtime.api.Sensors;
import com.pp.autojs.runtime.api.Threads;
import com.pp.autojs.runtime.api.Timers;
import com.pp.autojs.core.accessibility.UiSelector;
import com.pp.autojs.runtime.api.Images;
import com.pp.autojs.core.image.capture.ScreenCaptureRequester;
import com.pp.autojs.runtime.api.Dialogs;
import com.pp.autojs.runtime.exception.ScriptEnvironmentException;
import com.pp.autojs.runtime.exception.ScriptException;
import com.pp.autojs.runtime.exception.ScriptInterruptedException;
import com.pp.autojs.core.accessibility.SimpleActionAutomator;
import com.pp.autojs.runtime.api.UI;
import com.pp.concurrent.VolatileDispose;
import com.pp.lang.ThreadCompat;
import com.pp.pio.UncheckedIOException;
import com.pp.util.ClipboardUtil;
import com.pp.autojs.core.util.ProcessShell;
import com.pp.util.ScreenMetrics;
import com.pp.util.SdkVersionUtil;
import com.pp.util.Supplier;
import com.pp.util.UiHandler;
import com.pp.autojs.core.activity.ActivityInfoProvider;

import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptStackElement;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Stardust on 2017/1/27.
 */

public class ScriptRuntime {

    private static final String TAG = "ScriptRuntime";


    public static class Builder {
        private UiHandler mUiHandler;
        private Console mConsole;
        private AccessibilityBridge mAccessibilityBridge;
        private Supplier<AbstractShell> mShellSupplier;
        private ScreenCaptureRequester mScreenCaptureRequester;
        private AppUtils mAppUtils;
        private ScriptEngineService mEngineService;

        public Builder() {

        }

        public Builder setUiHandler(UiHandler uiHandler) {
            mUiHandler = uiHandler;
            return this;
        }

        public Builder setConsole(Console console) {
            mConsole = console;
            return this;
        }

        public Builder setAccessibilityBridge(AccessibilityBridge accessibilityBridge) {
            mAccessibilityBridge = accessibilityBridge;
            return this;
        }

        public Builder setShellSupplier(Supplier<AbstractShell> shellSupplier) {
            mShellSupplier = shellSupplier;
            return this;
        }

        public Builder setScreenCaptureRequester(ScreenCaptureRequester requester) {
            mScreenCaptureRequester = requester;
            return this;
        }

        public Builder setAppUtils(AppUtils appUtils) {
            mAppUtils = appUtils;
            return this;
        }

        public Builder setEngineService(ScriptEngineService service) {
            mEngineService = service;
            return this;
        }


        public ScriptRuntime build() {
            return new ScriptRuntime(this);
        }

    }


    @ScriptVariable
    public final AppUtils app;

    @ScriptVariable
    public final Console console;

    @ScriptVariable
    public final SimpleActionAutomator automator;

    @ScriptVariable
    public final ActivityInfoProvider info;

    @ScriptVariable
    public final UI ui;

    @ScriptVariable
    public final Dialogs dialogs;

    @ScriptVariable
    public Events events;

    @ScriptVariable
    public final ScriptBridges bridges = new ScriptBridges();

    @ScriptVariable
    public Loopers loopers;

    @ScriptVariable
    public Timers timers;

    @ScriptVariable
    public Device device;

    @ScriptVariable
    public final AccessibilityBridge accessibilityBridge;

    @ScriptVariable
    public final Engines engines;

    @ScriptVariable
    public Threads threads;

    @ScriptVariable
    public final Floaty floaty;

    @ScriptVariable
    public UiHandler uiHandler;

    @ScriptVariable
    public final Colors colors = new Colors();

    @ScriptVariable
    public final Files files;

    @ScriptVariable
    public Sensors sensors;

    @ScriptVariable
    public final Media media;

    @ScriptVariable
    public final Plugins plugins;

    private Images images;

    private static WeakReference<Context> applicationContext;
    private Map<String, Object> mProperties = new ConcurrentHashMap<>();
    private AbstractShell mRootShell;
    private Supplier<AbstractShell> mShellSupplier;
    private ScreenMetrics mScreenMetrics = new ScreenMetrics();
    private Thread mThread;
    private TopLevelScope mTopLevelScope;


    protected ScriptRuntime(Builder builder) {
        uiHandler = builder.mUiHandler;
        Context context = uiHandler.getContext();
        app = builder.mAppUtils;
        console = builder.mConsole;
        accessibilityBridge = builder.mAccessibilityBridge;
        mShellSupplier = builder.mShellSupplier;
        ui = new UI(context, this);
        this.automator = new SimpleActionAutomator(accessibilityBridge, this);
        automator.setScreenMetrics(mScreenMetrics);
        this.info = accessibilityBridge.getInfoProvider();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            images = new Images(context, this, builder.mScreenCaptureRequester);
        }
        engines = new Engines(builder.mEngineService, this);
        dialogs = new Dialogs(this);
        device = new Device(context);
        floaty = new Floaty(uiHandler, ui, this);
        files = new Files(this);
        media = new Media(context, this);
        plugins = new Plugins(context, this);
    }

    public void init() {
        if (loopers != null)
            throw new IllegalStateException("already initialized");
        threads = new Threads(this);
        timers = new Timers(this);
        loopers = new Loopers(this);
        events = new Events(uiHandler.getContext(), accessibilityBridge, this);
        mThread = Thread.currentThread();
        sensors = new Sensors(uiHandler.getContext(), this);
    }

    public TopLevelScope getTopLevelScope() {
        return mTopLevelScope;
    }

    public void setTopLevelScope(TopLevelScope topLevelScope) {
        if (mTopLevelScope != null) {
            throw new IllegalStateException("top level has already exists");
        }
        mTopLevelScope = topLevelScope;
    }

    public static void setApplicationContext(Context context) {
        applicationContext = new WeakReference<>(context);
    }

    public static Context getApplicationContext() {
        if (applicationContext == null || applicationContext.get() == null) {
            throw new ScriptEnvironmentException("No application context");
        }
        return applicationContext.get();
    }

    public UiHandler getUiHandler() {
        return uiHandler;
    }

    public AccessibilityBridge getAccessibilityBridge() {
        return accessibilityBridge;
    }

    public void toast(final String text) {
        uiHandler.toast(text);
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new ScriptInterruptedException();
        }
    }

    public void setClip(final String text) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ClipboardUtil.setClip(uiHandler.getContext(), text);
            return;
        }
        VolatileDispose<Object> dispose = new VolatileDispose<>();
        uiHandler.post(() -> {
            ClipboardUtil.setClip(uiHandler.getContext(), text);
            dispose.setAndNotify(text);
        });
        dispose.blockedGet();
    }

    public String getClip() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return ClipboardUtil.getClipOrEmpty(uiHandler.getContext()).toString();
        }
        final VolatileDispose<String> clip = new VolatileDispose<>();
        uiHandler.post(() -> clip.setAndNotify(ClipboardUtil.getClipOrEmpty(uiHandler.getContext()).toString()));
        return clip.blockedGetOrThrow(ScriptInterruptedException.class);
    }

    public AbstractShell getRootShell() {
        ensureRootShell();
        return mRootShell;
    }

    private void ensureRootShell() {
        if (mRootShell == null) {
            mRootShell = mShellSupplier.get();
            mRootShell.SetScreenMetrics(mScreenMetrics);
            mShellSupplier = null;
        }
    }

    public AbstractShell.Result shell(String cmd, int root) {
        return ProcessShell.execCommand(cmd, root != 0);
    }

    public UiSelector selector() {
        return new UiSelector(accessibilityBridge);
    }

    public boolean isStopped() {
        return Thread.currentThread().isInterrupted();
    }

    public static void requiresApi(int i) {
        if (Build.VERSION.SDK_INT < i) {
            throw new ScriptException(GlobalAppContext.getString(R.string.text_requires_sdk_version_to_run_the_script) + SdkVersionUtil.sdkIntToString(i));
        }
    }

    public void requestPermissions(String[] permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        Context context = uiHandler.getContext();
        permissions = Permissions.getPermissionsNeedToRequest(context, permissions);
        if (permissions.length == 0)
            return;
        Permissions.requestPermissions(context, permissions);
    }

    public void loadJar(String path) {
        path = files.path(path);
        try {
            ((AndroidClassLoader) ContextFactory.getGlobal().getApplicationClassLoader()).loadJar(new File(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void loadDex(String path) {
        path = files.path(path);
        try {
            ((AndroidClassLoader) ContextFactory.getGlobal().getApplicationClassLoader()).loadDex(new File(path));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void exit() {
        mThread.interrupt();
        engines.myEngine().forceStop();
        threads.exit();
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new ScriptInterruptedException();
        }
    }

    public void exit(Throwable e) {
        engines.myEngine().uncaughtException(e);
        exit();
    }

    @Deprecated
    public void stop() {
        exit();
    }


    public void setScreenMetrics(int width, int height) {
        mScreenMetrics.setScreenMetrics(width, height);
    }

    public ScreenMetrics getScreenMetrics() {
        return mScreenMetrics;
    }

    public void ensureAccessibilityServiceEnabled() {
        accessibilityBridge.ensureServiceEnabled();
    }

    public void onExit() {
        Log.d(TAG, "on exit");
        //清除interrupt状态
        ThreadCompat.interrupted();
        //悬浮窗需要第一时间关闭以免出现恶意脚本全屏悬浮窗屏蔽屏幕并且在exit中写死循环的问题
        ignoresException(floaty::closeAll);
        try {
            events.emit("exit");
        } catch (Throwable e) {
            console.error("exception on exit: ", e);
        }
        ignoresException(threads::shutDownAll);
        ignoresException(events::recycle);
        ignoresException(media::recycle);
        ignoresException(loopers::recycle);
        ignoresException(() -> {
            if (mRootShell != null) mRootShell.exit();
            mRootShell = null;
            mShellSupplier = null;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ignoresException(images::releaseScreenCapturer);
        }
        ignoresException(sensors::unregisterAll);
        ignoresException(timers::recycle);
        ignoresException(ui::recycle);
    }

    private void ignoresException(Runnable r) {
        try {
            r.run();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public Object getImages() {
        return images;
    }

    public Object getProperty(String key) {
        return mProperties.get(key);
    }

    public Object putProperty(String key, Object value) {
        return mProperties.put(key, value);
    }

    public Object removeProperty(String key) {
        return mProperties.remove(key);
    }

    public Continuation createContinuation() {
        return Continuation.Companion.create(this, mTopLevelScope);
    }

    public Continuation createContinuation(Scriptable scope) {
        return Continuation.Companion.create(this, scope);
    }


    public static String getStackTrace(Throwable e, boolean printJavaStackTrace) {
        String message = e.getMessage();
        StringBuilder scriptTrace = new StringBuilder(message == null ? "" : message + "\n");
        if (e instanceof RhinoException) {
            RhinoException rhinoException = (RhinoException) e;
            scriptTrace.append(rhinoException.details()).append("\n");
            for (ScriptStackElement element : rhinoException.getScriptStack()) {
                element.renderV8Style(scriptTrace);
                scriptTrace.append("\n");
            }
            if (printJavaStackTrace) {
                scriptTrace.append("- - - - - - - - - - -\n");
            } else {
                return scriptTrace.toString();
            }
        }
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            e.printStackTrace(writer);
            writer.close();
            BufferedReader bufferedReader = new BufferedReader(new StringReader(stringWriter.toString()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                scriptTrace.append("\n").append(line);
            }
            return scriptTrace.toString();
        } catch (IOException e1) {
            e1.printStackTrace();
            return message;
        }
    }

}
