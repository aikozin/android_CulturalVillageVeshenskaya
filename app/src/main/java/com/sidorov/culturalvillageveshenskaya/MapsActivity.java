package com.sidorov.culturalvillageveshenskaya;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sidorov.culturalvillageveshenskaya.Other.ProgressIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap mMap;
    LatLng coordVeshenskaya = new LatLng(49.6330465, 41.7156983);
    View map;
    View logo;
    FrameLayout shortAboutFragment;
    public ProgressIndicator progressIndicator;

    boolean isAboutOpen = false;
    int heightMap = 0, heightMapWithAboutWindowOpen = 0;

    public Fragment fragment;

    ImageButton openGMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        logo = findViewById(R.id.logo);
        logo.setVisibility(View.VISIBLE);
        shortAboutFragment = findViewById(R.id.shortAboutFragment);
        map = findViewById(R.id.map);
        openGMaps = findViewById(R.id.openGMaps);

        progressIndicator = new ProgressIndicator(findViewById(R.id.progressIndicator), 1000);
        progressIndicator.startAnimation();
    }

    @Override
    public void onBackPressed() {
        if (isAboutOpen) {
            showInfoWindow(View.INVISIBLE);
        } else
            super.onBackPressed();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(coordVeshenskaya)
                .zoom(7)
                .build()
        ));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    99);
        }

        new ServerMarkerAllGet().execute();

        mMap.setOnMarkerClickListener(marker -> {
            openGMaps.setVisibility(View.VISIBLE);
            openGMaps.setOnClickListener(v -> {
                String coord = "google.navigation:q=" + marker.getPosition().latitude + "," + marker.getPosition().longitude;
                Uri gmmIntentUri = Uri.parse(coord);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(MapsActivity.this, "Для выполнения данного действия необходимо установить приложение \"Google Карты\"", Toast.LENGTH_SHORT).show();
                }
            });

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragment = new FragmentShortAbout();
            Bundle bundle = new Bundle();
            bundle.putString("Title", marker.getTitle());
            fragment.setArguments(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.shortAboutFragment, fragment).commit();

            progressIndicator.startAnimation();

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker.getPosition())
                    .zoom(14)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.animateCamera(cameraUpdate);
            return true;
        });
    }

    private class ServerMarkerAllGet extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Void... voids) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String url = "http://80.254.124.90/00sidorov/markerAll/get";
            return new API(url).getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);
            logo.animate().setDuration(500).alpha(0).withEndAction(() -> logo.setVisibility(View.GONE));
            progressIndicator.stopAnimation();
            try {
                JSONArray data = json.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    JSONObject object = data.getJSONObject(i);
                    LatLng point = new LatLng(object.getDouble("coordLat"), object.getDouble("coordLng"));

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(point)
                            .title(object.getString("code"));
                    mMap.addMarker(markerOptions);
                }

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(coordVeshenskaya)
                        .zoom(12)
                        .build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.animateCamera(cameraUpdate);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void showInfoWindow(int visible) {
        if (heightMap == 0)
            heightMap = map.getMeasuredHeight();

        progressIndicator.stopAnimation();

        shortAboutFragment.post(() -> {
            ValueAnimator anim = null;
            switch (visible) {
                case View.VISIBLE:
                    anim = ValueAnimator.ofInt(map.getMeasuredHeight(), heightMap - shortAboutFragment.getHeight());
                    anim.addUpdateListener(valueAnimator -> {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = map.getLayoutParams();
                        layoutParams.height = val;
                        map.setLayoutParams(layoutParams);
                    });
                    anim.setDuration(500);
                    anim.setInterpolator(new AccelerateDecelerateInterpolator());
                    anim.start();

                    isAboutOpen = true;
                    break;
                case View.INVISIBLE:
                    anim = ValueAnimator.ofInt(map.getMeasuredHeight(), heightMap);
                    anim.addUpdateListener(valueAnimator -> {
                        int val = (Integer) valueAnimator.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams = map.getLayoutParams();
                        layoutParams.height = val;
                        map.setLayoutParams(layoutParams);
                    });
                    anim.setDuration(500);
                    anim.setInterpolator(new AccelerateDecelerateInterpolator());
                    anim.start();

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(mMap.getCameraPosition().target)
                            .zoom(12)
                            .build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                    mMap.animateCamera(cameraUpdate);

                    isAboutOpen = false;
                    break;
            }
        });
    }

    public void showAboutWindow(int visible) {
        ValueAnimator anim = null;
        switch (visible) {
            case View.VISIBLE:
                heightMapWithAboutWindowOpen = map.getMeasuredHeight();
                anim = ValueAnimator.ofInt(map.getMeasuredHeight(), 0);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = map.getLayoutParams();
                    layoutParams.height = val;
                    map.setLayoutParams(layoutParams);
                });
                anim.setDuration(500);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.start();

                ((FragmentShortAbout) fragment).animateImage();
                break;
            case View.INVISIBLE:
                anim = ValueAnimator.ofInt(map.getMeasuredHeight(), heightMapWithAboutWindowOpen);
                anim.addUpdateListener(valueAnimator -> {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = map.getLayoutParams();
                    layoutParams.height = val;
                    map.setLayoutParams(layoutParams);
                });
                anim.setDuration(500);
                anim.setInterpolator(new AccelerateDecelerateInterpolator());
                anim.start();
                break;
        }
    }
}