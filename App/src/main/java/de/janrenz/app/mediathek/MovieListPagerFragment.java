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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;
import com.viewpagerindicator.TitlePageIndicator;

import org.holoeverywhere.LayoutInflater;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieListPagerFragment extends org.holoeverywhere.app.Fragment {

    // The list adapter for the list we are displaying

    MyAdapter mAdapter;
    ViewPager mPager;
    public static final int DISPLAYEDPAGES = 8;
    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_CURRENT_POSITION = "current_position";

    /**
     * Default constructor required by framework.
     */
    public MovieListPagerFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    @Subscribe
    public void updatePressed(UpdatePressedEvent event) {
        // be sure to do BusProvider.getInstance().register(this);

        if (mAdapter != null && mPager != null) {
            mAdapter = new MyAdapter(getFragmentManager());
            mAdapter.notifyDataSetChanged();
            mPager = (ViewPager) getActivity().findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);
            mPager.setCurrentItem(DISPLAYEDPAGES - 1);
        } else {

        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new MyAdapter(getFragmentManager());
        mPager = (ViewPager) getActivity().findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        // Set the pager with an adapter
        // Bind the title indicator to the adapter
        TitlePageIndicator titleIndicator = (TitlePageIndicator) getActivity()
                .findViewById(R.id.movielistpageindicator);
        titleIndicator.setViewPager(mPager);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_POSITION)) {
            mPager.setCurrentItem(savedInstanceState.getInt(STATE_CURRENT_POSITION));
            //
        } else {
            mPager.setCurrentItem(DISPLAYEDPAGES-1);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPager != null) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_CURRENT_POSITION, mPager.getCurrentItem());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.movielist, container, false);
        return v;
    }

    public static class MyAdapter extends FragmentStatePagerAdapter {
        private int mcount = DISPLAYEDPAGES;
        private Boolean triggeredLoadOnce = false;

        public MyAdapter(FragmentManager fm) {
            super(fm);

        }

        public void setCount(int newCount) {
            mcount = newCount;

        }

        public String getPageTitle(int position) {
            if (position == (mcount - 1)) {
                return this.getDateString(position) + " (heute)";
            } else {
                return this.getDateString(position);
            }

        }

        @SuppressLint("SimpleDateFormat")
        @SuppressWarnings("deprecation")
        private String getDateString(int position) {
            Date dt = new Date();
            // z.B. 'Fri Jan 26 19:03:56 GMT+01:00 2001'
            dt.setHours(0);
            dt.setMinutes(0);
            dt.setSeconds(0);
            Long fragmentTime = dt.getTime()
                    - ((24 * 60 * 60 * 1000) * (mcount - position - 1));
            new Date(fragmentTime);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE dd.MM");
            return simpleDateFormat.format(fragmentTime);
        }

        @Override
        public int getCount() {
            return mcount;
        }

        @SuppressWarnings("deprecation")
        @Override
        public Fragment getItem(int position) {
            HeadlinesFragment f = new HeadlinesFragment();
            Bundle args = new Bundle();
            int offset = DISPLAYEDPAGES - 1 - position;
            if (offset == 0 && triggeredLoadOnce == false) {
                //the last item selects
                f.setSelectMovieAfterLoad(true);
                triggeredLoadOnce = true;
            }
            if (offset == 0) {
                f.setIsToday(true);
            }
            // this is now the offset
            Date dt = new Date();
            dt.setHours(0);
            dt.setMinutes(0);
            dt.setSeconds(0);
            Long curtime = dt.getTime() / 1000 - ((24 * 60 * 60) * offset);
            int datestamp = (int) (curtime * 1);
            args.putInt("dateint", datestamp);
            args.putInt("initialOffset", offset);
            f.setArguments(args);
            f.setDateint(datestamp);

            return f;
        }

        // http://stackoverflow.com/questions/10849552/android-viewpager-cant-update-dynamically
        public int getItemPosition(Object item) {
            HeadlinesFragment fragment = (HeadlinesFragment) item;
            if (fragment.getIsToday() == true) {
                return POSITION_NONE;

            } else {
                return super.getItemPosition(item);
            }

        }
    }

}
