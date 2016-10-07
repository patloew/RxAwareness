# Reactive Awareness API Library for Android

[![Build Status](https://travis-ci.org/patloew/RxAwareness.svg?branch=master)](https://travis-ci.org/patloew/RxAwareness) [![API](https://img.shields.io/badge/API-9%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=9)

This library wraps the Awareness API in [RxJava](https://github.com/ReactiveX/RxJava) Observables, Singles and Completables. No more managing GoogleApiClients!

# Usage

Create an RxAwareness instance once, preferably in your Application's `onCreate()` or by using a dependency injection framework. The RxAwareness class is very similar to the Awareness class provided by the Awareness API. Instead of `Awareness.SnapshotApi.getHeadphoneState(apiClient)` you can use `rxAwareness.snapshot().getHeadphoneState()`. Make sure to have the right permissions from Marshmallow on, if they are needed by your Awareness API requests.

Example:

```java
// Create one instance and share it
RxAwareness rxAwareness = new RxAwareness(context);

rxAwareness.snapshot().getWeather()
    .subscribe(weather -> {
        // do something
    });

rxAwareness.fence().listenForStateChanges("headphoneState", HeadphoneFence.pluggingIn())
    .map(FenceState::getCurrentState)
    .subscribe(currentFenceState -> {
        // do something
    });
```

An optional global default timeout for all Awareness API requests made through the library can be set via `rxAwareness.setDefaultTimeout(...)`. In addition, timeouts can be set when creating a new Observable by providing timeout parameters, e.g. `rxAwareness.snapshot().getHeadphoneState(15, TimeUnit.SECONDS)`. These parameters override the default timeout. When a timeout occurs, a StatusException is provided via `onError()`. The RxJava timeout operators can be used instead, but these do not cancel the Awareness API request immediately.

You can also obtain a `Single<GoogleApiClient>`, which connects on subscribe and disconnects on unsubscribe via `GoogleAPIClientSingle.create(...)`.

The following Exceptions are thrown in the lib and provided via `onError()`:

* `StatusException`: When the call to the Awareness API was not successful or timed out.
* `GoogleAPIConnectionException`: When connecting to the GoogleAPIClient was not successful.
* `GoogleAPIConnectionSuspendedException`: When the GoogleApiClient connection was suspended.

# Sample

A basic sample app is available in the `sample` project. You need to create an API Key for the sample app, see the [guide in the Awareness API docs](https://developers.google.com/awareness/android-api/get-a-key).

# Setup

The library is available on jCenter. Add the following to your `build.gradle`:

	dependencies {
	    compile 'com.patloew.rxawareness:rxawareness:1.0.0'
	}

# Testing

When unit testing your app's classes, RxAwareness behavior can be mocked easily. See the `MainPresenterTest` in the `sample` project for an example test.

# Credits

The code for managing the GoogleApiClient is taken from the [Android-ReactiveLocation](https://github.com/mcharmas/Android-ReactiveLocation) library by Michał Charmas, which is licensed under the Apache License, Version 2.0.

# License

	Copyright 2016 Patrick Löwenstein

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	    http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.