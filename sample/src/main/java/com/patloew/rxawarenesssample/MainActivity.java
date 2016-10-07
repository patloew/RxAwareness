package com.patloew.rxawarenesssample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.patloew.rxawareness.RxAwareness;

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
 * limitations under the License. */
public class MainActivity extends AppCompatActivity implements MainView {

    private TextView headphoneStateTextView;
    private TextView weatherTextView;
    private ProgressBar progressBar;

    private RxAwareness rxAwareness;

    private MainPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        headphoneStateTextView = (TextView) findViewById(R.id.tv_headphonestate);
        weatherTextView = (TextView) findViewById(R.id.tv_weather);
        progressBar = (ProgressBar) findViewById(R.id.pb_main);

        rxAwareness = new RxAwareness(this);
        rxAwareness.setDefaultTimeout(15, TimeUnit.SECONDS);

        presenter = new MainPresenter(rxAwareness);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.attachView(this);
        presenter.getAwarenessData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.detachView();
    }

    @Override
    public void onHeadphoneStateLoaded(String headphoneState) {
        headphoneStateTextView.setText("Headphone State: " + headphoneState);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onWeatherLoaded(String conditionString) {
        weatherTextView.setText("Weather: " + conditionString);
        progressBar.setVisibility(View.GONE);
    }

}
