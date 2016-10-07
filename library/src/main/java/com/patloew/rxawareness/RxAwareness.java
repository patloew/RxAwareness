package com.patloew.rxawareness;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.TimeUnit;

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
 * limitations under the License.
 *
 * -----------------------------
 *
 * Make sure to have the permissions on Marshmallow, if they are needed
 * by your Awareness API requests.
 *
 */
public class RxAwareness {

    Long timeoutTime = null;
    TimeUnit timeoutUnit = null;

    final Context ctx;

    private final Fence fence = new Fence(this);
    private final Snapshot snapshot = new Snapshot(this);


    /* Creates a new RxAwareness instance.
     *
     * @param ctx Context.
     */
    public RxAwareness(@NonNull Context ctx) {
        this.ctx = ctx.getApplicationContext();
    }


    /* Set a default timeout for all requests to the Awareness API made in the lib.
     * When a timeout occurs, onError() is called with a StatusException.
     */
    public void setDefaultTimeout(long time, @NonNull TimeUnit timeUnit) {
        if(timeUnit != null) {
            timeoutTime = time;
            timeoutUnit = timeUnit;
        } else {
            throw new IllegalArgumentException("timeUnit parameter must not be null");
        }
    }

    /* Reset the default timeout.
     */
    public void resetDefaultTimeout() {
        timeoutTime = null;
        timeoutUnit = null;
    }


    public Fence fence() {
        return fence;
    }

    public Snapshot snapshot() {
        return snapshot;
    }
}
