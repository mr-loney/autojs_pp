package com.pp.autojs

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.pp.autojs.model.script.ScriptFile
import com.pp.autojs.model.script.Scripts


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras != null) {
            val script_path = extras.getString("ScriptPath");
            val log_path = extras.getString("LogPath");
            if (script_path != null) {
                Log.e("MainActivity", "begin run script = " + script_path + " ; log path = " + log_path);
                Scripts.run(ScriptFile(script_path));
            }
        }
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