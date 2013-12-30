/*
 * Copyright (C) 2013 Jan Renz
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

package de.janrenz.app.mediathek;

import android.annotation.SuppressLint;
import android.net.http.HttpResponseCache;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.io.IOException;

import org.holoeverywhere.app.*;

public class BaseApplication extends org.holoeverywhere.app.Application {




    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        ImageLoaderConfiguration loadingOptions = new ImageLoaderConfiguration.Builder(getApplicationContext())
           .build();
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoader.getInstance().init(loadingOptions);
        try {
        	if (Integer.valueOf(android.os.Build.VERSION.SDK)>13){
        		File httpCacheDir = new File(getApplicationContext().getCacheDir(), "http");
        		long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
        		HttpResponseCache.install(httpCacheDir, httpCacheSize);
        		
        	}
         }
         catch (IOException e) {
            Log.i("APP WIDE", "HTTP response cache installation failed:" + e);
        
        }
    }
}