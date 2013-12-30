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


import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.view.Menu;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.support.v7.widget.SearchView;

import org.holoeverywhere.app.AlertDialog;

import de.cketti.library.changelog.ChangeLog;



/**
 * Main activity: shows headlines list and articles, if layout permits.
 * 
 * This is the main activity of the application. It can have several different
 * layouts depending on the SDK version, screen size and orientation. The
 * configurations are divided in two large groups: single-pane layouts and
 * dual-pane layouts.
 * 
 * In single-pane mode, this activity shows a list of headlines using a
 * {@link HeadlinesFragment}. When the user clicks on a headline, a separate
 * activity (a {@link ArticleActivity}) is launched to show the news article.
 * 
 * In dual-pane mode, this activity shows a {@HeadlinesFragment
 * } on the left side and an {@ArticleFragment
 * } on the right side. When the user selects a headline on the
 * left, the corresponding article is shown on the right.
 */
public class MediathekActivity extends org.holoeverywhere.app.Activity implements SearchView.OnQueryTextListener {

	// Whether or not we are in dual-pane mode
	boolean mIsDualPane = false;

	// The fragment where the headlines are displayed
	HeadlinesFragment mHeadlinesFragment;

	// The fragment where the article is displayed (null if absent)
	ArticlePagerFragment mArticleFragment;

	// The news category and article index currently being displayed
	int mArtIndex = 0;

	// List of category titles

    ViewPager mPager;
	ChangeLog cl;

    @Override
    protected Holo onCreateConfig(Bundle savedInstanceState) {
        Holo config = super.onCreateConfig(savedInstanceState);
        config.ignoreThemeCheck = true;
        return config;
    }

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);

		// find our fragments
		mHeadlinesFragment = (HeadlinesFragment) getSupportFragmentManager()
				.findFragmentById(R.id.pager);
		mArticleFragment = (ArticlePagerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.article);

		View articleView = findViewById(R.id.article);
		mIsDualPane = articleView != null
				&& articleView.getVisibility() == View.VISIBLE;

		
		cl = new ChangeLog(this);
		//only show changelog_master if there are breaking changes
		//if (cl.isFirstRun()) {
		//    cl.getLogDialog().show();
		//}

	}

	/** Restore category/article selection from saved state. */
	void restoreSelection(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (mIsDualPane) {
				// int artIndex = savedInstanceState.getInt("artIndex", 0);
				
				// !TODO handle this
				// onHeadlineSelected(artIndex, null, null);
			}
		}
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
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		restoreSelection(savedInstanceState);
	}

	/**
	 * Sets up Action Bar (if present).
	 * 
	 * @param showTabs
	 *            whether to show tabs (if false, will show list).
	 * @param selTab
	 *            the selected tab or list item.
	 */
	public void setUpActionBar(boolean showTabs, int selTab) {
		return;
	}
    private SearchView searchView = null;

    Menu mMenu = null;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Suche in Mediathek…");
        searchView.setOnQueryTextListener(this);
		mMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	  protected AlertDialog getInfoDialog() {
	        TextView tv = new TextView(this);
	        //tv.setBackgroundColor(getResources().getColor(R.color.abs__bright_foreground_holo_dark)); 
	        tv.setPadding(15, 15, 15, 15);
	        tv.setMovementMethod(new ScrollingMovementMethod());
	        tv.setScrollBarStyle(1);
	        tv.setText(Html.fromHtml(getString(R.string.infotext)));
	        AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        
	        builder.setTitle("Mediathek 1")         
	                .setView(tv)
	                .setInverseBackgroundForced(true)//needed for old android version
	                .setCancelable(false)
	                // OK button
	                .setPositiveButton(
	                        this.getResources().getString(R.string.changelog_ok_button),
	                        new DialogInterface.OnClickListener() {
	                            @Override
	                            public void onClick(DialogInterface dialog, int which) {
	                               dialog.dismiss();
	                            }
	                        });
            // Show "More…" button if we're only displaying a partial change log.
            builder.setNegativeButton(R.string.info_popup_changelog,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                    		   if (cl != null ) cl.getFullLogDialog().show();	
                        }
                    });
	        

	        return builder.create();
	    }
	  
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			// custom dialog	
			final AlertDialog dialog = this.getInfoDialog();
			// set the custom dialog components - text, image and button
			dialog.show();
			return true;
		case R.id.action_refresh:
			//send update event to everyone who cares
			BusProvider.getInstance().post(new UpdatePressedEvent());
			return true;
		case R.id.action_settings:
			
			  Intent i = new Intent(this, SettingsActivity.class);
	            //i.putExtra("pos", position );
	            startActivity(i);
			return true;
		case R.id.action_share:
			BusProvider.getInstance().post(new ShareActionSelectedEvent());	
			return true;
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this, MediathekActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	/** Save instance state. Saves current category/article index. */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("artIndex", mArtIndex);
		try {
			super.onSaveInstanceState(outState);
		} catch (Exception e) {
			// TODO: handle exception
			//may throw java.lang.IllegalStateException
		}
	}

    /** if a user enters some text in the search field in the action bar **/
    @Override
    public boolean onQueryTextSubmit(String query) {

        //lets close the input field
        if (mMenu != null){
            MenuItem searchMenuItem = mMenu.findItem(R.id.action_search);
            searchMenuItem.collapseActionView();
            // lets open up the search intent
            Intent i = new Intent(this, SearchActivity.class);
            i.setAction(Intent.ACTION_SEARCH);
            //add search string to our search activity
            i.putExtra(SearchManager.QUERY, query );
            startActivity(i);
        }

        return true;
    }
    @Override
    public boolean onQueryTextChange(String query) {
        //kind of a hack to close the action view if a users clicks on the x,
        if (query.equalsIgnoreCase("")){
            MenuItem searchMenuItem = mMenu.findItem(R.id.action_search);

        }
        return true;


    }
}
