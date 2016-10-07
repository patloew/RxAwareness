package com.patloew.rxawareness;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognitionResult;

import java.util.concurrent.TimeUnit;

import rx.Observer;

/* Copyright 2016 Patrick Löwenstein
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */
class SnapshotGetDetectedActivityEmitter extends BaseEmitter<ActivityRecognitionResult> {

    SnapshotGetDetectedActivityEmitter(RxAwareness rxAwareness, Long timeout, TimeUnit timeUnit) {
        super(rxAwareness, timeout, timeUnit);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<ActivityRecognitionResult> observer) {
        //noinspection MissingPermission
        setupAwarenessPendingResult(Awareness.SnapshotApi.getDetectedActivity(apiClient),
                new SingleResultCallback<ActivityRecognitionResult, DetectedActivityResult>(observer) {
                    @Override ActivityRecognitionResult mapResult(DetectedActivityResult result) {
                        return result.getActivityRecognitionResult();
                    }
                });
    }

}
