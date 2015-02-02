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
package com.droiddevil.myuber;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.widget.RemoteViews;

import com.droiddevil.myuber.annotations.ForApplication;
import com.droiddevil.myuber.data.api.UberService;
import com.droiddevil.myuber.db.models.WidgetRecord;
import com.droiddevil.myuber.uber.UberPrice;
import com.droiddevil.myuber.uber.UberPriceResponse;
import com.droiddevil.myuber.uber.UberTime;
import com.droiddevil.myuber.uber.UberTimeResponse;
import com.droiddevil.myuber.utils.LocationUtils;
import com.droiddevil.myuber.utils.UberUtils;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class WidgetUpdateService extends IntentService {

    private static final String WORKER_NAME = "WidgetUpdateService-worker";

    private static final String KEY_WIDGET_IDS = "widget_ids";

    public static Intent createStartIntent(Context context, int[] appWidgetIds) {
        Intent intent = new Intent(context, WidgetUpdateService.class);
        intent.putExtra(KEY_WIDGET_IDS, appWidgetIds);
        return intent;
    }

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    UberService mUberService;

    private Location mUserLocation;

    public WidgetUpdateService() {
        super(WORKER_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Inject
        MyUberApplication application = MyUberApplication.get(this);
        application.getApplicationGraph().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get user location
        try {
            mUserLocation = LocationUtils.getUserLocation(mContext).toBlocking().first();
        } catch (Exception e) {
            Timber.e(e, "Could not retrieve GPS coordinates");
            return;
        }
        Timber.i("Retrieved user location");

        int[] appWidgetIds = intent.getIntArrayExtra(KEY_WIDGET_IDS);
        for (int id : appWidgetIds) {
            List<WidgetRecord> records = WidgetRecord.find(WidgetRecord.class, "APP_WIDGET_ID = ?", String.valueOf(id));
            if (records.size() == 0) {
                Timber.e("Could not find widget record for widget id: %s", id);
            } else {
                Timber.i("Updating widget: %s", id);
                updateWidget(records.get(0));
            }
        }
    }

    private void updateWidget(WidgetRecord record) {
        UberPriceResponse uberPriceResponse = null;
        try {
            uberPriceResponse = mUberService.getPriceEstimates(
                mUserLocation.getLatitude(),
                mUserLocation.getLongitude(),
                record.getDestinationLatitude(),
                record.getDestinationLongitude())
                .toBlocking().first();
        } catch (Exception e) {
            Timber.e(e, "Could not get Uber pricing");
            return;
        }

        UberTimeResponse uberTimeResponse = null;
        try {
            uberTimeResponse = mUberService.getTimeEstimates(
                record.getUberProductId(),
                mUserLocation.getLatitude(),
                mUserLocation.getLongitude())
                .toBlocking().first();
        } catch (Exception e) {
            Timber.e(e, "Could not get Uber timing");
            return;
        }

        // Get price for currently selected product id
        UberPrice price = UberUtils.findPriceByProductId(record.getUberProductId(), uberPriceResponse.getPrices());
        int priceDuration = 0;
        String priceEstimate = "...";
        if (price != null) {
            priceDuration = price.getDuration();
            priceEstimate = price.getEstimate();
        }

        // Get time for currently selected product id
        UberTime time = UberUtils.findTimeByProductId(record.getUberProductId(), uberTimeResponse.getTimes());
        int timeEstimate = 0;
        if (time != null) {
            timeEstimate = time.getEstimate();
        }

        // Compute total eta
        String eta = "...";
        int totalDuration = priceDuration + timeEstimate;
        if (totalDuration > 0) {
            eta = UberUtils.getFriendlyDuration(totalDuration);
        }

        // Get widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

        // Update widget layout
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_small);
        views.setTextViewText(R.id.title, record.getTitle());
        views.setTextViewText(R.id.ride, record.getUberProductDisplayName());
        views.setTextViewText(R.id.eta, eta);
        views.setTextViewText(R.id.cost, priceEstimate);

        // Build click intent
        String uriString = null;
        String encodedAddress = Uri.encode(record.getDestinationAddress());
        PackageManager pm = mContext.getPackageManager();
        try {
            // Native
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            uriString = String.format("uber://?client_id=%s&action=setPickup&product_id=%s&dropoff[formatted_address]=%s&dropoff[latitude]=%s&dropoff[longitude]=%s", BuildConfig.UBER_CLIENT_ID, record.getUberProductId(), encodedAddress, record.getDestinationLatitude(), record.getDestinationLongitude());
        } catch (PackageManager.NameNotFoundException e) {
            // Web
            uriString = String.format("https://m.uber.com/sign-up?client_id=%s&product_id=%s&dropoff_address=%s&dropoff_latitude=%s&dropoff_longitude=%s", BuildConfig.UBER_CLIENT_ID, record.getUberProductId(), encodedAddress, record.getDestinationLatitude(), record.getDestinationLongitude());
        }

        // Install click intent
        Intent clickIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));
        PendingIntent pendingClickIntent = PendingIntent.getActivity(mContext, 0, clickIntent, 0);
        views.setOnClickPendingIntent(R.id.container, pendingClickIntent);

        // Update widget
        appWidgetManager.updateAppWidget(record.getAppWidgetId(), views);
    }
}
