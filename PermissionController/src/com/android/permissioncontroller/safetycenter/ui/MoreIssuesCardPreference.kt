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

package com.android.permissioncontroller.safetycenter.ui

import android.content.Context
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.permissioncontroller.R
import com.android.permissioncontroller.safetycenter.ui.view.MoreIssuesHeaderView

/** A preference that displays a card linking to a list of more {@link SafetyCenterIssue}. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class MoreIssuesCardPreference(
    context: Context,
    @DrawableRes val preferenceWidgetIconResourceId: Int,
    private var previousMoreIssuesCardData: MoreIssuesCardData?,
    private var newMoreIssuesCardData: MoreIssuesCardData,
    private val onClickListener: () -> Unit
) : Preference(context), ComparablePreference {

    init {
        layoutResource = R.layout.preference_more_issues_card
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val issueHeaderView = holder.itemView as MoreIssuesHeaderView
        issueHeaderView.showExpandableHeader(
            previousMoreIssuesCardData,
            newMoreIssuesCardData,
            context.getString(R.string.safety_center_more_issues_card_title),
            onClickListener
        )
    }

    fun setNewMoreIssuesCardData(moreIssuesCardData: MoreIssuesCardData) {
        previousMoreIssuesCardData = newMoreIssuesCardData
        newMoreIssuesCardData = moreIssuesCardData
        notifyChanged()
    }

    override fun isSameItem(preference: Preference): Boolean {
        return preference is MoreIssuesCardPreference
    }

    override fun hasSameContents(preference: Preference): Boolean {
        return preference is MoreIssuesCardPreference &&
            previousMoreIssuesCardData == preference.previousMoreIssuesCardData &&
            newMoreIssuesCardData == preference.newMoreIssuesCardData &&
            preferenceWidgetIconResourceId == preference.preferenceWidgetIconResourceId
    }

    companion object {
        val TAG: String = MoreIssuesCardPreference::class.java.simpleName
    }
}

internal data class MoreIssuesCardData(
    val severityLevel: Int,
    val hiddenIssueCount: Int,
    val isExpanded: Boolean
)
