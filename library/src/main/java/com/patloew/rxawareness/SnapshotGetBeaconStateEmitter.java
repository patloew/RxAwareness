package com.patloew.rxawareness;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.BeaconStateResult;
import com.google.android.gms.awareness.state.BeaconState;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Collection;
import java.util.List;
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
class SnapshotGetBeaconStateEmitter extends BaseEmitter<List<BeaconState.BeaconInfo>> {

    private final Collection<BeaconState.TypeFilter> beaconTypes;

    SnapshotGetBeaconStateEmitter(RxAwareness rxAwareness, Collection<BeaconState.TypeFilter> beaconTypes, Long timeout, TimeUnit timeUnit) {
        super(rxAwareness, timeout, timeUnit);
        this.beaconTypes = beaconTypes;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<List<BeaconState.BeaconInfo>> observer) {
        //noinspection MissingPermission
        setupAwarenessPendingResult(Awareness.SnapshotApi.getBeaconState(apiClient, beaconTypes),
                new SingleResultCallback<List<BeaconState.BeaconInfo>, BeaconStateResult>(observer) {
                    @Override List<BeaconState.BeaconInfo> mapResult(BeaconStateResult result) {
                        return result.getBeaconState().getBeaconInfo();
                    }
                });
    }

}
