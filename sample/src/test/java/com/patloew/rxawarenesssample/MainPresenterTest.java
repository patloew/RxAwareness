package com.patloew.rxawarenesssample;

import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.patloew.rxawareness.Fence;
import com.patloew.rxawareness.RxAwareness;
import com.patloew.rxawareness.Snapshot;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import rx.Observable;
import rx.Single;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
@RunWith(JUnit4.class)
public class MainPresenterTest {

    @Rule public RxSchedulersOverrideRule rxSchedulersOverrideRule = new RxSchedulersOverrideRule();

    @Mock RxAwareness rxAwareness;
    @Mock MainView mainView;

    @Mock Snapshot snapshot;
    @Mock Fence fence;

    MainPresenter mainPresenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        doReturn(snapshot).when(rxAwareness).snapshot();
        doReturn(fence).when(rxAwareness).fence();

        mainPresenter = new MainPresenter(rxAwareness);
        mainPresenter.attachView(mainView);
    }

    @Test
    public void test_HeadphonePluggedIn_WeatherClearStormy() {
        HeadphoneState headphoneState = Mockito.mock(HeadphoneState.class);
        doReturn(HeadphoneState.PLUGGED_IN).when(headphoneState).getState();
        doReturn(Single.just(headphoneState)).when(snapshot).getHeadphoneState();

        Weather weather = Mockito.mock(Weather.class);
        doReturn(new int[] {Weather.CONDITION_CLEAR, Weather.CONDITION_STORMY}).when(weather).getConditions();
        doReturn(Single.just(weather)).when(snapshot).getWeather();

        doReturn(Observable.empty()).when(fence).listenForStateChanges(Matchers.anyString(), Matchers.any(AwarenessFence.class));

        mainPresenter.getAwarenessData();

        verify(mainView, times(1)).onHeadphoneStateLoaded(Matchers.eq("Plugged In"));
        verify(mainView, times(1)).onWeatherLoaded(Matchers.eq("Clear, Stormy"));
    }
}
