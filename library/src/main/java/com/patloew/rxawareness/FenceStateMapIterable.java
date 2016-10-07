package com.patloew.rxawareness;

import android.support.annotation.NonNull;

import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;

import java.util.Iterator;

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
class FenceStateMapIterable implements Iterable<FenceState>, Iterator<FenceState> {

    private final FenceStateMap fenceStateMap;
    private final Iterator<String> keyIterator;

    FenceStateMapIterable(@NonNull FenceStateMap fenceStateMap) {
        this.fenceStateMap = fenceStateMap;
        this.keyIterator = fenceStateMap.getFenceKeys().iterator();
    }

    @Override
    public Iterator<FenceState> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return keyIterator.hasNext();
    }

    @Override
    public FenceState next() {
        return fenceStateMap.getFenceState(keyIterator.next());
    }
}
