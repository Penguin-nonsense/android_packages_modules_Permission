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

import android.util.ArrayMap

typealias IndexedMap<K, V> = ArrayMap<K, V>

inline fun <K, V> IndexedMap<K, V>.allIndexed(predicate: (Int, K, V) -> Boolean): Boolean {
    for (index in 0 until size) {
        if (!predicate(index, keyAt(index), valueAt(index))) {
            return false
        }
    }
    return true
}

inline fun <K, V> IndexedMap<K, V>.anyIndexed(predicate: (Int, K, V) -> Boolean): Boolean {
    for (index in 0 until size) {
        if (predicate(index, keyAt(index), valueAt(index))) {
            return true
        }
    }
    return false
}

inline fun <K, V> IndexedMap<K, V>.copy(copyValue: (V) -> V): IndexedMap<K, V> =
    IndexedMap(this).apply {
        forEachValueIndexed { index, value ->
            setValueAt(index, copyValue(value))
        }
    }

inline fun <K, V, R> IndexedMap<K, V>.firstNotNullOfOrNullIndexed(transform: (Int, K, V) -> R): R? {
    for (index in 0 until size) {
        transform(index, keyAt(index), valueAt(index))?.let { return it }
    }
    return null
}

inline fun <K, V> IndexedMap<K, V>.forEachIndexed(action: (Int, K, V) -> Unit) {
    for (index in 0 until size) {
        action(index, keyAt(index), valueAt(index))
    }
}

inline fun <K, V> IndexedMap<K, V>.forEachKeyIndexed(action: (Int, K) -> Unit) {
    for (index in 0 until size) {
        action(index, keyAt(index))
    }
}

inline fun <K, V> IndexedMap<K, V>.forEachValueIndexed(action: (Int, V) -> Unit) {
    for (index in 0 until size) {
        action(index, valueAt(index))
    }
}

inline fun <K, V> IndexedMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    get(key)?.let { return it }
    return defaultValue().also { put(key, it) }
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <K, V> IndexedMap<K, V>.minusAssign(key: K) {
    remove(key)
}

@Suppress("NOTHING_TO_INLINE")
inline operator fun <K, V> IndexedMap<K, V>.set(key: K, value: V) {
    put(key, value)
}
