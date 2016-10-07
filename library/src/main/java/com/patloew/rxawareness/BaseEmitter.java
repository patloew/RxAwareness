package com.patloew.rxawareness;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;

import java.util.concurrent.TimeUnit;

import rx.Emitter;
import rx.Observer;
import rx.functions.Action1;
import rx.functions.Cancellable;

/* Copyright (C) 2015 Michał Charmas (http://blog.charmas.pl)
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
 *
 * ---------------
 *
 * FILE MODIFIED by Patrick Löwenstein, 2016
 *
 */
abstract class BaseEmitter<T> implements Action1<Emitter<T>> {

    protected final Context ctx;
    private final Api<? extends Api.ApiOptions.NotRequiredOptions>[] services;
    private final Scope[] scopes;
    private final Long timeoutTime;
    private final TimeUnit timeoutUnit;

    protected BaseEmitter(@NonNull RxAwareness rxAwareness, Long timeout, TimeUnit timeUnit) {
        this.ctx = rxAwareness.ctx;
        this.services = new Api[] { Awareness.API };
        this.scopes = null;

        if(timeout != null && timeUnit != null) {
            this.timeoutTime = timeout;
            this.timeoutUnit = timeUnit;
        } else {
            this.timeoutTime = rxAwareness.timeoutTime;
            this.timeoutUnit = rxAwareness.timeoutUnit;
        }
    }

    protected BaseEmitter(@NonNull Context ctx, @NonNull Api<? extends Api.ApiOptions.NotRequiredOptions>[] services, Scope[] scopes) {
        this.ctx = ctx;
        this.services = services;
        this.scopes = scopes;
        timeoutTime = null;
        timeoutUnit = null;
    }

    @Override
    public final void call(Emitter<T> emitter) {
        final GoogleApiClient apiClient = createApiClient(new ApiClientConnectionCallbacks(emitter));

        try {
            apiClient.connect();
        } catch (Throwable ex) {
            emitter.onError(ex);
        }

        emitter.setCancellation(new Cancellable() {
            @Override
            public void cancel() throws Exception {
                if (apiClient.isConnected() || apiClient.isConnecting()) {
                    onUnsubscribed(apiClient);
                    apiClient.disconnect();
                }
            }
        });
    }

    protected final <R extends Result> void setupAwarenessPendingResult(PendingResult<R> pendingResult, ResultCallback<? super R> resultCallback) {
        if(timeoutTime != null && timeoutUnit != null) {
            pendingResult.setResultCallback(resultCallback, timeoutTime, timeoutUnit);
        } else {
            pendingResult.setResultCallback(resultCallback);
        }
    }

    final GoogleApiClient createApiClient(ApiClientConnectionCallbacks apiClientConnectionCallbacks) {

        GoogleApiClient.Builder apiClientBuilder = new GoogleApiClient.Builder(ctx);


        for (Api<? extends Api.ApiOptions.NotRequiredOptions> service : services) {
            apiClientBuilder.addApi(service);
        }

        if(scopes != null) {
            for (Scope scope : scopes) {
                apiClientBuilder.addScope(scope);
            }
        }

        apiClientBuilder.addConnectionCallbacks(apiClientConnectionCallbacks);
        apiClientBuilder.addOnConnectionFailedListener(apiClientConnectionCallbacks);

        GoogleApiClient apiClient = apiClientBuilder.build();

        apiClientConnectionCallbacks.setClient(apiClient);

        return apiClient;
    }


    protected abstract void onGoogleApiClientReady(GoogleApiClient apiClient, Observer<T> observer);

    protected void onUnsubscribed(GoogleApiClient awarenessClient) { }


    class ApiClientConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener {

        final Observer<T> observer;

        private GoogleApiClient apiClient;

        private ApiClientConnectionCallbacks(Observer<T> observer) {
            this.observer = observer;
        }

        @Override
        public void onConnected(Bundle bundle) {
            try {
                onGoogleApiClientReady(apiClient, observer);
            } catch (Throwable ex) {
                observer.onError(ex);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            observer.onError(new GoogleAPIConnectionSuspendedException(cause));
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            observer.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
        }

        public void setClient(GoogleApiClient client) {
            this.apiClient = client;
        }
    }
}
