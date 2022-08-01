package com.pp.automator.filter

import com.pp.automator.UiObject

/**
 * Created by Stardust on 2017/3/9.
 */

interface KeyGetter {

    fun getKey(nodeInfo: UiObject): String?
}
