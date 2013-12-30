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

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the news headlines for a particular news category.
 * 
 * This Fragment displays a list with the news headlines for a particular news
 * category. When an item is selected, it notifies the configured listener that
 * a headlines was selected.
 */
public class ListPageFragment extends org.holoeverywhere.app.ListFragment implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	// The list of headlines that we are displaying
	List<String> mHeadlinesList = new ArrayList<String>();

	// The list adapter for the list we are displaying
	SimpleCursorAdapter mListAdapter;

	private Cursor myCursor;
	

	// The listener we are to notify when a headline is selected
	OnHeadlineSelectedListener mHeadlineSelectedListener = null;
	private static final int LOADER_ID = 0x02;

	/**
	 * Represents a listener that will be notified of headline selections.
	 */
	public interface OnHeadlineSelectedListener {
		/**
		 * Called when a given headline is selected.
		 * 
		 * @param index
		 *            the index of the selected headline.
		 * @param string
		 */
		public void onHeadlineSelected(int index, String string, ArrayList<?> all,
				String title, String subtitle);
	}

	/**
	 * Default constructor required by framework.
	 */
	public ListPageFragment() {
		super();
	}

	@Override
	public void onStart() {
		super.onStart();
		setListShown(false);
		BusProvider.getInstance().register(this);
		setListAdapter(mListAdapter);
		getListView().setOnItemClickListener(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// mListAdapter = new ArrayAdapter<String>(getActivity(),
		// R.layout.headline_item,
		// mHeadlinesList);
		mListAdapter = new RemoteImageCursorAdapter(getActivity(),
				R.layout.headline_item, null,
				new String[] { "title", "image", }, new int[] { R.id.text_view,
						R.id.thumbnail });
		// ListView listView = (ListView) findViewById(R.id.list);
		this.setListAdapter(mListAdapter);
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null,
				this);
		BusProvider.getInstance().register(this);
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(
				getActivity(),
				Uri.parse("content://de.janrenz.app.mediathek.cursorloader.data"),
				new String[] { "title", "image", "extId" }, null, null, null);
	}

	/**
	 * Sets the listener that should be notified of headline selection events.
	 * 
	 * @param listener
	 *            the listener to notify.
	 */
	public void setOnHeadlineSelectedListener(
			OnHeadlineSelectedListener listener) {
		mHeadlineSelectedListener = listener;
	}

	@Override
	public void onResume() {
		
		setListShown(false);
		getActivity().getSupportLoaderManager().restartLoader(LOADER_ID +  (int) Math.random()*1000, null,
				this);
		BusProvider.getInstance().register(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}

	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		
		setListShown(true);
		mListAdapter.changeCursor(cursor);
		//mListAdapter.swapCursor(cursor);
		//myCursor = cursor;

	}

	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		setListShown(true);
		
	//mListAdapter.swapCursor(null);
		//myCursor = null;
	}
	
	@Subscribe
	public void onMovieSelected(MovieFocusedEvent event) {
		if (mListAdapter != null && getArguments().getInt("dateint", 0) == event.dayTimestamp) {
			getListView().setSelection(event.pos);
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
		if (null != mHeadlineSelectedListener) {
			myCursor.moveToPosition(position);
			mHeadlineSelectedListener
					.onHeadlineSelected(position, myCursor.getString(myCursor
							.getColumnIndexOrThrow("extId")), null,
							myCursor.getString(myCursor
									.getColumnIndexOrThrow("title")), myCursor
									.getString(myCursor
											.getColumnIndexOrThrow("subtitle")));
		}
	}

	

	
}
