/*
 * Copyright (C) 2022 The Android Open Source Project
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

package android.safetycenter.cts.testing

import android.Manifest.permission.READ_DEVICE_CONFIG
import android.Manifest.permission.WRITE_DEVICE_CONFIG
import android.content.Context
import android.content.res.Resources
import android.provider.DeviceConfig
import android.provider.DeviceConfig.NAMESPACE_PRIVACY
import android.provider.DeviceConfig.Properties
import android.safetycenter.cts.testing.ShellPermissions.callWithShellPermissionIdentity
import java.time.Duration

/** A class that facilitates working with Safety Center flags. */
// TODO(b/219553295): Add timeout flags.
object SafetyCenterFlags {

    /** Flag that determines whether SafetyCenter is enabled. */
    private const val PROPERTY_SAFETY_CENTER_ENABLED = "safety_center_is_enabled"

    /**
     * Flag that determines the time for which a Safety Center refresh is allowed to wait for a
     * source to respond to a refresh request before timing out and marking the refresh as finished.
     */
    private const val PROPERTY_SAFETY_CENTER_REFRESH_SOURCE_TIMEOUT =
        "safety_center_refresh_source_timeout_millis"

    /**
     * Default time for which a Safety Center refresh is allowed to wait for a source to respond to
     * a refresh request before timing out and marking the refresh as finished.
     */
    private val REFRESH_SOURCE_TIMEOUT_DEFAULT_DURATION = Duration.ofSeconds(10)

    /** Returns whether the device supports Safety Center. */
    fun Context.deviceSupportsSafetyCenter() =
        resources.getBoolean(
            Resources.getSystem().getIdentifier("config_enableSafetyCenter", "bool", "android"))

    /** A property that allows getting and modifying [PROPERTY_SAFETY_CENTER_ENABLED]. */
    var isEnabled: Boolean
        get() =
            callWithShellPermissionIdentity(
                {
                    DeviceConfig.getBoolean(
                        NAMESPACE_PRIVACY, PROPERTY_SAFETY_CENTER_ENABLED, /* defaultValue */ false)
                },
                READ_DEVICE_CONFIG)
        set(value) {
            callWithShellPermissionIdentity(
                {
                    val valueWasSet =
                        DeviceConfig.setProperty(
                            NAMESPACE_PRIVACY,
                            PROPERTY_SAFETY_CENTER_ENABLED,
                            /* value = */ value.toString(),
                            /* makeDefault = */ false)
                    require(valueWasSet) { "Could not set Safety Center flag value to: $value" }
                },
                WRITE_DEVICE_CONFIG)
        }

    /**
     * A property that allows getting and setting the
     * [PROPERTY_SAFETY_CENTER_REFRESH_SOURCE_TIMEOUT] device config flag.
     */
    var refreshTimeout: Duration
        get() =
            callWithShellPermissionIdentity(
                {
                    Duration.ofMillis(
                        DeviceConfig.getLong(
                            NAMESPACE_PRIVACY,
                            PROPERTY_SAFETY_CENTER_REFRESH_SOURCE_TIMEOUT,
                            REFRESH_SOURCE_TIMEOUT_DEFAULT_DURATION.toMillis()))
                },
                READ_DEVICE_CONFIG)
        set(value) =
            callWithShellPermissionIdentity(
                {
                    val valueWasSet =
                        DeviceConfig.setProperty(
                            NAMESPACE_PRIVACY,
                            PROPERTY_SAFETY_CENTER_REFRESH_SOURCE_TIMEOUT,
                            /* value = */ value.toMillis().toString(),
                            /* makeDefault = */ false)
                    require(valueWasSet) {
                        "Could not set Safety Center refresh timeout to: $value"
                    }
                },
                WRITE_DEVICE_CONFIG)

    /**
     * Returns a snapshot of all the Safety Center flags.
     *
     * This snapshot is only taken once and cached afterwards. This must be called at least once
     * prior to modifying any flag.
     */
    val snapshot: Properties by lazy {
        callWithShellPermissionIdentity(
            { DeviceConfig.getProperties(NAMESPACE_PRIVACY) }, READ_DEVICE_CONFIG)
    }

    /** Resets the Safety Center flags based on the given [snapshot]. */
    fun reset(snapshot: Properties) {
        callWithShellPermissionIdentity(
            { DeviceConfig.setProperties(snapshot) }, WRITE_DEVICE_CONFIG)
    }

    /** Returns the [PROPERTY_SAFETY_CENTER_ENABLED] of the Safety Center flags snapshot. */
    fun Properties.isSafetyCenterEnabled() =
        getBoolean(PROPERTY_SAFETY_CENTER_ENABLED, /* defaultValue */ false)
}
