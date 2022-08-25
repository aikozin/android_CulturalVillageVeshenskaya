package com.sidorov.culturalvillageveshenskaya;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sidorov.culturalvillageveshenskaya.Other.FeedBacks;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FragmentLongAbout extends Fragment {

    TextView objectsMessageTV, objectsTimeWorkTV, objectsAuthorTV, attributesTextTV, objectsUrlsTV, feedbackRatingTV;

    List<FeedBacks> feedBacks;
    LinearLayout feedbackListView;

    TextView litterTV;
    EditText feedbackUserNameET, feedbackMessageET;
    ImageView[] starImages;
    public int isRatingSet = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_long_about, container, false);

        ((MapsActivity) Objects.requireNonNull(getActivity())).progressIndicator.startAnimation();

        objectsMessageTV = view.findViewById(R.id.objectsMessageTV);
        objectsTimeWorkTV = view.findViewById(R.id.objectsTimeWorkTV);
        objectsAuthorTV = view.findViewById(R.id.objectsAuthorTV);
        attributesTextTV = view.findViewById(R.id.attributesTextTV);
        objectsUrlsTV = view.findViewById(R.id.objectsUrlsTV);
        feedbackRatingTV = view.findViewById(R.id.feedbackRatingTV);

        feedBacks = new ArrayList<>();
        feedbackListView = view.findViewById(R.id.feedbackListView);

        litterTV = view.findViewById(R.id.litterTV);
        feedbackUserNameET = view.findViewById(R.id.feedbackUserNameET);
        feedbackMessageET = view.findViewById(R.id.feedbackMessageET);
        starImages = new ImageView[] {
                view.findViewById(R.id.star0),
                view.findViewById(R.id.star1),
                view.findViewById(R.id.star2),
                view.findViewById(R.id.star3),
                view.findViewById(R.id.star4)
        };

        Bundle bundle = getArguments();
        assert bundle != null;
        String index = bundle.getString("Title");
        new ServerLongDescriptionGet().execute(index);

        Button buttonSetFeedback = view.findViewById(R.id.buttonSetFeedback);
        EditText[] edList = {feedbackUserNameET, feedbackMessageET};
        CustomTextWatcher textWatcher = new CustomTextWatcher(edList, buttonSetFeedback, litterTV);
        for (EditText editText : edList) editText.addTextChangedListener(textWatcher);
        for (ImageView imageView : starImages) imageView.setOnClickListener(v -> {
            int id = Integer.parseInt(v.getTag().toString());
            for (int i = 0; i < 5; i++) {
                if (i <= id)
                    starImages[i].setImageResource(R.mipmap.star1);
                else
                    starImages[i].setImageResource(R.mipmap.star0);
                isRatingSet = id + 1;
                for (EditText editText : edList)
                    if (editText.getText().toString().trim().length() <= 0) {
                        buttonSetFeedback.setEnabled(false);
                        break;
                    }
                    else if (isRatingSet != -1)
                        buttonSetFeedback.setEnabled(true);
            }
        });
        buttonSetFeedback.setOnClickListener(v -> {
            new ServerSetFeedback().execute(
                    index,
                    feedbackUserNameET.getText().toString(),
                    feedbackMessageET.getText().toString(),
                    "" + isRatingSet
            );
        });

        return view;
    }

    private class ServerLongDescriptionGet extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String url = "http://80.254.124.90/00sidorov/fullDescription/get/?code=" + strings[0];

            String urlFeedbacks = "http://80.254.124.90/00sidorov/feedback/get/?code=" + strings[0];
            JSONObject jsonFeedback = new API(urlFeedbacks).getJsonString();

            try {
                JSONArray jsonArray = jsonFeedback.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++)
                    feedBacks.add(new FeedBacks(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new API(url).getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            super.onPostExecute(json);

            try {
                String objectsMessage = json.getString("objectsMessage"),
                        objectsTimeWork = json.getString("objectsTimeWork"),
                        objectsAuthor = json.getString("objectsAuthor"),
                        attributesText = json.getString("attributesText"),
                        objectsUrls = json.getString("objectsUrls");
                Double feedbackRating = json.getDouble("feedbackRating");

                objectsMessageTV.setText(objectsMessage);
                objectsTimeWorkTV.setText(objectsTimeWork);
                objectsAuthorTV.setText(objectsAuthor);
                attributesTextTV.setText(attributesText);
                objectsUrlsTV.setText("Доп. информация в интернете...");
                objectsUrlsTV.setOnClickListener(v -> {
                    Uri adress = Uri.parse(objectsUrls);
                    Intent intent = new Intent(Intent.ACTION_VIEW, adress);
                        startActivity(intent);
                });
                feedbackRatingTV.setText(String.format("%.1f", feedbackRating));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            objectsMessageTV.setBackground(null);
            objectsTimeWorkTV.setBackground(null);
            objectsAuthorTV.setBackground(null);
            attributesTextTV.setBackground(null);
            objectsUrlsTV.setBackground(null);

            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            for (int i = 0; i < feedBacks.size(); i++) {
                View convertView = layoutInflater.inflate(R.layout.feedback_item, null);

                TextView litterTV = convertView.findViewById(R.id.litterTV);
                TextView feedbackUserNameTV = convertView.findViewById(R.id.feedbackUserNameTV);
                ImageView[] starImages = new ImageView[] {
                        convertView.findViewById(R.id.star0),
                        convertView.findViewById(R.id.star1),
                        convertView.findViewById(R.id.star2),
                        convertView.findViewById(R.id.star3),
                        convertView.findViewById(R.id.star4)
                };
                TextView feedbackTimeTV = convertView.findViewById(R.id.feedbackTimeTV);
                TextView feedbackMessageTV = convertView.findViewById(R.id.feedbackMessageTV);

                litterTV.setText(feedBacks.get(i).getFeedbackUserName().substring(0, 1));
                feedbackUserNameTV.setText(feedBacks.get(i).getFeedbackUserName());
                feedbackTimeTV.setText(feedBacks.get(i).getFeedbackTime());
                feedbackMessageTV.setText(feedBacks.get(i).getFeedbackMessage());
                for (int j = 0; j < feedBacks.get(i).getFeedbackRating(); j++) {
                    starImages[j].setImageResource(R.mipmap.star1);
                }

                feedbackListView.addView(convertView);
            }

            ((MapsActivity) Objects.requireNonNull(getActivity())).showAboutWindow(View.VISIBLE);
            ((MapsActivity) Objects.requireNonNull(getActivity())).progressIndicator.stopAnimation();
        }
    }

    public class ServerSetFeedback extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            String message = "";
            try {
                message = URLEncoder.encode(strings[2], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String url = "http://80.254.124.90/00sidorov/feedback/set/?data=" +
                    strings[0] + "," +
                    strings[1] + "," +
                    message + "," +
                    strings[3];
            API api = new API(url);
            return api.getJsonString();
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            try {
                String status = jsonObject.getString("status");
                if (status.equals("ok")) {
                    Toast.makeText(getActivity(), "Отзыв успешно отправлен", Toast.LENGTH_SHORT).show();
                    ((FragmentShortAbout) ((MapsActivity) getActivity()).fragment).showFragmentLongAbout();
                }
                else
                    Toast.makeText(getActivity(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Что-то пошло не так", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class CustomTextWatcher implements TextWatcher {

        View v;
        EditText[] edList;
        TextView litter;

        public CustomTextWatcher(EditText[] edList, Button v, TextView litter) {
            this.v = v;
            this.edList = edList;
            this.litter = litter;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            for (EditText editText : edList) {
                if (editText.getId() == edList[0].getId()) {
                    if (s.toString().equals(editText.getText().toString()))
                        try {
                            litter.setText(s.toString().substring(0, 1));
                        } catch (Exception e) {}
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            for (EditText editText : edList) {
                if (editText.getText().toString().trim().length() <= 0) {
                    v.setEnabled(false);
                    break;
                }
                else if (isRatingSet != -1)
                    v.setEnabled(true);
            }
        }
    }
}
