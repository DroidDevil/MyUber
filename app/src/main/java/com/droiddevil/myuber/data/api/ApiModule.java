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
package com.droiddevil.myuber.data.api;

import com.droiddevil.myuber.BuildConfig;
import com.droiddevil.myuber.data.api.MyUberRequestInterceptor;
import com.droiddevil.myuber.data.api.UberService;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.Endpoint;
import retrofit.Endpoints;
import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Client;
import retrofit.converter.GsonConverter;
import timber.log.Timber;

@Module(
    library = true,
    complete = false
)
public class ApiModule {

    @Provides
    @Singleton
    Endpoint provideEndpoint() {
        return Endpoints.newFixedEndpoint("https://api.uber.com");
    }

    @Provides
    @Singleton
    RestAdapter provideRestAdapter(Endpoint endpoint, Client client, Gson gson) {
        return new RestAdapter.Builder()
            .setEndpoint(endpoint)
            .setConverter(new GsonConverter(gson))
            .setErrorHandler(new ErrorHandler() {
                @Override
                public Throwable handleError(RetrofitError cause) {
                    Timber.e(cause, "An error occurred while processing a request");
                    return cause;
                }
            })
            .setRequestInterceptor(new MyUberRequestInterceptor())
            .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
            .build();
    }

    @Provides
    @Singleton
    UberService provideUberService(RestAdapter restAdapter) {
        return restAdapter.create(UberService.class);
    }

}
