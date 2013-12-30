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

import java.util.Date;

import android.util.DisplayMetrics;
import android.util.Log;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class RemoteImageCursorAdapter  extends SimpleCursorAdapter implements Filterable {
		 
	    private Context context;
	    private int layout;

	    private LayoutInflater mLayoutInflater;
	    
	    public RemoteImageCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to) {
	        super(context, layout, c, from, to);
	        this.context = context;
	        this.layout = layout;
	        this.mLayoutInflater = LayoutInflater.from(context);


	    }
	 
	    @Override
	    public View newView(Context context, Cursor cursor, ViewGroup parent) {

	    	  View v = mLayoutInflater.inflate(this.layout, parent, false);

	          return v;
	    }
	    
	 
	    
	    @SuppressWarnings("deprecation")
		@Override
	    public void bindView(View v, Context context, Cursor c) {
	    	
	    	  String title = c.getString(c.getColumnIndexOrThrow("title"));
	    	  String subtitle = c.getString(c.getColumnIndexOrThrow("subtitle"));
	    	  String imagePath = c.getString(c.getColumnIndexOrThrow("image"));
	    	  String startTime = c.getString(c.getColumnIndexOrThrow("startTime"));
	    	  String startTimeAsTimestamp = c.getString(c.getColumnIndex("startTimeAsTimestamp"));
	    	  String isLive = c.getString(c.getColumnIndex("isLive"));
            if (this.layout == R.layout.headline_item_grid){
            final View vl = v;

            v.findViewById(R.id.thumbnail).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {

                    try {

                        View imgView = vl.findViewById(R.id.thumbnail);
                        imgView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        ViewGroup.LayoutParams layout = imgView.getLayoutParams();
                        layout.height = imgView.getWidth()/16*9;
                        imgView.setLayoutParams(layout);
                    }catch (Exception e){}
                }
            });
            }

            /**
             * Next set the text of the entry.
             */
            if (isLive.equalsIgnoreCase("true")){
                v.findViewById(R.id.live).setVisibility(View.VISIBLE);
                //v.setBackgroundColor(   context.getResources().getColor(R.color.highlight_live_list));
	          }else{
	        	  v.findViewById(R.id.live).setVisibility(View.GONE);
	        	  //v.setBackgroundColor(   context.getResources().getColor(R.color.list_background));
	          }
	          
	          TextView title_text = (TextView) v.findViewById(R.id.text_view);
	          if (title_text != null) {
	              title_text.setText(title);
	          }
	          
	          TextView subtitle_text = (TextView) v.findViewById(R.id.text_view_sub);
	          if (subtitle_text != null) {
	        	  subtitle_text.setText(subtitle);
	          }
	          TextView subtitle2_text = (TextView) v.findViewById(R.id.text_view_sub2);
	          if (subtitle2_text != null) {
	        	  Date dt = new Date();
	      		// z.B. 'Fri Jan 26 19:03:56 GMT+01:00 2001'
	        	  dt.setTime(Integer.parseInt(startTimeAsTimestamp)*1000);
	      		dt.setHours(0);
	      		dt.setMinutes(0);
	      		dt.setSeconds(0);
	        	subtitle2_text.setText("ARD > " + startTime + " Uhr");
	          }
	          /**
	           * Set the image
	           */
	          DisplayImageOptions loadingOptions = new DisplayImageOptions.Builder()
	          ///.showStubImage(R.drawable.abs__item_background_holo_light)
              //.imageScaleType(ImageScaleType.EXACTLY)
	          // .showImageForEmptyUri(R.drawable.ic_empty)
                    //  .memoryCache(new WeakMemoryCache())
                      .cacheInMemory()
	          //.cacheOnDisc()
	          .build();
	          ImageView image_view =  (ImageView) v.findViewById(R.id.thumbnail);

	          if (image_view != null) {

                  if (this.layout == R.layout.headline_item_grid){
                      imagePath = imagePath + "/" + 320;

                  }else{

                      imagePath = imagePath + "/" + 150;
                  }
                  ImageLoader.getInstance().displayImage(imagePath, image_view, loadingOptions);
	          }
	    }
	
}
