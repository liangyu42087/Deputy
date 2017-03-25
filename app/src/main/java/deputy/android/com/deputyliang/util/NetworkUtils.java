/*
 * Copyright (C) 2016 The Android Open Source Project
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
package deputy.android.com.deputyliang.util;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import deputy.android.com.deputyliang.network.VolleyRequestQueue;

public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    public static final String BASE_URL = "https://apjoqdqpi3.execute-api.us-west-2.amazonaws.com/dmc";
    public static final String BUSINESS_URL = BASE_URL + "/business";
    public static final String START_SHIFT_URL = BASE_URL + "/shift/start";
    public static final String END_SHIFT_URL = BASE_URL + "/shift/end";
    public static final String SHIFTS_URL = BASE_URL + "/shifts";


    public static String getResponseFromHttpUrl(String url) throws IOException {
        HttpURLConnection urlConnection = null;
        try {

            URL shiftQuery = new URL(url);
            urlConnection = (HttpURLConnection) shiftQuery.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty(VolleyRequestQueue.PARAM_AUTHORIZATION, VolleyRequestQueue.PARAM_TOKEN);
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            String response = null;
            if (hasInput) {
                response = scanner.next();
            }
            scanner.close();
            return response;
        } finally {
            if(url != null)
                urlConnection.disconnect();
        }
    }
}