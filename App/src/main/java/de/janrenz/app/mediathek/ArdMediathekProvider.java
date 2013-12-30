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

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.turbomanage.httpclient.HttpResponse;
import com.turbomanage.httpclient.android.AndroidHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Date;

public class ArdMediathekProvider extends ContentProvider {

	public static final String TAG = ArdMediathekProvider.class.getSimpleName();

	@Override
	public boolean onCreate() {

		return true;
	}

	public String readJSONFeed(String URL) {

		try {

            AndroidHttpClient httpClient = new AndroidHttpClient(URL);

		    HttpResponse httpResponse;
            httpResponse = httpClient.get("", null);
            return httpResponse.getBodyAsString();
		} catch (Exception e) {
			//Log.e("readJSONFeed", "error:" + e.getLocalizedMessage());
			return "";
		}
		
	}

	@Override
	public Cursor query(Uri uri, String[] strings, String s,
		String[] strings1, String s1) {
		String url = "";
		String queryparam = uri.getQueryParameter("timestamp");
      ;
		Integer timestamp = null;
		if (queryparam == null) {
			Date dt = new Date();
			timestamp = dt.getSeconds();
		} else {

			timestamp = Integer.parseInt(queryparam);
		}

		String queryparammethod = uri.getQueryParameter("method");
		if (queryparammethod == null) {
			queryparammethod = "list";
			url = "http://m-service.daserste.de/appservice/1.4.2/video/list/"
					+ timestamp + "?func=getVideoList&unixTimestamp="
					+ timestamp;
        } else if (queryparammethod.equalsIgnoreCase("search")) {
            // url = /appservice/1.4.1/search/heiter/0/25?func=getSearchResultList&searchPattern=heiter&searchOffset=0&searchLength=25
            String searchQuery = uri.getQueryParameter("query");
            //!TODO make this url safe
            url = "http://m-service.daserste.de/appservice/1.4.2/search/"
                    + URLEncoder.encode(searchQuery)
                    + "/0/50?func=getSearchResultList&searchPattern="
                    + URLEncoder.encode(searchQuery)
                    + "&searchOffset=0&searchLength=50";
		} else if (queryparammethod.equalsIgnoreCase("broadcast")) {
			url = "http://m-service.daserste.de/appservice/1.4.2/broadcast/current/"
					+ timestamp
					+ "?func=getCurrentBroadcast&unixTimestamp="
					+ timestamp;
		} else {
			// oh oh
		}
		String queryparamReload = uri.getQueryParameter("reload");
		String queryExtReload = "";
		if (queryparamReload != null) {
			queryExtReload = "&reload=" + Math.random();
		}

		String result = "";
		result = readJSONFeed(url);
        MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title",
				"subtitle", "image", "extId", "startTime",
				"startTimeAsTimestamp", "isLive" });
		if (result == "") {
			return cursor;
		}

		if (queryparammethod.equalsIgnoreCase("broadcast")) {
			cursor = (MatrixCursor) processResultForBroadcast(result);
        } else if (queryparammethod.equalsIgnoreCase("search")) {
            cursor =  (MatrixCursor) processResultForList(result, true);
        } else {
            Boolean orderParam = false;
            try {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                Boolean reverse = sharedPref.getBoolean(SettingsActivity.REVERSE_LIST_ORDER, false);

                //reverse is menat by user perspective here, by default its false
                if (reverse == true){
                    orderParam = true;
                }
            } catch (Exception e) {
                // use the default if we cant fetch it
            }
            //Log.e("order Param", orderParam.toString());
			cursor = (MatrixCursor) processResultForList(result, orderParam);
		}

		return (Cursor) cursor;
	}

	private Cursor processResultForBroadcast(String result) {

		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title",
				"subtitle", "image", "extId", "startTime",
				"startTimeAsTimestamp", "isLive", "description", "timeinfo" });

		try {
			// TODO Auto-generated catch block
			JSONArray jsonArray = new JSONArray(result);

			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject json_data = jsonArray.getJSONObject(i);
				// build the Headline
				String t1 = android.text.Html.fromHtml(
						json_data.getString("Title1")).toString();
				String t2 = android.text.Html.fromHtml(
						json_data.getString("Title3")).toString();
				String t3 = android.text.Html.fromHtml(
						json_data.getString("Title2")).toString();


                cursor.addRow(new Object[] { i, t1 , t3,
                            json_data.getString("ImageUrl").toString(),
                            json_data.getString("VId"),
                            json_data.getString("BTimeF").toString(),
                            json_data.getString("BTime").toString(),
                            "true",
                            android.text.Html.fromHtml(json_data.getString("Teasertext").toString()).toString(),
                            android.text.Html.fromHtml(json_data.getString("Title5").toString()).toString()

                    });



			}
		} catch (JSONException e) {
			e.printStackTrace();

		}

        return  (Cursor) cursor;
	}

	private Cursor processResultForList(String result, Boolean doReverse) {
		MatrixCursor cursor = new MatrixCursor(new String[] { "_id", "title",
				"subtitle", "image", "extId", "startTime",
				"startTimeAsTimestamp", "isLive" });
		try {
			JSONArray jsonArray = new JSONArray(result);
			for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json_data;
                if (doReverse == true){
                    json_data = jsonArray.getJSONObject( jsonArray.length()-(i+1) );
                }else{
                    json_data = jsonArray.getJSONObject(i);
                }

				// build the Headline
				String t2 = android.text.Html.fromHtml(
						json_data.getString("Title3")).toString();
				String t3 = android.text.Html.fromHtml(
						json_data.getString("Title2")).toString();

				// Handle grouped views
                Boolean IsGrouped = false;
                try {
                    IsGrouped = json_data.getBoolean("IsGrouped");
                }catch (Exception e){
                    //this value might not exists
                }
				if (IsGrouped) {
					String mtime = json_data.getString("BTime").toString();
					String cliplisturl = "http://m-service.daserste.de/appservice/1.4.2/video/clip/list/"
							+ mtime
							+ "/"
							+ URLEncoder.encode(t3)
							+ "?func=getVideoClipList&clipTimestamp="
							+ mtime
							+ "&clipTitle=" + URLEncoder.encode(t3);
					String result2 = "";
					result2 = readJSONFeed(cliplisturl);
					JSONArray jsonArray2 = new JSONArray(result2);
					
					
					for (int j = 0; j < jsonArray2.length(); j++) {
						JSONObject json_data2 = jsonArray2.getJSONObject(j);
						t2 = android.text.Html.fromHtml(
								json_data2.getString("Title3")).toString();
						t3 = android.text.Html.fromHtml(
								json_data2.getString("Title2")).toString();

						// only add movie if it has a video
						if (android.text.Html.fromHtml(
								json_data2.getString("VId")).toString() != "" ) {
							
							cursor.addRow(new Object[] {
									1000 + j,
									t2,
									t3,
									json_data2.getString("ImageUrl").toString(),
									json_data2.getString("VId"),
									json_data2.getString("BTimeF").toString(),
									json_data2.getString("BTime").toString(),
									json_data2.getString("IsLive") });
							}
						
					}
				}
				Boolean hideLive = false;
				try {
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
					hideLive = sharedPref.getBoolean(SettingsActivity.HIDE_LIVE, false);		
				} catch (Exception e) {
				}

                Boolean IsGrouped2 = false;
                try {
                    IsGrouped2 = json_data.getBoolean("IsGrouped");
                }catch (Exception e){
                    //this value might not exists
                }
                if (!IsGrouped2) {
					if (! json_data.getString("VId").equalsIgnoreCase( "" )) {
						if (json_data.getString("IsLive").toString().equalsIgnoreCase("false") ||  (
								json_data.getString("IsLive").toString().equalsIgnoreCase("true") && hideLive == false )	
								)
						cursor.addRow(new Object[] { i, t2, t3,
								json_data.getString("ImageUrl").toString(),
								json_data.getString("VId"),
								json_data.getString("BTimeF").toString(),
								json_data.getString("BTime").toString(),
								json_data.getString("IsLive") });
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return (Cursor) cursor;
		}
		return (Cursor) cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		return null;
	}

	@Override
	public int delete(Uri uri, String s, String[] strings) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues contentValues, String s,
			String[] strings) {
		return 0;
	}
}