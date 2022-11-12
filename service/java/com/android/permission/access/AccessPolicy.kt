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

package com.android.permission.access

import com.android.permission.access.external.PackageState
import com.android.permission.access.permission.UidPermissionPolicy
import com.android.permission.access.util.* // ktlint-disable no-wildcard-imports

class AccessPolicy private constructor(
    private val schemePolicies: IndexedMap<String, IndexedMap<String, SchemePolicy>>
) {
    constructor() : this(
        IndexedMap<String, IndexedMap<String, SchemePolicy>>().apply {
            val uidPermissionPolicy = UidPermissionPolicy()
            getOrPut(uidPermissionPolicy.subjectScheme) { IndexedMap() }
                .put(uidPermissionPolicy.objectScheme, uidPermissionPolicy)
        }
    )

    fun getDecision(subject: AccessUri, `object`: AccessUri, state: AccessState): Int {
        // TODO: Warn when not found?
        val schemePolicy = getSchemePolicy(subject, `object`) ?: return AccessDecisions.DENIED
        return schemePolicy.getDecision(subject, `object`, state)
    }

    fun setDecision(
        subject: AccessUri,
        `object`: AccessUri,
        decision: Int,
        oldState: AccessState,
        newState: AccessState
    ) {
        // TODO: Warn when not found?
        val schemePolicy = getSchemePolicy(subject, `object`) ?: return
        return schemePolicy.setDecision(subject, `object`, decision, oldState, newState)
    }

    private fun getSchemePolicy(subject: AccessUri, `object`: AccessUri): SchemePolicy? =
        schemePolicies[subject.scheme]?.get(`object`.scheme)

    fun onUserAdded(userId: Int, oldState: AccessState, newState: AccessState) {
        newState.systemState.userIds += userId
        newState.userStates[userId] = UserState()
        forEachSchemePolicy { it.onUserAdded(userId, oldState, newState) }
    }

    fun onUserRemoved(userId: Int, oldState: AccessState, newState: AccessState) {
        newState.systemState.userIds -= userId
        newState.userStates -= userId
        forEachSchemePolicy { it.onUserRemoved(userId, oldState, newState) }
    }

    fun onPackageAdded(packageState: PackageState, oldState: AccessState, newState: AccessState) {
        var isAppIdAdded = false
        newState.systemState.apply {
            packageStates[packageState.packageName] = packageState
            appIds.getOrPut(packageState.appId) {
                isAppIdAdded = true
                IndexedListSet()
            }.add(packageState.packageName)
        }
        if (isAppIdAdded) {
            forEachSchemePolicy { it.onAppIdAdded(packageState.appId, oldState, newState) }
        }
        forEachSchemePolicy { it.onPackageAdded(packageState, oldState, newState) }
    }

    fun onPackageRemoved(packageState: PackageState, oldState: AccessState, newState: AccessState) {
        var isAppIdRemoved = false
        newState.systemState.apply {
            packageStates -= packageState.packageName
            appIds.apply appIds@{
                this[packageState.appId]?.apply {
                    this -= packageState.packageName
                    if (isEmpty()) {
                        this@appIds -= packageState.appId
                        isAppIdRemoved = true
                    }
                }
            }
        }
        forEachSchemePolicy { it.onPackageRemoved(packageState, oldState, newState) }
        if (isAppIdRemoved) {
            forEachSchemePolicy { it.onAppIdRemoved(packageState.appId, oldState, newState) }
        }
    }

    private inline fun forEachSchemePolicy(action: (SchemePolicy) -> Unit) {
        schemePolicies.forEachValueIndexed { _, it ->
            it.forEachValueIndexed { _, it ->
                action(it)
            }
        }
    }
}

abstract class SchemePolicy {
    abstract val subjectScheme: String

    abstract val objectScheme: String

    abstract fun getDecision(subject: AccessUri, `object`: AccessUri, state: AccessState): Int

    abstract fun setDecision(
        subject: AccessUri,
        `object`: AccessUri,
        decision: Int,
        oldState: AccessState,
        newState: AccessState
    )

    abstract fun onUserAdded(userId: Int, oldState: AccessState, newState: AccessState)

    abstract fun onUserRemoved(userId: Int, oldState: AccessState, newState: AccessState)

    abstract fun onAppIdAdded(appId: Int, oldState: AccessState, newState: AccessState)

    abstract fun onAppIdRemoved(appId: Int, oldState: AccessState, newState: AccessState)

    abstract fun onPackageAdded(
        packageState: PackageState,
        oldState: AccessState,
        newState: AccessState
    )

    abstract fun onPackageRemoved(
        packageState: PackageState,
        oldState: AccessState,
        newState: AccessState
    )
}
