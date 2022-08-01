package com.pp.autojs.external.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.pp.app.GlobalAppContext;
import com.pp.autojs.execution.ExecutionConfig;

import com.pp.autojs.autojs.AutoJs;
import com.pp.autojs.model.script.ScriptFile;
import com.pp.autojs.timing.IntentTask;
import com.pp.autojs.timing.TimedTaskManager;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BaseBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "BaseBroadcastReceiver";

    @SuppressLint("CheckResult")
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive: intent = " + intent + ", this = " + this);
        try {
            TimedTaskManager.getInstance().getIntentTaskOfAction(intent.getAction())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(intentTask -> runTask(context, intent, intentTask), Throwable::printStackTrace);
        } catch (Exception e) {
            GlobalAppContext.toast(e.getMessage());
        }
    }

    static void runTask(Context context, Intent intent, IntentTask task) {
        Log.d(LOG_TAG, "runTask: action = " + intent.getAction() + ", script = " + task.getScriptPath());
        ScriptFile file = new ScriptFile(task.getScriptPath());
        ExecutionConfig config = new ExecutionConfig();
        config.setArgument("intent", intent.clone());
        config.setWorkingDirectory(file.getParent());
        try {
            AutoJs.getInstance().getScriptEngineService().execute(file.toSource(), config);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}
