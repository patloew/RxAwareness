package com.patloew.rxawareness;

import android.app.PendingIntent;
import android.support.annotation.NonNull;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.Status;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import rx.Completable;
import rx.Emitter;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

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
public class Fence {

    private final RxAwareness rxAwareness;

    Fence(RxAwareness rxAwareness) {
        this.rxAwareness = rxAwareness;
    }


    // Query Fences

    public Observable<FenceState> getAllFences() {
        return getFencesInternal(FenceQueryRequest.all(), null, null);
    }

    public Observable<FenceState> getAllFences(long timeout, @NonNull TimeUnit timeUnit) {
        return getFencesInternal(FenceQueryRequest.all(), timeout, timeUnit);
    }

    public Single<FenceState> getFence(final String fenceKey) {
        return getFencesInternal(FenceQueryRequest.forFences(fenceKey), null, null).toSingle();
    }

    public Single<FenceState> getFence(final String fenceKey, long timeout, @NonNull TimeUnit timeUnit) {
        return getFencesInternal(FenceQueryRequest.forFences(fenceKey), timeout, timeUnit).toSingle();
    }

    public Observable<FenceState> getFences(String... fenceKeys) {
        return getFencesInternal(FenceQueryRequest.forFences(fenceKeys), null, null);
    }

    public Observable<FenceState> getFences(Collection<String> fenceKeys) {
        return getFencesInternal(FenceQueryRequest.forFences(fenceKeys), null, null);
    }

    public Observable<FenceState> getFences(Collection<String> fenceKeys, long timeout, @NonNull TimeUnit timeUnit) {
        return getFencesInternal(FenceQueryRequest.forFences(fenceKeys), timeout, timeUnit);
    }

    private Observable<FenceState> getFencesInternal(FenceQueryRequest request, Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new FenceQueryEmitter(rxAwareness, request, timeout, timeUnit), Emitter.BackpressureMode.LATEST)
                .flatMap(new Func1<FenceStateMap, Observable<FenceState>>() {
                    @Override
                    public Observable<FenceState> call(FenceStateMap fenceStateMap) {
                        return Observable.from(new FenceStateMapIterable(fenceStateMap));
                    }
                });
    }


    // Fence State Listener

    public Observable<FenceState> listenForStateChanges(@NonNull String fenceKey, @NonNull AwarenessFence fence) {
        return listenForStateChangesInternal(fenceKey, fence, null, null);
    }

    public Observable<FenceState> listenForStateChanges(@NonNull String fenceKey, @NonNull AwarenessFence fence, long timeout, @NonNull TimeUnit timeUnit) {
        return listenForStateChangesInternal(fenceKey, fence, timeout, timeUnit);
    }

    private Observable<FenceState> listenForStateChangesInternal(String fenceKey, AwarenessFence fence, Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new FenceStateEmitter(rxAwareness, fenceKey, fence, timeout, timeUnit), Emitter.BackpressureMode.BUFFER);
    }


    // Fence Updates

    private RxFenceUpdateRequestBuilder updateBuilder() {
        return new RxFenceUpdateRequestBuilder();
    }


    public class RxFenceUpdateRequestBuilder extends FenceUpdateRequest.Builder {

        @Override
        public RxFenceUpdateRequestBuilder addFence(String key, AwarenessFence fence, PendingIntent pendingIntent) {
            super.addFence(key, fence, pendingIntent);
            return this;
        }

        @Override
        public RxFenceUpdateRequestBuilder removeFence(PendingIntent pendingIntent) {
            super.removeFence(pendingIntent);
            return this;
        }

        @Override
        public RxFenceUpdateRequestBuilder removeFence(String key) {
            super.removeFence(key);
            return this;
        }

        public Completable toCompletable() {
            return toObservableInternal(null, null).toCompletable();
        }

        public Completable toCompletable(long timeout, @NonNull TimeUnit timeUnit) {
            return toObservableInternal(timeout, timeUnit).toCompletable();
        }

        public Single<Status> toSingle() {
            return toObservableInternal(null, null).toSingle();
        }

        public Single<Status> toSingle(long timeout, @NonNull TimeUnit timeUnit) {
            return toObservableInternal(timeout, timeUnit).toSingle();
        }

        private Observable<Status> toObservableInternal(Long timeout, TimeUnit timeUnit) {
            return Observable.fromEmitter(new FenceUpdateEmitter(rxAwareness, build(), timeout, timeUnit), Emitter.BackpressureMode.LATEST);
        }
    }
}
