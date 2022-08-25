package com.sidorov.culturalvillageveshenskaya;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Objects;

public class FragmentShortAbout extends Fragment {

    View aboutContent;
    TextView objectsNameTV, typeNameTV;
    ImageView objectsImageIV, aboutImageIV;
    LinearLayout aboutButton;
    int heightImage;
    String index;
    FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_short_about, container, false);

        aboutContent = view.findViewById(R.id.aboutContent);
        objectsNameTV = view.findViewById(R.id.objectsName);
        typeNameTV = view.findViewById(R.id.typeName);
        objectsImageIV = view.findViewById(R.id.objectsUrlImage);
        aboutImageIV = view.findViewById(R.id.aboutImage);
        aboutButton = view.findViewById(R.id.aboutButton);

        Bundle bundle = getArguments();
        assert bundle != null;
        index = bundle.getString("Title");
        new ServerShortDescriptionGet().execute(index);

        view.findViewById(R.id.closeInfoWindow).setOnClickListener(v -> {
            ((MapsActivity) Objects.requireNonNull(getActivity())).showInfoWindow(View.INVISIBLE);
        });

        aboutButton.setOnClickListener(v -> {
            if (((TextView) view.findViewById(R.id.textView)).getText().toString().equals("ОТКРЫТЬ ПОДРОБНОСТИ")) {
                heightImage = objectsImageIV.getMeasuredHeight();

                ((TextView) view.findViewById(R.id.textView)).setText("СКРЫТЬ ПОДРОБНОСТИ");

                showFragmentLongAbout();
            } else {
                ((MapsActivity) Objects.requireNonNull(getActivity())).showAboutWindow(View.INVISIBLE);

                ((TextView) view.findViewById(R.id.textView)).setText("ОТКРЫТЬ ПОДРОБНОСТИ");

                ValueAnimator imageAnim = ValueAnimator.ofInt(objectsImageIV.getMeasuredHeight(), heightImage);
                imageAnim.addUpdateListener(valueAnimator -> {
                    int val = (int) valueAnimator.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = objectsImageIV.getLayoutParams();
                    layoutParams.height = val;
                    objectsImageIV.setLayoutParams(layoutParams);
                });
                imageAnim.setDuration(500);
                imageAnim.setInterpolator(new AccelerateDecelerateInterpolator());
                imageAnim.start();
            }
        });

        return view;
    }

    public void showFragmentLongAbout() {
        fragmentManager = getActivity().getSupportFragmentManager();
        FragmentLongAbout fragmentLongAbout = new FragmentLongAbout();
        Bundle bundle2 = new Bundle();
        bundle2.putString("Title", index);
        fragmentLongAbout.setArguments(bundle2);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.longAboutFragment, fragmentLongAbout).commit();
    }

    public void animateImage() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        ValueAnimator imageAnim = ValueAnimator.ofInt(objectsImageIV.getMeasuredHeight(), displayMetrics.heightPixels / 3);
        imageAnim.addUpdateListener(valueAnimator -> {
            int val = (int) valueAnimator.getAnimatedValue();
            ViewGroup.LayoutParams layoutParams = objectsImageIV.getLayoutParams();
            layoutParams.height = val;
            objectsImageIV.setLayoutParams(layoutParams);
        });
        imageAnim.setDuration(500);
        imageAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        imageAnim.start();
    }

    private class ServerShortDescriptionGet extends AsyncTask<String, Void, JSONObject> {

        Bitmap bitmap1, bitmap2;

        @Override
        protected JSONObject doInBackground(String... strings) {
            String url = "http://80.254.124.90/00sidorov/shortDescription/get/?code=" + strings[0];
            JSONObject json = new API(url).getJsonString();
            try {
                String objectsUrlImage = json.getString("objectsUrlImage"),
                        attributesUrlImage = json.getString("attributesUrlImage");
                InputStream in = null;
                if (!objectsUrlImage.equals(""))
                    in = new java.net.URL(objectsUrlImage).openStream();
                if (in != null)
                    bitmap1 = BitmapFactory.decodeStream(in);
                if (!attributesUrlImage.equals(""))
                    in = new java.net.URL(attributesUrlImage).openStream();
                if (in != null)
                    bitmap2 = BitmapFactory.decodeStream(in);
            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return json;
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            try {
                String objectsName = json.getString("objectsName"),
                        typeName = json.getString("typeName");

                objectsNameTV.setText(objectsName);
                typeNameTV.setText(typeName);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            aboutButton.setClickable(true);
            objectsImageIV.setImageBitmap(bitmap1);
            aboutImageIV.setImageBitmap(bitmap2);

            objectsImageIV.setBackground(null);
            aboutImageIV.setBackground(null);
            objectsNameTV.setBackground(null);
            typeNameTV.setBackground(null);

            ((MapsActivity) Objects.requireNonNull(getActivity())).showInfoWindow(View.VISIBLE);
        }
    }
}
