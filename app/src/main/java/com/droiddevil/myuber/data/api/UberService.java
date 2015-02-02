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

import com.droiddevil.myuber.uber.UberPriceResponse;
import com.droiddevil.myuber.uber.UberProductResponse;
import com.droiddevil.myuber.uber.UberTimeResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface UberService {

    @GET("/v1/products")
    Observable<UberProductResponse> getProducts(@Query("latitude") double latitude, @Query("longitude") double longitude);

    @GET("/v1/estimates/price")
    Observable<UberPriceResponse> getPriceEstimates(@Query("start_latitude") double startLatitude,
                                                    @Query("start_longitude") double startLongitude,
                                                    @Query("end_latitude") double endLatitude,
                                                    @Query("end_longitude") double endLongitude);

    @GET("/v1/estimates/time")
    Observable<UberTimeResponse> getTimeEstimates(@Query("product_id") String productId,
                                                   @Query("start_latitude") double startLatitude,
                                                   @Query("start_longitude") double startLongitude);
}
