package com.pp.autojs.model.script

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import com.pp.app.GlobalAppContext
import com.pp.autojs.execution.ExecutionConfig
import com.pp.autojs.execution.ScriptExecution
import com.pp.autojs.execution.SimpleScriptExecutionListener
import com.pp.autojs.runtime.exception.ScriptInterruptedException
import com.pp.autojs.script.ScriptSource
import com.pp.util.IntentUtil
import com.pp.autojs.Pref
import com.pp.autojs.Pref.ACTION_SCRIPT_EXECUTION_FINISHED
import com.pp.autojs.Pref.ACTION_SCRIPT_EXECUTION_START
import com.pp.autojs.autojs.AutoJs
import com.pp.autojs.external.fileprovider.AppFileProvider
import org.mozilla.javascript.RhinoException
import java.io.File
import java.io.FileFilter
import kotlin.math.log

/**
 * Created by Stardust on 2017/5/3.
 */

object Scripts {

    const val EXTRA_EXCEPTION_MESSAGE = "message"
    const val EXTRA_EXCEPTION_LINE_NUMBER = "lineNumber"
    const val EXTRA_EXCEPTION_COLUMN_NUMBER = "columnNumber"

    val FILE_FILTER = FileFilter { file ->
        file.isDirectory || file.name.endsWith(".js")
                || file.name.endsWith(".auto")
    }

    private val BROADCAST_SENDER_SCRIPT_EXECUTION_LISTENER = object : SimpleScriptExecutionListener() {

        override fun onSuccess(execution: ScriptExecution, result: Any?) {
            GlobalAppContext.get().sendBroadcast(Intent(ACTION_SCRIPT_EXECUTION_FINISHED))
        }

        override fun onException(execution: ScriptExecution, e: Throwable) {
            val rhinoException = getRhinoException(e)
            var line = -1
            var col = 0
            if (rhinoException != null) {
                line = rhinoException.lineNumber()
                col = rhinoException.columnNumber()
            }
            if (ScriptInterruptedException.causedByInterrupted(e)) {
                GlobalAppContext.get().sendBroadcast(Intent(ACTION_SCRIPT_EXECUTION_FINISHED)
                        .putExtra(EXTRA_EXCEPTION_LINE_NUMBER, line)
                        .putExtra(EXTRA_EXCEPTION_COLUMN_NUMBER, col))
            } else {
                GlobalAppContext.get().sendBroadcast(Intent(ACTION_SCRIPT_EXECUTION_FINISHED)
                        .putExtra(EXTRA_EXCEPTION_MESSAGE, e.message)
                        .putExtra(EXTRA_EXCEPTION_LINE_NUMBER, line)
                        .putExtra(EXTRA_EXCEPTION_COLUMN_NUMBER, col))
            }
        }

    }


    fun openByOtherApps(uri: Uri) {
        IntentUtil.viewFile(GlobalAppContext.get(), uri, "text/plain", AppFileProvider.AUTHORITY)
    }

    fun openByOtherApps(file: File) {
        openByOtherApps(Uri.fromFile(file))
    }

    fun edit(context: Context, file: ScriptFile) {
    }

    fun edit(context: Context, path: String) {
        edit(context, ScriptFile(path))
    }

    fun run(file: ScriptFile): ScriptExecution? {
        return try {
            GlobalAppContext.get().sendBroadcast(Intent(ACTION_SCRIPT_EXECUTION_START))
            AutoJs.getInstance().scriptEngineService.execute(file.toSource(),
                    ExecutionConfig(workingDirectory = file.parent))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(GlobalAppContext.get(), e.message, Toast.LENGTH_LONG).show()
            null
        }

    }


    fun run(source: ScriptSource): ScriptExecution? {
        return try {
            GlobalAppContext.get().sendBroadcast(Intent(ACTION_SCRIPT_EXECUTION_START))
            AutoJs.getInstance().scriptEngineService.execute(source, ExecutionConfig(workingDirectory = Pref.getScriptDirPath()))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(GlobalAppContext.get(), e.message, Toast.LENGTH_LONG).show()
            null
        }

    }

    fun runWithBroadcastSender(file: File): ScriptExecution {
        return AutoJs.getInstance().scriptEngineService.execute(ScriptFile(file).toSource(), BROADCAST_SENDER_SCRIPT_EXECUTION_LISTENER,
                ExecutionConfig(workingDirectory = file.parent))
    }


    fun runRepeatedly(scriptFile: ScriptFile, loopTimes: Int, delay: Long, interval: Long): ScriptExecution {
        val source = scriptFile.toSource()
        val directoryPath = scriptFile.parent
        return AutoJs.getInstance().scriptEngineService.execute(source, ExecutionConfig(workingDirectory = directoryPath,
                delay = delay, loopTimes = loopTimes, interval = interval))
    }

    @Nullable
    fun getRhinoException(throwable: Throwable?): RhinoException? {
        var e = throwable
        while (e != null) {
            if (e is RhinoException) {
                return e
            }
            e = e.cause
        }
        return null
    }
}
