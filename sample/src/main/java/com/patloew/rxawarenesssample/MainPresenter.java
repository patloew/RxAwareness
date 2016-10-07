package com.patloew.rxawarenesssample;

import android.util.Log;

import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.HeadphoneFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.patloew.rxawareness.RxAwareness;

import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

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
public class MainPresenter {

    private static final String TAG = "MainPresenter";

    private final CompositeSubscription subscription = new CompositeSubscription();

    private final RxAwareness rxAwareness;

    private MainView view;

    public MainPresenter(RxAwareness rxAwareness) {
        this.rxAwareness = rxAwareness;
    }

    public void attachView(MainView view) {
        this.view = view;
    }

    public void detachView() {
        this.view = null;
        subscription.clear();
    }

    public void getAwarenessData() {
        subscription.add(
                rxAwareness.snapshot().getHeadphoneState()
                    .map(headphoneState -> getHeadphoneStateString(headphoneState.getState()))
                    .subscribe(view::onHeadphoneStateLoaded, throwable -> Log.e(TAG, "Error getting headphone state", throwable))
        );

        subscription.add(
                rxAwareness.fence().listenForStateChanges("headphoneState", HeadphoneFence.pluggingIn())
                    .map(FenceState::getCurrentState)
                    .subscribe(currentFenceState -> {
                        view.onHeadphoneStateLoaded(getHeadphoneStateString(currentFenceState == FenceState.TRUE ? HeadphoneState.PLUGGED_IN : HeadphoneState.UNPLUGGED));
                    }, throwable -> Log.e(TAG, "Error getting headphone state changes.", throwable))
        );

        subscription.add(
                rxAwareness.snapshot().getWeather()
                        .flatMapObservable(weather -> Observable.fromEmitter(new IntArrayEmitter(weather.getConditions()), Emitter.BackpressureMode.BUFFER))
                        .map(this::getWeatherConditionString)
                        .reduce((s, s2) -> s+", "+s2)
                        .subscribe(view::onWeatherLoaded, throwable -> Log.e(TAG, "Error getting weather.", throwable))
        );
    }

    private String getHeadphoneStateString(int state) {
        if(state == HeadphoneState.PLUGGED_IN) {
            return "Plugged In";
        } else {
            return "Unplugged";
        }
    }

    private String getWeatherConditionString(int condition) {
        switch(condition) {
            case Weather.CONDITION_CLEAR:
                return "Clear";
            case Weather.CONDITION_CLOUDY:
                return "Cloudy";
            case Weather.CONDITION_FOGGY:
                return "Foggy";
            case Weather.CONDITION_HAZY:
                return "Hazy";
            case Weather.CONDITION_ICY:
                return "Icy";
            case Weather.CONDITION_RAINY:
                return "Rainy";
            case Weather.CONDITION_SNOWY:
                return "Snowy";
            case Weather.CONDITION_STORMY:
                return "Stormy";
            case Weather.CONDITION_WINDY:
                return "Windy";
            default: // Weather.CONDITION_UNKNOWN
                return "Unknown";
        }
    }

    private static class IntArrayEmitter implements Action1<Emitter<Integer>> {

        private final int[] intArray;

        IntArrayEmitter(int[] intArray) {
            this.intArray = intArray;
        }

        @Override
        public void call(Emitter<Integer> e) {
            for(Integer i : intArray) {
                e.onNext(i);
            }

            e.onCompleted();
        }
    }

}
