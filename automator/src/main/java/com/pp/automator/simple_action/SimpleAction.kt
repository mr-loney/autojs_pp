package com.pp.automator.simple_action

import com.pp.automator.UiObject

/**
 * Created by Stardust on 2017/1/27.
 */

abstract class SimpleAction {

    @Volatile
    var isValid = true
    @Volatile
    var result = false

    abstract fun perform(root: UiObject): Boolean

}
