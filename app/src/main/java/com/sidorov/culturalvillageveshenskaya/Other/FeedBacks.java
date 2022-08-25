package com.sidorov.culturalvillageveshenskaya.Other;

import android.content.Intent;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FeedBacks {

    String feedbackUserName, feedbackMessage;
    long feedbackTime;
    int feedbackRating;

    public FeedBacks(JSONObject json) throws JSONException {
        this.feedbackTime = Long.parseLong(json.getString("feedbackTime"));
        this.feedbackUserName = json.getString("feedbackUserName");
        this.feedbackMessage = json.getString("feedbackMessage");
        this.feedbackRating = Integer.parseInt(json.getString("feedbackRating"));
    }

    public String getFeedbackTime() {
        long time = feedbackTime * (long) 1000;
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("dd MMM - HH:mm");
        return format.format(date);
    }

    public String getFeedbackUserName() {
        return feedbackUserName;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public int getFeedbackRating() {
        return feedbackRating;
    }
}
