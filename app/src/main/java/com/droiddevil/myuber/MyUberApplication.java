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

import android.content.Context;

import com.orm.SugarApp;

import dagger.ObjectGraph;
import timber.log.Timber;

public class MyUberApplication extends SugarApp {
    // TODO: Remove SugarApp extension when Sugar ORM 1.4 is released

    private ObjectGraph mApplicationGraph;

    public static MyUberApplication get(Context context) {
        return (MyUberApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mApplicationGraph = ObjectGraph.create(Modules.list(this));

        // Timber Logging initialization
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    public ObjectGraph getApplicationGraph() {
        return mApplicationGraph;
    }

    /**
     * A tree which logs important information for crash reporting.
     */
    private static class CrashReportingTree extends Timber.HollowTree {
        @Override
        public void i(String message, Object... args) {
            // TODO e.g., Crashlytics.log(String.format(message, args));
        }

        @Override
        public void i(Throwable t, String message, Object... args) {
            i(message, args);
        }

        @Override
        public void e(String message, Object... args) {
            i("ERROR: " + message, args);
        }

        @Override
        public void e(Throwable t, String message, Object... args) {
            e(message, args);

            // TODO e.g., Crashlytics.logException(t);
        }
    }
}
