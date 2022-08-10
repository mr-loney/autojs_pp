package com.pp.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.pp.app.NotAskAgainDialog
import com.pp.app.autojs.AutoJs
import com.pp.app.model.script.ScriptFile
import com.pp.app.model.script.Scripts
import com.pp.app.tool.AccessibilityServiceTool
import com.pp.app.tool.FileTool


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showAccessibilitySettingPromptIfDisabled()

        Log.d("MainActivity", "================");
        intent?.extras?.also {
            val action = it.get("Action") as String
            Log.d("MainActivity", "================ action = " + action);

            if (action == "run") {
                runScript(it)
            } else if (action == "stop") {
                stopScript(it)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Log.d("MainActivity", "================");

        intent?.extras?.also {
            val action = it.get("Action") as String
            Log.d("MainActivity", "================ action = " + action);

            if (action == "run") {
                runScript(it)
            } else if (action == "stop") {
                stopScript(it)
            }
        }

    }

    private fun stopScript(extras: Bundle) {
        Log.i("MainActivity", "stop all running script");
        AutoJs.getInstance().scriptEngineService.stopAllAndToast()
        finish();
    }

    private fun runScript(extras: Bundle) {
        Log.d("MainActivity", "================ AccessibilityServiceTool.isAccessibilityServiceEnabled(this) = " + (AccessibilityServiceTool.isAccessibilityServiceEnabled(this)));
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(this)) {
            val script_path = extras.get("ScriptPath") as Uri?;
            val log_path = extras.get("LogPath") as Uri?
            Log.d("MainActivity", "================ script_path = " + script_path);
            if (script_path != null) {
                FileTool.copyScript(this, script_path).also { output ->
                    Log.i("MainActivity", "begin run script = " + output + " ; log path = " + log_path);
                    Scripts.run(ScriptFile(output));
                };
            }
        }
    }

    private fun showAccessibilitySettingPromptIfDisabled() {
        if (AccessibilityServiceTool.isAccessibilityServiceEnabled(this)) {
            Log.i("MainActivity", "AccessibilityService 无权限");
            return
        }
        NotAskAgainDialog.Builder(this, "MainActivity.accessibility")
            .title(R.string.text_need_to_enable_accessibility_service)
            .content(R.string.explain_accessibility_permission)
            .positiveText(R.string.text_go_to_setting)
            .negativeText(R.string.text_cancel)
            .onPositive({ dialog, which -> AccessibilityServiceTool.enableAccessibilityService() }
            ).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}