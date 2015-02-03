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

import android.app.Application;
import android.content.Context;

import com.droiddevil.myuber.annotations.ForApplication;
import com.droiddevil.myuber.data.DataModule;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
    library = true,
    includes = {
        DataModule.class
    },
    injects = {
        WidgetUpdateService.class,
        MyUberWidgetProvider.class
    }
)
public class BaseModule {

    private final Application mApplication;

    public BaseModule(Application application) {
        this.mApplication = application;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    Tracker provideAnalyticsTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(mApplication);
        return analytics.newTracker(R.xml.ga_tracker);
    }

}
