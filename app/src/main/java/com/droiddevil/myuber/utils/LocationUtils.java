/*
 * Copyright (C) 2015 Tanner Perrien
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
 */
package com.droiddevil.myuber.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import rx.Observable;
import rx.Subscriber;
import timber.log.Timber;

public class LocationUtils {

    private static final int MAX_ACCURACY_METERS = 100;

    private static final int MAX_STALE_TIME_MS = 2 * 60 * 1000; // 2 min

    public static Observable<Location> getUserLocation(final Context context) {
        return Observable.create(new Observable.OnSubscribe<Location>() {

            @Override
            public void call(final Subscriber<? super Location> subscriber) {
                // Get GPS fix
                Location location = SmartLocation.with(context).location().getLastLocation();
                if (location != null && acceptLocation(location)) {
                    subscriber.onNext(location);
                    subscriber.onCompleted();
                } else {
                    SmartLocation.with(context).location().oneFix().start(new OnLocationUpdatedListener() {
                        @Override
                        public void onLocationUpdated(Location location) {
                            if (!acceptLocation(location)) {
                                Timber.e("Using unacceptable location from Fuse provider");
                            }
                            subscriber.onNext(location);
                            subscriber.onCompleted();
                        }
                    });
                }
            }
        });
    }

    public static Observable<Address> getAddressFromCoords(final Context context, final double latitude, final double longitude) {
        return Observable.create(new Observable.OnSubscribe<Address>() {

            @Override
            public void call(Subscriber<? super Address> subscriber) {
                Address address = null;
                Geocoder geocoder = new Geocoder(context);
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses.size() > 0) {
                        address = addresses.get(0);
                    } else {
                        Timber.d("Could not find address for given lat/lng coordinates");
                    }
                } catch (IOException e) {
                    Timber.e(e, "An error occurred while trying to get an address from lat/lng");
                }
                subscriber.onNext(address);
                subscriber.onCompleted();
            }
        });
    }

    public static Observable<Address> getAddressFromLocation(final Context context, final String rawAddress) {
        return Observable.create(new Observable.OnSubscribe<Address>() {

            @Override
            public void call(Subscriber<? super Address> subscriber) {
                Address address = null;
                Geocoder geocoder = new Geocoder(context);
                try {
                    List<Address> addresses = geocoder.getFromLocationName(rawAddress, 1);
                    if (addresses.size() > 0) {
                        address = addresses.get(0);
                    } else {
                        Timber.d("Could not find address for given string");
                    }
                } catch (IOException e) {
                    Timber.e(e, "An error occurred while trying to get an address from string");
                }
                subscriber.onNext(address);
                subscriber.onCompleted();
            }
        });
    }

    private static boolean acceptLocation(Location location) {
        float accuracy = location.getAccuracy();
        boolean accuracyConfirmed = accuracy > 0 && accuracy < MAX_ACCURACY_METERS;
        boolean timeConfirmed = System.currentTimeMillis() - location.getTime() < MAX_STALE_TIME_MS;
        return accuracyConfirmed && timeConfirmed;
    }
}
