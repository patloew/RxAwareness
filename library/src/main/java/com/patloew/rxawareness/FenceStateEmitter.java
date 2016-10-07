package com.patloew.rxawareness;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

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
class FenceStateEmitter extends BaseEmitter<FenceState> {

    private final String FENCE_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + ".FENCE_RECEIVER_ACTION";

    private final String fenceKey;
    private final AwarenessFence fence;

    private RxAwarenessFenceReceiver receiver;

    FenceStateEmitter(RxAwareness rxAwareness, String fenceKey, AwarenessFence fence, Long timeout, TimeUnit timeUnit) {
        super(rxAwareness, timeout, timeUnit);
        this.fenceKey = fenceKey;
        this.fence = fence;
    }

    @Override
    protected void onGoogleApiClientReady(GoogleApiClient apiClient, final Observer<FenceState> observer) {
        FenceUpdateRequest request = getRequestAndRegisterReceiver(observer);

        setupAwarenessPendingResult(Awareness.FenceApi.updateFences(apiClient, request), new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(!status.isSuccess()) {
                    observer.onError(new StatusException(status));
                }
            }
        });
    }

    FenceUpdateRequest getRequestAndRegisterReceiver(Observer<FenceState> observer) {
        Intent intent = new Intent(FENCE_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        receiver = new RxAwarenessFenceReceiver(observer);
        ctx.registerReceiver(receiver, new IntentFilter(FENCE_RECEIVER_ACTION));

        return new FenceUpdateRequest.Builder()
                .addFence(fenceKey, fence, pendingIntent)
                .build();
    }

    @Override
    protected void onUnsubscribed(GoogleApiClient awarenessClient) {
        if(receiver != null) {
            ctx.unregisterReceiver(receiver);
        }

        FenceUpdateRequest request = new FenceUpdateRequest.Builder()
                .removeFence(fenceKey)
                .build();

        Awareness.FenceApi.updateFences(awarenessClient, request);
    }


    class RxAwarenessFenceReceiver extends BroadcastReceiver {

        private final Observer<FenceState> observer;

        public RxAwarenessFenceReceiver(Observer<FenceState> observer) {
            this.observer = observer;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            observer.onNext(FenceState.extract(intent));
        }

    }
}
