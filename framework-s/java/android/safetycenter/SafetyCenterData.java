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

package android.safetycenter;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import com.android.modules.utils.build.SdkLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A representation of the safety state of the device.
 *
 * @hide
 */
@SystemApi
@RequiresApi(TIRAMISU)
public final class SafetyCenterData implements Parcelable {

    @NonNull
    public static final Creator<SafetyCenterData> CREATOR =
            new Creator<SafetyCenterData>() {
                @Override
                public SafetyCenterData createFromParcel(Parcel in) {
                    SafetyCenterStatus status = in.readTypedObject(SafetyCenterStatus.CREATOR);
                    List<SafetyCenterIssue> issues =
                            in.createTypedArrayList(SafetyCenterIssue.CREATOR);
                    List<SafetyCenterEntryOrGroup> entryOrGroups =
                            in.createTypedArrayList(SafetyCenterEntryOrGroup.CREATOR);
                    List<SafetyCenterStaticEntryGroup> staticEntryGroups =
                            in.createTypedArrayList(SafetyCenterStaticEntryGroup.CREATOR);

                    if (SdkLevel.isAtLeastU()) {
                        List<SafetyCenterIssue> dismissedIssues =
                                in.createTypedArrayList(SafetyCenterIssue.CREATOR);
                        return new SafetyCenterData(
                                status, issues, entryOrGroups, staticEntryGroups, dismissedIssues);
                    } else {
                        return new SafetyCenterData(
                                status, issues, entryOrGroups, staticEntryGroups);
                    }
                }

                @Override
                public SafetyCenterData[] newArray(int size) {
                    return new SafetyCenterData[size];
                }
            };

    @NonNull private final SafetyCenterStatus mStatus;
    @NonNull private final List<SafetyCenterIssue> mIssues;
    @NonNull private final List<SafetyCenterEntryOrGroup> mEntriesOrGroups;
    @NonNull private final List<SafetyCenterStaticEntryGroup> mStaticEntryGroups;
    @NonNull private final List<SafetyCenterIssue> mDismissedIssues;

    /** Creates a {@link SafetyCenterData}. */
    public SafetyCenterData(
            @NonNull SafetyCenterStatus status,
            @NonNull List<SafetyCenterIssue> issues,
            @NonNull List<SafetyCenterEntryOrGroup> entriesOrGroups,
            @NonNull List<SafetyCenterStaticEntryGroup> staticEntryGroups) {
        mStatus = requireNonNull(status);
        mIssues = unmodifiableList(new ArrayList<>(requireNonNull(issues)));
        mEntriesOrGroups = unmodifiableList(new ArrayList<>(requireNonNull(entriesOrGroups)));
        mStaticEntryGroups = unmodifiableList(new ArrayList<>(requireNonNull(staticEntryGroups)));
        mDismissedIssues = unmodifiableList(new ArrayList<>());
    }

    /** Creates a {@link SafetyCenterData}. */
    @RequiresApi(UPSIDE_DOWN_CAKE)
    public SafetyCenterData(
            @NonNull SafetyCenterStatus status,
            @NonNull List<SafetyCenterIssue> issues,
            @NonNull List<SafetyCenterEntryOrGroup> entriesOrGroups,
            @NonNull List<SafetyCenterStaticEntryGroup> staticEntryGroups,
            @NonNull List<SafetyCenterIssue> dismissedIssues) {
        if (!SdkLevel.isAtLeastU()) {
            throw new UnsupportedOperationException();
        }
        mStatus = requireNonNull(status);
        mIssues = unmodifiableList(new ArrayList<>(requireNonNull(issues)));
        mEntriesOrGroups = unmodifiableList(new ArrayList<>(requireNonNull(entriesOrGroups)));
        mStaticEntryGroups = unmodifiableList(new ArrayList<>(requireNonNull(staticEntryGroups)));
        mDismissedIssues = unmodifiableList(new ArrayList<>(requireNonNull(dismissedIssues)));
    }

    /** Returns the overall {@link SafetyCenterStatus} of the Safety Center. */
    @NonNull
    public SafetyCenterStatus getStatus() {
        return mStatus;
    }

    /** Returns the list of active {@link SafetyCenterIssue} objects in the Safety Center. */
    @NonNull
    public List<SafetyCenterIssue> getIssues() {
        return mIssues;
    }

    /**
     * Returns the structured list of {@link SafetyCenterEntry} and {@link SafetyCenterEntryGroup}
     * objects, wrapped in {@link SafetyCenterEntryOrGroup}.
     */
    @NonNull
    public List<SafetyCenterEntryOrGroup> getEntriesOrGroups() {
        return mEntriesOrGroups;
    }

    /** Returns the list of {@link SafetyCenterStaticEntryGroup} objects in the Safety Center. */
    @NonNull
    public List<SafetyCenterStaticEntryGroup> getStaticEntryGroups() {
        return mStaticEntryGroups;
    }

    /** Returns the list of dismissed {@link SafetyCenterIssue} objects in the Safety Center. */
    @NonNull
    @RequiresApi(UPSIDE_DOWN_CAKE)
    public List<SafetyCenterIssue> getDismissedIssues() {
        if (!SdkLevel.isAtLeastU()) {
            throw new UnsupportedOperationException();
        }
        return mDismissedIssues;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SafetyCenterData)) return false;
        SafetyCenterData that = (SafetyCenterData) o;
        return Objects.equals(mStatus, that.mStatus)
                && Objects.equals(mIssues, that.mIssues)
                && Objects.equals(mEntriesOrGroups, that.mEntriesOrGroups)
                && Objects.equals(mStaticEntryGroups, that.mStaticEntryGroups)
                && Objects.equals(mDismissedIssues, that.mDismissedIssues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mStatus, mIssues, mEntriesOrGroups, mStaticEntryGroups, mDismissedIssues);
    }

    @Override
    public String toString() {
        return "SafetyCenterData{"
                + "mStatus="
                + mStatus
                + ", mIssues="
                + mIssues
                + ", mEntriesOrGroups="
                + mEntriesOrGroups
                + ", mStaticEntryGroups="
                + mStaticEntryGroups
                + ", mDismissedIssues="
                + mDismissedIssues
                + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mStatus, flags);
        dest.writeTypedList(mIssues);
        dest.writeTypedList(mEntriesOrGroups);
        dest.writeTypedList(mStaticEntryGroups);
        if (SdkLevel.isAtLeastU()) {
            dest.writeTypedList(mDismissedIssues);
        }
    }
}
