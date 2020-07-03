package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    public static final int MAX_TWEET_LENGTH = 280;
    private static final int REQUEST_CODE_REPLY = 25;
    public static final String TAG = "ComposeActivity";

    EditText etCompose;
    Button btnTweet;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        btnTweet = findViewById(R.id.btnTweet);
        etCompose = findViewById(R.id.etCompose);

        final int requestCode = Parcels.unwrap(getIntent().getParcelableExtra("requestCode"));
        long reply_tweet_id = -1;
        if (requestCode == REQUEST_CODE_REPLY) {
//            etCompose.setHint(String.format("%s", "Add another Tweet"));
            Tweet reply_tweet = Parcels.unwrap(getIntent().getParcelableExtra("reply_tweet"));
            etCompose.setText(String.format("@%s ", reply_tweet.getUser().getHandle()));
            reply_tweet_id = reply_tweet.getId();
        } else {
            etCompose.setHint(String.format("%s", "What's happening?"));
        }

        // Set click listener on button
        final long finalReply_tweet_id = reply_tweet_id;
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_SHORT).show();
                } else if (requestCode == REQUEST_CODE_REPLY) {
                    // Make API call to Twitter to **REPLY** to a tweet
                    client.replyTweet(tweetContent, finalReply_tweet_id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to publish tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Log.i(TAG, "Published tweet says: " + tweet.getBody());

                                Intent intent = new Intent();
                                intent.putExtra("tweet", Parcels.wrap(tweet));
                                setResult(RESULT_OK, intent);
                                finish();
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed on retrieving published tweet", e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to publish tweet", throwable);
                        }
                    });
                } else {
                    // Make API call to Twitter to publish the tweet
                    client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i(TAG, "onSuccess to publish tweet");
                            try {
                                Tweet tweet = Tweet.fromJson(json.jsonObject);
                                Log.i(TAG, "Published tweet says: " + tweet.getBody());

                                Intent intent = new Intent();
                                intent.putExtra("tweet", Parcels.wrap(tweet));
                                setResult(RESULT_OK, intent);
                                finish();
                            } catch (JSONException e) {
                                Log.e(TAG, "Failed on retrieving published tweet", e);
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.e(TAG, "onFailure to publish tweet", throwable);
                        }
                    });
                }
            }
        });
    }
}