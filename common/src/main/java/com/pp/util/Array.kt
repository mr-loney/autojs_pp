package com.pp.util

import java.util.*

inline fun <reified T> sortedArrayOf(vararg elements: T): Array<T> {
    val a = arrayOf(*elements)
    Arrays.sort(a)
    return a
}