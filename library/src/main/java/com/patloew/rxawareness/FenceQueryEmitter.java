package com.patloew.rxawareness;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceStateMap;
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
class FenceQueryEmitter extends BaseEmitter<FenceStateMap> {

    private final FenceQueryRequest request;

    FenceQueryEmitter(RxAwareness rxAwareness, FenceQueryRequest request, Long timeout, TimeUnit timeUnit) {
        super(rxAwareness, timeout, timeUnit);
        this.request = request;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<FenceStateMap> observer) {
        setupAwarenessPendingResult(Awareness.FenceApi.queryFences(apiClient, request),
                new SingleResultCallback<FenceStateMap, FenceQueryResult>(observer) {
                    @Override FenceStateMap mapResult(FenceQueryResult result) {
                        return result.getFenceStateMap();
                    }
                });
    }

}
