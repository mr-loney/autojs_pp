package com.pp.autojs.core.boardcast;

import com.pp.autojs.core.eventloop.EventEmitter;
import com.pp.autojs.core.looper.Timer;
import com.pp.autojs.runtime.ScriptBridges;

/**
 * Created by Stardust on 2018/4/1.
 */

public class BroadcastEmitter extends EventEmitter {

    public BroadcastEmitter(ScriptBridges bridges, Timer timer) {
        super(bridges, timer);
        Broadcast.registerListener(this);
    }

    public boolean onBroadcast(String eventName, Object... args) {
        return super.emit(eventName, args);
    }

    public void unregister() {
        Broadcast.unregisterListener(this);
    }

    @Override
    public boolean emit(String eventName, Object... args) {
        Broadcast.send(eventName, args);
        return true;
    }
}
