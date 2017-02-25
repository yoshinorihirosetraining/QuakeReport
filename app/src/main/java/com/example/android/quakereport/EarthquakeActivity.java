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
package com.example.android.quakereport;

import android.app.Activity;
import android.content.Context;
import android.media.audiofx.Equalizer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class EarthquakeActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    public static final String LOG_TAG = EarthquakeActivity.class.getName();

    public static final String REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&eventtype=earthquake&orderby=time&minmag=6&limit=10";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);
        ListView listView = (ListView) findViewById(R.id.list);
        TextView emptyText = (TextView) findViewById(R.id.empty);
        listView.setEmptyView(emptyText);

        //Task task = new Task();
        //task.execute(REQUEST_URL);

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null
                               && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            Log.v("EarthquakeActivity", "initLoader()");
            getSupportLoaderManager().initLoader(0, null, this);
        } else {
            ((ProgressBar)findViewById(R.id.loading_indicator)).setVisibility(GONE);
            emptyText.setText("No internet connection.");
        }
    }

    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        Log.v("EarthquakeActivity", "onCreateLoader()");
        return new EarthquakeLoader(this, REQUEST_URL);
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        Log.v("EarthquakeActivity", "onLoadFinished()");

        ((ProgressBar)findViewById(R.id.loading_indicator)).setVisibility(GONE);
        ((TextView)findViewById(R.id.empty)).setText("No earhquakes found.");

        if (earthquakes == null) return;

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new {@link ArrayAdapter} of earthquakes
        Earthquake.Adapter adapter = new Earthquake.Adapter(
                (Activity)this,
                (ArrayList<Earthquake>) earthquakes);

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        Log.v("EarthquakeActivity", "onLoaderReset()");
    }

    public static class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>> {

        String url = null;

        public EarthquakeLoader(Context context, String url) {
            super(context);
            this.url = url;
        }

        @Override
        protected void onStartLoading() {
            Log.v("EarthquakeLoader", "onStartLoading()");
            forceLoad();
        }

        @Override
        public List<Earthquake> loadInBackground() {
            Log.v("EarthquakeLoader", "loadInBackground()");
            List<Earthquake> result = null;
            URL url = null;
            HttpURLConnection urlConnection = null;

            /* for debug
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                ;
            }
            */

            try {
                url = new URL(this.url);
            } catch (MalformedURLException e) {
                Log.e("EarthquakeActivity", "MalformedURLException");
                return null;
            }

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Log.e("EarthquakeActivity", "IOException");
                return null;
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder b = new StringBuilder();
                String str = null;
                while ((str = reader.readLine()) != null) { b.append(str); }
                result = QueryUtils.extractEarthquakes(b.toString());
            } catch (IOException e) {
                Log.e("EarthquakeActivity", "IOException");
            } finally {
                urlConnection.disconnect();
            }
            return result;
        }
    }

    private class Task extends AsyncTask<String, Void, List<Earthquake>> {
        protected List<Earthquake> doInBackground(String... urls) {
            List<Earthquake> result = null;
            URL url = null;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
            } catch (MalformedURLException e) {
                Log.e("EarthquakeActivity", "MalformedURLException");
                return null;
            }

            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Log.e("EarthquakeActivity", "IOException");
                return null;
            }

            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder b = new StringBuilder();
                String str = null;
                while ((str = reader.readLine()) != null) { b.append(str); }
                result = QueryUtils.extractEarthquakes(b.toString());
            } catch (IOException e) {
                Log.e("EarthquakeActivity", "IOException");
            } finally {
                urlConnection.disconnect();
            }
            return result;
        }
        protected void onPostExecute(List<Earthquake> lst) {
            if (lst == null) return;

            // Find a reference to the {@link ListView} in the layout
            ListView earthquakeListView = (ListView) findViewById(R.id.list);

            // Create a new {@link ArrayAdapter} of earthquakes
            Earthquake.Adapter adapter = new Earthquake.Adapter(
                    (Activity)EarthquakeActivity.this,
                    (ArrayList<Earthquake>) lst);

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            earthquakeListView.setAdapter(adapter);
        }
    }
}
