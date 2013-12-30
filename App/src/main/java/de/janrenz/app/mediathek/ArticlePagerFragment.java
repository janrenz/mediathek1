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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Menu;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;
import com.viewpagerindicator.*;

import org.holoeverywhere.LayoutInflater;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Fragment that displays a news article.
 */
public class ArticlePagerFragment extends org.holoeverywhere.app.Fragment implements OnPageChangeListener, OnTouchListener{

	MyAdapter mAdapter;
	ViewPager mPager;
	int dayTimestamp;
	Boolean sendFocusEvent = false;
	// Parameterless constructor is needed by framework
	public ArticlePagerFragment() {
		super();
	}

	public static ArticlePagerFragment newInstance(int pos,
			ArrayList<Movie> allItems) {
		ArticlePagerFragment f = new ArticlePagerFragment();
		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putInt("pos", pos);
		args.putParcelableArrayList("movies", allItems);
		f.setArguments(args);
		return f;
	}
       
	@Override
	public void onResume() {
		try {
			super.onResume();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		BusProvider.getInstance().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}

	//events are used if we are running in table mode
	@Subscribe
	public void onMovieSelected(MovieSelectedEvent event) {
		this.sendFocusEvent = false;
		this.dayTimestamp = event.dayTimestamp;
		if (mAdapter != null) {
			mAdapter.setAllItems(event.mList);
			
			mAdapter.notifyDataSetChanged();
			mPager.setCurrentItem(event.pos);

			CirclePageIndicator titleIndicator = (CirclePageIndicator) getActivity()
					.findViewById(R.id.detailpageindicator);
            mPager = (ViewPager) getActivity().findViewById(R.id.singlepager);
            titleIndicator.setViewPager(mPager);
			titleIndicator.setCurrentItem(event.pos);
		}
	
		if (mPager != null) {
			mPager.setOnPageChangeListener(this);
			mPager.setOnTouchListener(this);
		}else{
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new MyAdapter(getFragmentManager());
		mPager = (ViewPager) getActivity().findViewById(R.id.singlepager);
		mPager.setAdapter(mAdapter);
		try {
			if (mAdapter != null && getArguments().getParcelableArrayList("movies") != null) {
				ArrayList<Movie> movies = getArguments().getParcelableArrayList("movies");
				mAdapter.setCount(movies.size());
				mAdapter.setAllItems(movies);
				mPager = (ViewPager) getActivity().findViewById( R.id.singlepager );
				mPager.setCurrentItem( getArguments().getInt("pos", 0) );
				// Set the pager with an adapter
				// Bind the title indicator to the adapter
                CirclePageIndicator titleIndicator = (CirclePageIndicator) getActivity()
						.findViewById(R.id.detailpageindicator);
				titleIndicator.setViewPager((ViewPager) getActivity().findViewById( R.id.singlepager ));
				titleIndicator.setCurrentItem(getArguments().getInt("pos", 0));
			}

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("ArticelPager", "mAdapter is null");
		}
		
		
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		
	}
	@Override
	// mPager.setCurrentItem(0);
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// check if there is an activity with all the data we need
		View v = inflater.inflate(R.layout.detailpager, container, false);
		return v;
	}

	public static class MyAdapter extends FragmentStatePagerAdapter {
		private int mcount = 0;
		private ArrayList<Movie> mallItems;
		private ArrayList<String> extIds;
		private SparseArray<WeakReference<ArticleFragment>> mPageReferenceMap = new SparseArray<WeakReference<ArticleFragment>>();

		public MyAdapter(FragmentManager fm) {
			super(fm);
		}

		public void setCount(int newCount) {
			mcount = newCount;
		}

		public void setAllItems(ArrayList<Movie> allItems) {
			mallItems = allItems;
			extIds = new ArrayList<String>();
			for (Movie movie : allItems) {
				extIds.add(movie.getExtId());
			}
			this.setCount(allItems.size());
			this.notifyDataSetChanged();
		}

		public String getPageTitle(int position) {
			return "Titel" + position;
		}

		@Override
		public int getCount() {
			return mcount;
		}

		@Override
		public Fragment getItem(int position) {
			
			//check if isLiveItem
		
			Bundle args = new Bundle();
			args.putInt("num", position);
			args.putString("extId", mallItems.get(position).getExtId());
			args.putString("title", mallItems.get(position).getTitle());
			args.putString("subtitle", mallItems.get(position).getSubtitle());
			args.putString("senderinfo", mallItems.get(position).getSenderinfo());
			if (mallItems.get(position).getIsLive() == true){
				LiveFragment f = new LiveFragment();
				ArticleFragment fcast = (ArticleFragment)  f;
				mPageReferenceMap.put(Integer.valueOf(position), new WeakReference<ArticleFragment>(fcast));
				f.setArguments(args);
				// we need to have this accessible from the outside as well
				f.setExtId(mallItems.get(position).getExtId());
				return f;
			}else{
				ArticleFragment f = new ArticleFragment();	
				
				mPageReferenceMap.put(Integer.valueOf(position), new WeakReference<ArticleFragment>(f));
				f.setArguments(args);
				// we need to have this accessible from the outside as well
				f.setExtId(mallItems.get(position).getExtId());
				return f;
			}
			
			
		}

		// http://stackoverflow.com/questions/10849552/android-viewpager-cant-update-dynamically
		public int getItemPosition(Object item) {
			ArticleFragment fragment = (ArticleFragment) item;
			String title = fragment.getExtId();
			int position = extIds.indexOf(title);

			if (position >= 0) {
				return position;
			} else {
				return POSITION_NONE;
			}
		}
		public ArticleFragment getFragment(int key) {
			WeakReference<ArticleFragment> weakReference = mPageReferenceMap.get(key);

			if (null != weakReference) {

				return (ArticleFragment) weakReference.get();
			}
			else {

				return null;
			}
		
		}
		
		@Override
		public void destroyItem(View container, int position, Object object) {
		try {
			
			super.destroyItem(container, position, object);
		} catch (Exception e) {
			// TODO: handle exception
		}
			mPageReferenceMap.remove(Integer.valueOf(position));
		}
	}
	@Subscribe
	public void onShareActionSelected(ShareActionSelectedEvent event) {
		//only if we are the current displayed fragment
		//this
		try {
			int currentItem = this.mPager.getCurrentItem();
			MyAdapter adapter = ((MyAdapter)mPager.getAdapter());
			ArticleFragment fragment = (ArticleFragment) adapter.getFragment(currentItem);
			fragment.shareMovieUrl();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	
	}
	
	@Override
	public void onPageScrollStateChanged(int pos) {
		
	}

	@Override
	public void onPageScrolled(int pos, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int pos) {
		if (this.sendFocusEvent){
			BusProvider.getInstance().post(new MovieFocusedEvent(pos,this.dayTimestamp ));			
		}
		CirclePageIndicator titleIndicator = (CirclePageIndicator) getActivity()
				.findViewById(R.id.detailpageindicator);
		titleIndicator.setCurrentItem(pos);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		this.sendFocusEvent = true;
		return false;
	}

}
