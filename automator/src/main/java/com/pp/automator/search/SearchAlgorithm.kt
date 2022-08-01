package com.pp.automator.search

import com.pp.automator.UiObject
import com.pp.automator.filter.Filter

interface SearchAlgorithm {

    fun search(root: UiObject, filter: Filter, limit: Int = Int.MAX_VALUE): ArrayList<UiObject>
}