package com.patloew.rxawareness;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.common.api.GoogleApiClient;

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
class SnapshotGetHeadphoneStateEmitter extends BaseEmitter<HeadphoneState> {

    SnapshotGetHeadphoneStateEmitter(RxAwareness rxAwareness, Long timeout, TimeUnit timeUnit) {
        super(rxAwareness, timeout, timeUnit);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<HeadphoneState> observer) {
        setupAwarenessPendingResult(Awareness.SnapshotApi.getHeadphoneState(apiClient),
                new SingleResultCallback<HeadphoneState, HeadphoneStateResult>(observer) {
                    @Override HeadphoneState mapResult(HeadphoneStateResult result) {
                        return result.getHeadphoneState();
                    }
                });
    }

}
