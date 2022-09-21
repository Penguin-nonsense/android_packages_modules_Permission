/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.permission.access.util

import android.util.SparseBooleanArray

class IntSet private constructor(
    private val array: SparseBooleanArray
) {
    constructor() : this(SparseBooleanArray())

    val size: Int
        get() = array.size()

    operator fun contains(element: Int): Boolean = array[element]

    fun elementAt(index: Int): Int = array.keyAt(index)

    fun indexOf(element: Int): Int = array.indexOfKey(element)

    fun add(element: Int) {
        array.put(element, true)
    }

    fun remove(element: Int) {
        array.delete(element)
    }

    fun clear() {
        array.clear()
    }

    fun removeAt(index: Int) {
        array.removeAt(index)
    }

    fun copy(): IntSet = IntSet(array.clone())
}

inline fun IntSet.allIndexed(predicate: (Int, Int) -> Boolean): Boolean {
    for (index in 0 until size) {
        if (!predicate(index, elementAt(index))) {
            return false
        }
    }
    return true
}

inline fun IntSet.anyIndexed(predicate: (Int, Int) -> Boolean): Boolean {
    for (index in 0 until size) {
        if (predicate(index, elementAt(index))) {
            return true
        }
    }
    return false
}

inline fun IntSet.forEachIndexed(action: (Int, Int) -> Unit) {
    for (index in 0 until size) {
        action(index, elementAt(index))
    }
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun IntSet.minusAssign(element: Int) {
    remove(element)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun IntSet.plusAssign(element: Int) {
    add(element)
}
