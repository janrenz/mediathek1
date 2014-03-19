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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

/**
 * Activity that displays a particular news article onscreen.
 * 
 * This activity is started only when the screen is not large enough for a
 * two-pane layout, in which case this separate activity is shown in order to
 * display the news article. This activity kills itself if the display is
 * reconfigured into a shape that allows a two-pane layout, since in that case
 * the news article will be displayed by the {@link MediathekActivity} and this
 * Activity therefore becomes unnecessary.
 */
public class ArticleActivity extends org.holoeverywhere.app.Activity {
	private static final int MENUSHAREID = 1;
	// The news category index and the article index for the article we are to
	// display
	int mCatIndex, mArtIndex;
	// the external id
	String extId;
	ArrayList<Movie> allItems;

	ViewPager mPager;

	/**
	 * Sets up the activity.
	 * 
	 * Setting up the activity means reading the category/article index from the
	 * Intent that fired this Activity and loading it onto the UI. We also
	 * detect if there has been a screen configuration change (in particular, a
	 * rotation) that makes this activity unnecessary, in which case we do the
	 * honorable thing and get out of the way.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BusProvider.getInstance().register(this);
		setContentView(R.layout.detailactivity);
		mCatIndex = getIntent().getExtras().getInt("catIndex", 0);
		mArtIndex = getIntent().getExtras().getInt("artIndex", 0);
		extId = getIntent().getExtras().getString("extId");
		allItems = getIntent().getExtras().getParcelableArrayList("allItems");

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (findViewById(R.id.detail_fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}
			// During initial setup, plug in the details fragment.
			Fragment details = new ArticlePagerFragment();
			Bundle args = getIntent().getExtras();
			details.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.detail_fragment_container, details).commit();
			// getFragmentManager().beginTransaction().add(android.R.id.content,
			// details).commit();
		}
       // mMediaRouter = MediaRouter.getInstance(getApplicationContext());
		setContentView(R.layout.detailactivity);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpTo(this, new Intent(this,
                        MediathekActivity.class));
                return true;

            case R.id.action_share:
                BusProvider.getInstance().post(new ShareActionSelectedEvent());
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
	
	@Override
	public void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}
}