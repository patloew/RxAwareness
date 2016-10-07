package com.patloew.rxawareness;

import android.location.Location;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
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
class SnapshotGetLocationEmitter extends BaseEmitter<Location> {

    SnapshotGetLocationEmitter(RxAwareness rxAwareness, Long timeout, TimeUnit timeUnit) {
        super(rxAwareness, timeout, timeUnit);
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<Location> observer) {
        //noinspection MissingPermission
        setupAwarenessPendingResult(Awareness.SnapshotApi.getLocation(apiClient),
                new SingleResultCallback<Location, LocationResult>(observer) {
                    @Override Location mapResult(LocationResult result) {
                        return result.getLocation();
                    }
                });
    }

}
