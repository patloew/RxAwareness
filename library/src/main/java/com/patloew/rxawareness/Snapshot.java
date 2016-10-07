package com.patloew.rxawareness;

import android.Manifest;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;

import com.google.android.gms.awareness.state.BeaconState;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
public class Snapshot {

    private final RxAwareness rxAwareness;

    Snapshot(RxAwareness rxAwareness) {
        this.rxAwareness = rxAwareness;
    }


    // getBeaconState

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Observable<BeaconState.BeaconInfo> getBeaconState(Collection<BeaconState.TypeFilter> beaconTypes) {
        return getBeaconStateInternal(beaconTypes, null, null);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Observable<BeaconState.BeaconInfo> getBeaconState(BeaconState.TypeFilter... beaconTypes) {
        return getBeaconStateInternal(Arrays.asList(beaconTypes), null, null);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Observable<BeaconState.BeaconInfo> getBeaconState(Collection<BeaconState.TypeFilter> beaconTypes, long timeout, @NonNull TimeUnit timeUnit) {
        return getBeaconStateInternal(beaconTypes, timeout, timeUnit);
    }

    private Observable<BeaconState.BeaconInfo> getBeaconStateInternal(Collection<BeaconState.TypeFilter> beaconTypes, Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new SnapshotGetBeaconStateEmitter(rxAwareness, beaconTypes, timeout, timeUnit), Emitter.BackpressureMode.LATEST)
                    .flatMap(new Func1<List<BeaconState.BeaconInfo>, Observable<BeaconState.BeaconInfo>>() {
                        @Override
                        public Observable<BeaconState.BeaconInfo> call(List<BeaconState.BeaconInfo> beaconInfos) {
                            return Observable.from(beaconInfos);
                        }
                    });
    }


    // getDetectedActivity

    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Single<ActivityRecognitionResult> getDetectedActivity() {
        return getDetectedActivityInternal(null, null);
    }

    @RequiresPermission("com.google.android.gms.permission.ACTIVITY_RECOGNITION")
    public Single<ActivityRecognitionResult> getDetectedActivity(long timeout, @NonNull TimeUnit timeUnit) {
        return getDetectedActivityInternal(timeout, timeUnit);
    }

    private Single<ActivityRecognitionResult> getDetectedActivityInternal(Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new SnapshotGetDetectedActivityEmitter(rxAwareness, timeout, timeUnit), Emitter.BackpressureMode.LATEST).toSingle();
    }


    // getHeadphoneState

    public Single<HeadphoneState> getHeadphoneState() {
        return getHeadphoneStateInternal(null, null);
    }

    public Single<HeadphoneState> getHeadphoneState(long timeout, @NonNull TimeUnit timeUnit) {
        return getHeadphoneStateInternal(timeout, timeUnit);
    }

    private Single<HeadphoneState> getHeadphoneStateInternal(Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new SnapshotGetHeadphoneStateEmitter(rxAwareness, timeout, timeUnit), Emitter.BackpressureMode.LATEST).toSingle();
    }


    // getLocation

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Single<Location> getLocation() {
        return getLocationInternal(null, null);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Single<Location> getLocation(long timeout, @NonNull TimeUnit timeUnit) {
        return getLocationInternal(timeout, timeUnit);
    }

    private Single<Location> getLocationInternal(Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new SnapshotGetLocationEmitter(rxAwareness, timeout, timeUnit), Emitter.BackpressureMode.LATEST).toSingle();
    }


    // getPlaces

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Observable<PlaceLikelihood> getPlaces() {
        return getPlacesInternal(null, null);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Observable<PlaceLikelihood> getPlaces(long timeout, @NonNull TimeUnit timeUnit) {
        return getPlacesInternal(timeout, timeUnit);
    }

    private Observable<PlaceLikelihood> getPlacesInternal(Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new SnapshotGetPlacesEmitter(rxAwareness, timeout, timeUnit), Emitter.BackpressureMode.LATEST)
                .flatMap(new Func1<List<PlaceLikelihood>, Observable<PlaceLikelihood>>() {
                    @Override
                    public Observable<PlaceLikelihood> call(List<PlaceLikelihood> placeLikelihoods) {
                        return Observable.from(placeLikelihoods);
                    }
                });
    }

    
    // getWeather

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Single<Weather> getWeather() {
        return getWeatherInternal(null, null);
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public Single<Weather> getWeather(long timeout, @NonNull TimeUnit timeUnit) {
        return getWeatherInternal(timeout, timeUnit);
    }

    private Single<Weather> getWeatherInternal(Long timeout, TimeUnit timeUnit) {
        return Observable.fromEmitter(new SnapshotGetWeatherEmitter(rxAwareness, timeout, timeUnit), Emitter.BackpressureMode.LATEST).toSingle();
    }
}
