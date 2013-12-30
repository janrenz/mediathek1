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

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.app.LoaderManager;

import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import java.util.ArrayList;
import android.view.Menu;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;
import de.janrenz.app.mediathek.R;

/**
 * Fragment that displays the news headlines for a particular news category.
 * 
 * This Fragment displays a list with the news headlines for a particular news
 * category. When an item is selected, it notifies the configured listener that
 * a headlines was selected.
 */
public class HeadlinesFragment extends org.holoeverywhere.app.ListFragment implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	// The list adapter for the list we are displaying
	SimpleCursorAdapter mListAdapter;
	private Boolean isLoading = true;
	private Cursor myCursor;
	// The listener we are to notify when a headline is selected
	private static  int LOADER_ID = 0x02;
	private ArrayList<Movie> mAllItems = new ArrayList<Movie>();
	private int dateint;
	private Boolean selectMovieAfterLoad = false;
	private static final String STATE_CURRENT_POSITION = "current_scrollpos";
	private Bundle instanceState = null;
	private Boolean isToday = false;
	/**
	 * Default constructor required by framework.
	 */
	public HeadlinesFragment() {
		super();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		BusProvider.getInstance().register(this);
		setListAdapter(mListAdapter);
		getListView().setOnItemClickListener(this);
		setListShown(false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		try {
			if (getListView() != null) {
				// Serialize and persist the activated item position.
				outState.putInt(STATE_CURRENT_POSITION, getListView().getScrollY());
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		super.onSaveInstanceState(outState);
	}

	
	public void reloadAllVisisble() { 
		try {
			triggerLoad(false);
		} catch (Exception e) {

		}
	}
	
	@Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		this.setEmptyText("Keine Einträge gefunden.");
		setListShown(false);
		if (getResources().getBoolean(R.bool.has_two_panes)) {
			this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		}
		//this.instanceState = savedInstanceState;
        triggerLoad(true);
	}
	
	private void triggerLoad(Boolean forceReload ){
		setListShown(false);
		this.isLoading = true;
	    Bundle args = new Bundle();
	    args.putInt("dateint", this.getArguments().getInt("dateint", 0));
	    //note that we need a different loader id for each loader
	    int loaderId = LOADER_ID + this.getArguments().getInt("dateint", 0);
	 
	    //different loader id per day by using the timestamp of the firstmobve!
	    if (forceReload){  	
	    	getActivity().getSupportLoaderManager().restartLoader(loaderId, args, this);
	    }else{
	    	getActivity().getSupportLoaderManager().initLoader(loaderId, args, this);
	    }
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mListAdapter = new RemoteImageCursorAdapter(getActivity(),
				R.layout.headline_item, null,
				new String[] { "title", "image" }, new int[] { R.id.text_view,
						R.id.thumbnail });
		// ListView listView = (ListView) findViewById(R.id.list);
		this.setListAdapter(mListAdapter);	
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		// query code
		Uri queryUri = Uri.parse("content://de.janrenz.app.mediathek.cursorloader.data");
		Integer timestamp =  getArguments().getInt("dateint", 0);
		queryUri = queryUri.buildUpon().appendQueryParameter("timestamp", timestamp.toString()).build();
		//queryUri = queryUri.buildUpon().appendQueryParameter("reload", "1").build();
		try {			
			setListShown(false);
		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
		}
		return new CursorLoader(
				getActivity(),
				queryUri,
				new String[] { "title", "image" , "extId", "startTime", "startTimeAsTimestamp", "isLive"}, 
				null, 
				null, 
				null);
	}
	

	@Override
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
		if ( this.isLoading == false ){
			setListShown(true);
			if (this.getListView().getCount()== 0){
				triggerLoad(false);
			}
		}else{
			
		}
	}
	public void unregisterFromEventBus()
	{
		BusProvider.getInstance().unregister(this);
		return;
	}
	@Override
	public void onPause() {
		super.onPause();
		try {
			BusProvider.getInstance().unregister(this);
		} catch (Exception e) {
			// Es kann sein das wir noch gar nicht registriert sind
		}
	}
	@Subscribe public void updatePressed(UpdatePressedEvent event) {
		//be sure to do BusProvider.getInstance().register(this);
		if ( this.isLoading == false && this.isAdded()){
		    reloadAllVisisble();
		}
		
	}
	@Subscribe public void movieFocused(MovieFocusedEvent event) {
		//be sure to do BusProvider.getInstance().register(this);
		try {
			if (event.dayTimestamp == this.dateint){
				//getListView().setSelector(R.color.abs__bright_foreground_holo_dark);
				if (event.pos <= getListView().getCount()){
					getListView().clearChoices();
					getListView().setItemChecked(event.pos, true);
					getListView().setSelectionFromTop(event.pos, 20);
				}
				
			}
		} catch (Exception e) {
			// TODO: handle exception
			//view might not be created yet
		}
		
	}
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
			if (cursor != null && cursor.getCount()>0){
			mAllItems = new ArrayList<Movie>();
			mListAdapter.swapCursor(cursor);
			myCursor = cursor;
			for(myCursor.moveToFirst(); !myCursor.isAfterLast(); myCursor.moveToNext()) {
				// The Cursor is now set to the right position
				Movie mMovie = new Movie();
				mMovie.setTitle(myCursor.getString(myCursor.getColumnIndexOrThrow("title")));
				mMovie.setSubtitle(myCursor.getString(myCursor.getColumnIndexOrThrow("subtitle")));
				mMovie.setExtId(myCursor.getString(myCursor.getColumnIndexOrThrow("extId")));
				mMovie.setStarttime(myCursor.getString(myCursor.getColumnIndexOrThrow("startTime")));
				mMovie.setStarttimestamp(myCursor.getInt(myCursor.getColumnIndexOrThrow("startTimeAsTimestamp")));
				mMovie.setIsLive(myCursor.getString(myCursor.getColumnIndexOrThrow("isLive")));
				
				mAllItems.add(mMovie);
			}	
		}
		try {
			setListShown(true);		
			if (  this.instanceState != null && this.instanceState.containsKey(STATE_CURRENT_POSITION) && this.instanceState.getInt(STATE_CURRENT_POSITION) > 0 ) {
				getListView().scrollTo(0, this.instanceState.getInt(STATE_CURRENT_POSITION));	
				//
			} 
		} catch (Exception e) {
			// TODO: handle exception
		}
		this.isLoading = false;
		if (isAdded() && getResources().getBoolean(R.bool.has_two_panes) && this.selectMovieAfterLoad) {
			if ( this.getListView().getCount() > 0 ){
				 BusProvider.getInstance().post(new MovieSelectedEvent(0, mAllItems.get(0).getExtId(), getArguments().getInt("dateint", 0), mAllItems));
				this.getListView().setSelection(0);
				//only do this once
				this.selectMovieAfterLoad = false;
			}
		}
		BusProvider.getInstance().register(this);
		
	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		//setListShown(true);
		if (myCursor != null) {
			mListAdapter.swapCursor(null);
			myCursor = null;
		}
	}

	 
	
	/**
	 * Handles a click on a headline.
	 * 
	 * This causes the configured listener to be notified that a headline was
	 * selected.
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
			myCursor.moveToPosition(position);
			String cExtId = myCursor.getString(myCursor.getColumnIndexOrThrow("extId"));
			if (getResources().getBoolean(R.bool.has_two_panes)) {
	            // display it on the article fragment
				try {
					BusProvider.getInstance().post(new MovieSelectedEvent(position, cExtId, getArguments().getInt("dateint", 0), mAllItems));					
				} catch (Exception e) {
					// TODO: handle exception
				}
				 //this.getListView().setSelection(position);
			
	        }
	        else {
	            // use separate activity
	            Intent i = new Intent(getActivity(), ArticleActivity.class);
	            i.putExtra("pos", position );
	            i.putExtra("movies",mAllItems );
	            startActivity(i);
	        }
		
	}

	public int getDateint() {
		return dateint;
	}

	public void setDateint(int dateint) {
		this.dateint = dateint;
	}

	public Boolean getSelectMovieAfterLoad() {
		return selectMovieAfterLoad;
	}

	public void setSelectMovieAfterLoad(Boolean selectMovieAfterLoad) {
		this.selectMovieAfterLoad = selectMovieAfterLoad;
	}

	public Boolean getIsToday() {
		return isToday;
	}

	public void setIsToday(Boolean isToday) {
		this.isToday = isToday;
	}
}
