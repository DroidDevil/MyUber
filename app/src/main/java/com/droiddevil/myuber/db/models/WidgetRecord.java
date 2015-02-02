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
package com.droiddevil.myuber.db.models;

import com.orm.SugarRecord;

public class WidgetRecord extends SugarRecord<WidgetRecord> {

    private int appWidgetId;

    private String title;

    private String destinationAddress;

    private double destinationLatitude;

    private double destinationLongitude;

    private String uberProductId;

    private String uberProductDisplayName;

    public int getAppWidgetId() {
        return appWidgetId;
    }

    public void setAppWidgetId(int appWidgetId) {
        this.appWidgetId = appWidgetId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getUberProductId() {
        return uberProductId;
    }

    public void setUberProductId(String uberProductId) {
        this.uberProductId = uberProductId;
    }

    public String getUberProductDisplayName() {
        return uberProductDisplayName;
    }

    public void setUberProductDisplayName(String uberProductDisplayName) {
        this.uberProductDisplayName = uberProductDisplayName;
    }
}
