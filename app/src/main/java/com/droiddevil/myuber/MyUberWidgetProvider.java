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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.os.Bundle;

import com.droiddevil.myuber.db.models.WidgetRecord;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class MyUberWidgetProvider extends AppWidgetProvider {

    @Inject
    Tracker mAnalyticsTracker;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        // Start update service
        context.startService(WidgetUpdateService.createStartIntent(context, appWidgetIds));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        // Inject
        MyUberApplication.get(context).getApplicationGraph().inject(this);

        for (int id : appWidgetIds) {
            List<WidgetRecord> records = WidgetRecord.find(WidgetRecord.class, "APP_WIDGET_ID = ?", String.valueOf(id));
            if (records.size() == 0) {
                Timber.i("User cancelled widget creation");

                // Track event
                mAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("Widget").setAction("Cancel").setValue(appWidgetIds.length).build());
            } else {
                for (WidgetRecord record : records) {
                    Timber.i("Deleting widget: %s", id);

                    // Track event
                    mAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("Widget").setAction("Remove").setValue(appWidgetIds.length).build());

                    // Delete
                    record.delete();
                }
            }
        }
    }
}
