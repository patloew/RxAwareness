package com.patloew.rxawareness;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.FenceApi;
import com.google.android.gms.awareness.SnapshotApi;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceQueryRequest;
import com.google.android.gms.awareness.fence.FenceQueryResult;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceStateMap;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.snapshot.BeaconStateResult;
import com.google.android.gms.awareness.snapshot.DetectedActivityResult;
import com.google.android.gms.awareness.snapshot.HeadphoneStateResult;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.BeaconState;
import com.google.android.gms.awareness.state.HeadphoneState;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.places.PlaceLikelihood;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareOnlyThisForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import rx.Emitter;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;

@SuppressWarnings("MissingPermission")
@RunWith(PowerMockRunner.class)
@PrepareOnlyThisForTest({ Awareness.class, Status.class, ConnectionResult.class, BaseEmitter.class})
@SuppressStaticInitializationFor("com.google.android.gms.awareness.Awareness")
public class RxAwarenessTest {

    @Mock Context ctx;

    @Mock GoogleApiClient apiClient;
    @Mock Status status;
    @Mock ConnectionResult connectionResult;
    @Mock PendingResult pendingResult;

    @Mock FenceApi fenceApi;
    @Mock SnapshotApi snapshotApi;

    RxAwareness rxAwareness;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(Awareness.class);
        Whitebox.setInternalState(Awareness.class, fenceApi);
        Whitebox.setInternalState(Awareness.class, snapshotApi);

        when(ctx.getApplicationContext()).thenReturn(ctx);
        when(apiClient.isConnected()).thenReturn(true);

        rxAwareness = new RxAwareness(ctx);
    }


    //////////////////
    // UTIL METHODS //
    //////////////////

    // Mock GoogleApiClient connection success behaviour
    private <T> void setupBaseEmitterSuccess(final BaseEmitter<T> baseEmitter) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Observer<T> observer = ((BaseEmitter.ApiClientConnectionCallbacks)invocation.getArguments()[0]).observer;

                doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        baseEmitter.onGoogleApiClientReady(apiClient, observer);
                        return null;
                    }
                }).when(apiClient).connect();

                return apiClient;
            }
        }).when(baseEmitter).createApiClient(Matchers.any(BaseEmitter.ApiClientConnectionCallbacks.class));
    }

    // Mock GoogleApiClient connection error behaviour
    private <T> void setupBaseEmitterError(final BaseEmitter<T> baseEmitter) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                final Observer<T> observer = ((BaseEmitter.ApiClientConnectionCallbacks)invocation.getArguments()[0]).observer;

                doAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        observer.onError(new GoogleAPIConnectionException("Error connecting to GoogleApiClient.", connectionResult));
                        return null;
                    }
                }).when(apiClient).connect();

                return apiClient;
            }
        }).when(baseEmitter).createApiClient(Matchers.any(BaseEmitter.ApiClientConnectionCallbacks.class));
    }

    @SuppressWarnings("unchecked")
    private void setPendingResultValue(final Result result) {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((ResultCallback)invocation.getArguments()[0]).onResult(result);
                return null;
            }
        }).when(pendingResult).setResultCallback(Matchers.<ResultCallback>any());
    }

    private static void assertError(TestSubscriber sub, Class<? extends Throwable> errorClass) {
        sub.assertError(errorClass);
        sub.assertNoValues();
        sub.assertUnsubscribed();
    }

    @SuppressWarnings("unchecked")
    private static void assertSingleValue(TestSubscriber sub, Object value) {
        sub.assertCompleted();
        sub.assertUnsubscribed();
        sub.assertValue(value);
    }

    //////////////////////
    // OBSERVABLE TESTS //
    //////////////////////


    // GoogleApiClientSingle

    @Test
    public void GoogleAPIClientSingle_Success() {
        TestSubscriber<GoogleApiClient> sub = new TestSubscriber<>();
        GoogleAPIClientSingle emitter = PowerMockito.spy(new GoogleAPIClientSingle(ctx, new Api[] {}, new Scope[] {}));

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, apiClient);
    }

    @Test
    public void GoogleAPIClientSingle_ConnectionException() {
        TestSubscriber<GoogleApiClient> sub = new TestSubscriber<>();
        final GoogleAPIClientSingle emitter = PowerMockito.spy(new GoogleAPIClientSingle(ctx, new Api[] {}, new Scope[] {}));

        setupBaseEmitterError(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, GoogleAPIConnectionException.class);
    }


    /*********
     * Fence *
     *********/

    // FenceQueryEmitter

    @Test
    public void FenceQueryEmitter_Success() {
        TestSubscriber<FenceStateMap> sub = new TestSubscriber<>();
        FenceStateMap fenceStateMap = Mockito.mock(FenceStateMap.class);
        FenceQueryRequest request = Mockito.mock(FenceQueryRequest.class);
        FenceQueryResult result = Mockito.mock(FenceQueryResult.class);

        FenceQueryEmitter emitter = PowerMockito.spy(new FenceQueryEmitter(rxAwareness, request, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(fenceStateMap).when(result).getFenceStateMap();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(fenceApi.queryFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, fenceStateMap);
    }

    @Test
    public void FenceQueryEmitter_StatusException() {
        TestSubscriber<FenceStateMap> sub = new TestSubscriber<>();
        FenceQueryRequest request = Mockito.mock(FenceQueryRequest.class);
        FenceQueryResult result = Mockito.mock(FenceQueryResult.class);

        FenceQueryEmitter emitter = PowerMockito.spy(new FenceQueryEmitter(rxAwareness, request, null, null));

        doReturn(status).when(result).getStatus();
        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(fenceApi.queryFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);



        assertError(sub, StatusException.class);
    }

    // FenceUpdateEmitter

    @Test
    public void FenceUpdateEmitter_Success() {
        TestSubscriber<Status> sub = new TestSubscriber<>();
        FenceUpdateRequest request = Mockito.mock(FenceUpdateRequest.class);

        FenceUpdateEmitter emitter = PowerMockito.spy(new FenceUpdateEmitter(rxAwareness, request, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(fenceApi.updateFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, status);
    }

    @Test
    public void FenceUpdateEmitter_StatusException() {
        TestSubscriber<Status> sub = new TestSubscriber<>();
        FenceUpdateRequest request = Mockito.mock(FenceUpdateRequest.class);

        FenceUpdateEmitter emitter = PowerMockito.spy(new FenceUpdateEmitter(rxAwareness, request, null, null));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(false);
        when(fenceApi.updateFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }

    // FenceStateEmitter

    @Test
    public void FenceStateEmitter_NoValues() {
        TestSubscriber<FenceState> sub = new TestSubscriber<>();

        FenceUpdateRequest request = Mockito.mock(FenceUpdateRequest.class);
        AwarenessFence fence = Mockito.mock(AwarenessFence.class);
        FenceStateEmitter emitter = PowerMockito.spy(new FenceStateEmitter(rxAwareness, "fenceKey", fence, null, null));

        doReturn(request).when(emitter).getRequestAndRegisterReceiver(Matchers.any(Observer.class));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(fenceApi.updateFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        sub.assertNoValues();
        sub.assertNoTerminalEvent();
    }

    @Test
    public void FenceStateEmitter_TwoValues() {
        TestSubscriber<FenceState> sub = new TestSubscriber<>();

        FenceState fenceState = Mockito.mock(FenceState.class);
        FenceState fenceState2 = Mockito.mock(FenceState.class);
        FenceUpdateRequest request = Mockito.mock(FenceUpdateRequest.class);
        AwarenessFence fence = Mockito.mock(AwarenessFence.class);
        FenceStateEmitter emitter = PowerMockito.spy(new FenceStateEmitter(rxAwareness, "fenceKey", fence, null, null));

        ArgumentCaptor<Observer> argumentCaptor = ArgumentCaptor.forClass(Observer.class);
        doReturn(request).when(emitter).getRequestAndRegisterReceiver(argumentCaptor.capture());

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(fenceApi.updateFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        argumentCaptor.getValue().onNext(fenceState);
        argumentCaptor.getValue().onNext(fenceState2);

        sub.assertReceivedOnNext(Arrays.asList(fenceState, fenceState2));
        sub.assertNoTerminalEvent();
    }

    @Test
    public void FenceStateEmitter_SingleValueWithUnsubscribe() {
        TestSubscriber<FenceState> sub = new TestSubscriber<>();

        FenceStateEmitter.RxAwarenessFenceReceiver receiver = Mockito.mock(FenceStateEmitter.RxAwarenessFenceReceiver.class);
        FenceState fenceState = Mockito.mock(FenceState.class);
        FenceState fenceState2 = Mockito.mock(FenceState.class);
        FenceUpdateRequest request = Mockito.mock(FenceUpdateRequest.class);
        AwarenessFence fence = Mockito.mock(AwarenessFence.class);
        FenceStateEmitter emitter = PowerMockito.spy(new FenceStateEmitter(rxAwareness, "fenceKey", fence, null, null));
        Whitebox.setInternalState(emitter, "receiver", receiver);

        ArgumentCaptor<Observer> argumentCaptor = ArgumentCaptor.forClass(Observer.class);
        doReturn(request).when(emitter).getRequestAndRegisterReceiver(argumentCaptor.capture());

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(true);
        when(fenceApi.updateFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Subscription subscription = Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        argumentCaptor.getValue().onNext(fenceState2);
        subscription.unsubscribe();
        argumentCaptor.getValue().onNext(fenceState);

        verify(fenceApi, times(2)).updateFences(Matchers.eq(apiClient), Matchers.any(FenceUpdateRequest.class));
        verify(ctx, times(1)).unregisterReceiver(receiver);

        sub.assertValue(fenceState2);
        sub.assertNoTerminalEvent();
    }


    @Test
    public void FenceStateEmitter_Error() {
        TestSubscriber<FenceState> sub = new TestSubscriber<>();

        FenceStateEmitter.RxAwarenessFenceReceiver receiver = Mockito.mock(FenceStateEmitter.RxAwarenessFenceReceiver.class);
        FenceUpdateRequest request = Mockito.mock(FenceUpdateRequest.class);
        AwarenessFence fence = Mockito.mock(AwarenessFence.class);
        FenceStateEmitter emitter = PowerMockito.spy(new FenceStateEmitter(rxAwareness, "fenceKey", fence, null, null));
        Whitebox.setInternalState(emitter, "receiver", receiver);

        doReturn(request).when(emitter).getRequestAndRegisterReceiver(Matchers.any(Observer.class));

        setPendingResultValue(status);
        when(status.isSuccess()).thenReturn(false);
        when(fenceApi.updateFences(apiClient, request)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        verify(fenceApi, times(2)).updateFences(Matchers.eq(apiClient), Matchers.any(FenceUpdateRequest.class));
        verify(ctx, times(1)).unregisterReceiver(receiver);

        assertError(sub, StatusException.class);
    }


    /************
     * Snapshot *
     ************/

    // SnapshotGetBeaconStateEmitter

    @Test
    public void SnapshotGetBeaconStateEmitter_Success() {
        TestSubscriber<List<BeaconState.BeaconInfo>> sub = new TestSubscriber<>();
        Collection<BeaconState.TypeFilter> typeFilters = new ArrayList<>();
        List<BeaconState.BeaconInfo> beaconInfos = new ArrayList<>();
        BeaconState beaconState = Mockito.mock(BeaconState.class);
        BeaconStateResult result = Mockito.mock(BeaconStateResult.class);

        SnapshotGetBeaconStateEmitter emitter = PowerMockito.spy(new SnapshotGetBeaconStateEmitter(rxAwareness, typeFilters, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(beaconState).when(result).getBeaconState();
        doReturn(beaconInfos).when(beaconState).getBeaconInfo();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(snapshotApi.getBeaconState(apiClient, typeFilters)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, beaconInfos);
    }

    @Test
    public void SnapshotGetBeaconStateEmitter_StatusException() {
        TestSubscriber<List<BeaconState.BeaconInfo>> sub = new TestSubscriber<>();
        Collection<BeaconState.TypeFilter> typeFilters = new ArrayList<>();

        BeaconStateResult result = Mockito.mock(BeaconStateResult.class);

        SnapshotGetBeaconStateEmitter emitter = PowerMockito.spy(new SnapshotGetBeaconStateEmitter(rxAwareness, typeFilters, null, null));

        doReturn(status).when(result).getStatus();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(snapshotApi.getBeaconState(apiClient, typeFilters)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }


    // SnapshotGetDetectedActivityEmitter

    @Test
    public void SnapshotGetDetectedActivityEmitter_Success() {
        TestSubscriber<ActivityRecognitionResult> sub = new TestSubscriber<>();
        ActivityRecognitionResult activityRecognitionResult = Mockito.mock(ActivityRecognitionResult.class);
        DetectedActivityResult result = Mockito.mock(DetectedActivityResult.class);

        SnapshotGetDetectedActivityEmitter emitter = PowerMockito.spy(new SnapshotGetDetectedActivityEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(activityRecognitionResult).when(result).getActivityRecognitionResult();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(snapshotApi.getDetectedActivity(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, activityRecognitionResult);
    }

    @Test
    public void SnapshotGetDetectedActivityEmitter_StatusException() {
        TestSubscriber<ActivityRecognitionResult> sub = new TestSubscriber<>();
        DetectedActivityResult result = Mockito.mock(DetectedActivityResult.class);

        SnapshotGetDetectedActivityEmitter emitter = PowerMockito.spy(new SnapshotGetDetectedActivityEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(snapshotApi.getDetectedActivity(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }


    // SnapshotGetDetectedActivityEmitter

    @Test
    public void SnapshotGetHeadphoneStateEmitter_Success() {
        TestSubscriber<HeadphoneState> sub = new TestSubscriber<>();
        HeadphoneState headphoneState = Mockito.mock(HeadphoneState.class);
        HeadphoneStateResult result = Mockito.mock(HeadphoneStateResult.class);

        SnapshotGetHeadphoneStateEmitter emitter = PowerMockito.spy(new SnapshotGetHeadphoneStateEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(headphoneState).when(result).getHeadphoneState();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(snapshotApi.getHeadphoneState(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, headphoneState);
    }

    @Test
    public void SnapshotGetHeadphoneStateEmitter_StatusException() {
        TestSubscriber<HeadphoneState> sub = new TestSubscriber<>();
        HeadphoneStateResult result = Mockito.mock(HeadphoneStateResult.class);

        SnapshotGetHeadphoneStateEmitter emitter = PowerMockito.spy(new SnapshotGetHeadphoneStateEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(snapshotApi.getHeadphoneState(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }


    // SnapshotGetDetectedActivityEmitter

    @Test
    public void SnapshotGetLocationEmitter_Success() {
        TestSubscriber<Location> sub = new TestSubscriber<>();
        Location location = Mockito.mock(Location.class);
        LocationResult result = Mockito.mock(LocationResult.class);

        SnapshotGetLocationEmitter emitter = PowerMockito.spy(new SnapshotGetLocationEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(location).when(result).getLocation();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(snapshotApi.getLocation(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, location);
    }

    @Test
    public void SnapshotGetLocationEmitter_StatusException() {
        TestSubscriber<Location> sub = new TestSubscriber<>();
        LocationResult result = Mockito.mock(LocationResult.class);

        SnapshotGetLocationEmitter emitter = PowerMockito.spy(new SnapshotGetLocationEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(snapshotApi.getLocation(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }


    // SnapshotGetDetectedActivityEmitter

    @Test
    public void SnapshotGetPlacesEmitter_Success() {
        TestSubscriber<List<PlaceLikelihood>> sub = new TestSubscriber<>();
        List<PlaceLikelihood> placeLikelihoods = new ArrayList<>();
        PlacesResult result = Mockito.mock(PlacesResult.class);

        SnapshotGetPlacesEmitter emitter = PowerMockito.spy(new SnapshotGetPlacesEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(placeLikelihoods).when(result).getPlaceLikelihoods();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(snapshotApi.getPlaces(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, placeLikelihoods);
    }

    @Test
    public void SnapshotGetPlacesEmitter_StatusException() {
        TestSubscriber<List<PlaceLikelihood>> sub = new TestSubscriber<>();
        PlacesResult result = Mockito.mock(PlacesResult.class);

        SnapshotGetPlacesEmitter emitter = PowerMockito.spy(new SnapshotGetPlacesEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(snapshotApi.getPlaces(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }


    // SnapshotGetWeatherEmitter

    @Test
    public void SnapshotGetWeatherEmitter_Success() {
        TestSubscriber<Weather> sub = new TestSubscriber<>();
        Weather weather = Mockito.mock(Weather.class);
        WeatherResult result = Mockito.mock(WeatherResult.class);

        SnapshotGetWeatherEmitter emitter = PowerMockito.spy(new SnapshotGetWeatherEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();
        doReturn(weather).when(result).getWeather();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(true);
        when(snapshotApi.getWeather(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertSingleValue(sub, weather);
    }

    @Test
    public void SnapshotGetWeatherEmitter_StatusException() {
        TestSubscriber<Weather> sub = new TestSubscriber<>();
        WeatherResult result = Mockito.mock(WeatherResult.class);

        SnapshotGetWeatherEmitter emitter = PowerMockito.spy(new SnapshotGetWeatherEmitter(rxAwareness, null, null));

        doReturn(status).when(result).getStatus();

        setPendingResultValue(result);
        when(status.isSuccess()).thenReturn(false);
        when(snapshotApi.getWeather(apiClient)).thenReturn(pendingResult);

        setupBaseEmitterSuccess(emitter);
        Observable.fromEmitter(emitter, Emitter.BackpressureMode.LATEST).subscribe(sub);

        assertError(sub, StatusException.class);
    }

}
