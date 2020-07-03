package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    long id;
    String body;
    String createdAt;
    User user;
    String mediaUrl;

    int heartCount;
    boolean hearted;

    int retweetCount;
    boolean retweeted;

    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.id = jsonObject.getLong("id");
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));

        tweet.heartCount = jsonObject.getInt("favorite_count");
        tweet.hearted = jsonObject.getBoolean("favorited");

        tweet.retweetCount = jsonObject.getInt("retweet_count");
        tweet.retweeted = jsonObject.getBoolean("retweeted");

        if (jsonObject.has("extended_entities")) {
            JSONObject entities = jsonObject.getJSONObject("extended_entities");
            JSONArray media = entities.getJSONArray("media");
            for (int i = 0; i < media.length(); i++) {
                if ("photo".equals(media.getJSONObject(i).getString("type"))) {
                    tweet.mediaUrl = media.getJSONObject(i).getString("media_url_https");
                    break;
                }
            }
        }
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        String relativeDate = "";
        try {
            long dateMillis = sf.parse(rawJsonDate).getTime();
            relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return relativeDate;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public int getHeartCount() {
        return heartCount;
    }

    public boolean isHearted() {
        return hearted;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public long getId() {
        return id;
    }

    public void setHeartCount(int heartCount) {
        this.heartCount = heartCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }
}
