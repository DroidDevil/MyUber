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
package com.droiddevil.myuber.uber;

import com.google.gson.annotations.SerializedName;

public class UberPrice {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("currency_code")
    private String currencyCode;

    @SerializedName("display_name")
    private String displayName;

    private String estimate;

    @SerializedName("low_estimate")
    private int lowEstimate;

    @SerializedName("high_estimate")
    private int highEstimate;

    @SerializedName("surge_multiplier")
    private float surgeMultiplier;

    private int duration;

    private float distance;

    public String getProductId() {
        return productId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEstimate() {
        return estimate;
    }

    public int getLowEstimate() {
        return lowEstimate;
    }

    public int getHighEstimate() {
        return highEstimate;
    }

    public float getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public int getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }
}
