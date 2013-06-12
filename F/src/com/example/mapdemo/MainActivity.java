/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.example.mapdemo;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The main activity of the API library demo gallery.
 * <p/>
 * The main layout lists the demonstrated features, with buttons to launch them.
 */
public final class MainActivity extends FragmentActivity
        implements GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private String accountName;
    private Location location = new Location("");
    private boolean locationAdjusted = false;

    {
        location.setLatitude(20);
        location.setLongitude(20);
        location.setAccuracy(20);
    }

    private LocationClient lc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_demo);

        Intent intent = AccountPicker.newChooseAccountIntent(null, null, null,
                false, null, null, null, null);
        startActivityForResult(intent, 1);

        lc = new LocationClient(this, new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                lc.requestLocationUpdates(locationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(final Location location) {
                        MainActivity.this.location = location;
                        if (!locationAdjusted) {
                            LatLngBounds bounds = new LatLngBounds.Builder()
                                    .include(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .build();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                            locationAdjusted = true;
                        }
                        takeCareOfLocation();
                    }
                });
            }

            @Override
            public void onDisconnected() {

            }
        }, new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {

            }
        }
        );
        lc.connect();

        setUpMapIfNeeded();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            takeCareOfLocation();
        }
    }

    private void takeCareOfLocation() {
        new Thread(new Runnable() {         // Do rest of this task
            @Override
            public void run() {
                try {
                    String token = GoogleAuthUtil.getToken(MainActivity.this, accountName, "ah");
                    System.err.println(token);
                    SchemeRegistry scheme = new SchemeRegistry();
                    scheme.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
                    final DefaultHttpClient httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(new BasicHttpParams(), scheme), null);
                    HttpGet httpGet = new HttpGet("https://gzegzolpn.appspot.com/_ah/login?continue=https://gzegzolpn.appspot.com/&auth=" + token);
                    httpClient.execute(httpGet);
                    httpGet = new HttpGet("https://gzegzolpn.appspot.com/?latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude() + "&radius=" + location.getAccuracy());
                    final HttpResponse httpResponse = httpClient.execute(httpGet);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                addMarkersToMap(EntityUtils.toString(httpResponse.getEntity()));

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UserRecoverableAuthException e) {
                    startActivityForResult(e.getIntent(), 1);
                } catch (GoogleAuthException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add lots of markers to the map.
        addMarkersToMap("");

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);

        // Pan to see all markers in view.
        // Cannot zoom to bounds until the map has a size.
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView.getViewTreeObserver().isAlive()) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation") // We use the new method when supported
                @SuppressLint("NewApi") // We check which build version we are using.
                @Override
                public void onGlobalLayout() {
                    LatLngBounds bounds = new LatLngBounds.Builder()
                            .include(new LatLng(location.getLatitude(), location.getLongitude()))
                            .build();

                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(5));
                }
            });
        }
    }

    private void addMarkersToMap(String xml) {

        // Creates a marker rainbow demonstrating how to create default marker icons of different
        // hues (colors).
        mMap.clear();
        Pattern pattern = Pattern.compile("(?s)<email>(.*?)</email>.*?<latitude>(.*?)</latitude>.*?<longitude>(.*?)</longitude>.*?<radius>(.*?)</radius>");
        Matcher matcher = pattern.matcher(xml);

        while (matcher.find()) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(matcher.group(2)),
                            Double.parseDouble(matcher.group(3))))
                    .title(matcher.group(1))
            );
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        try {
            final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("plain/text");
            String recipient = marker.getTitle();
            String subject = "";
            String message = "";
            if (recipient != null) emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{recipient});
            if (subject != null) emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
            if (message != null) emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);

            startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        } catch (ActivityNotFoundException e) {
            // cannot send email for some reason
        }

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
}

