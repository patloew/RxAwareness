package com.patloew.rxawareness;

import android.support.annotation.NonNull;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

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
abstract class SingleResultCallback<V, R extends Result> implements ResultCallback<R> {

    private final Observer<V> observer;

    SingleResultCallback(Observer<V> observer) {
        this.observer = observer;
    }

    abstract V mapResult(R result);

    @Override
    public void onResult(@NonNull R result) {
        if (result.getStatus().isSuccess()) {
            observer.onNext(mapResult(result));
            observer.onCompleted();
        } else {
            observer.onError(new StatusException(result.getStatus()));
        }
    }
}
