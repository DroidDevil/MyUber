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

import com.droiddevil.myuber.uber.UberPrice;
import com.droiddevil.myuber.uber.UberTime;

import java.util.List;

public class UberUtils {

    public static UberPrice findPriceByProductId(String productId, List<UberPrice> prices) {
        for (UberPrice price : prices) {
            if (productId.equals(price.getProductId())) {
                return price;
            }
        }
        return null;
    }

    public static UberTime findTimeByProductId(String productId, List<UberTime> times) {
        for (UberTime time : times) {
            if (productId.equals(time.getProductId())) {
                return time;
            }
        }
        return null;
    }

    public static String getFriendlyDuration(int rawSeconds) {
        int timeMinutes = rawSeconds / 60;

        int seconds = rawSeconds % 60;
        int minutes = timeMinutes % 60;
        int hours = timeMinutes / 60;

        // Use seconds to bump minutes
        if (seconds > 30) {
            minutes++;
        }

        // Set min minutes
        minutes = Math.max(1, minutes);

        if (hours == 0) {
            return String.format("%d min", minutes);
        } else {
            return String.format("%d hr %d mins");
        }
    }
}
