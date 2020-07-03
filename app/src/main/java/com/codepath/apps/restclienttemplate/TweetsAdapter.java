package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.sql.Time;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;


public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    private static final String TAG = "TweetAdapter";
    private static final int REQUEST_CODE_REPLY = 25;

    Context context;
    List<Tweet> tweets;

    // Pass in the context and list of tweets
    public TweetsAdapter(Context context, List<Tweet> tweets) {
        this.context = context;
        this.tweets = tweets;
    }

    // For each row, inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // Bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data at position
        Tweet tweet= tweets.get(position);

        // Bind the tweet with view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

    // Define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {

        TwitterClient client;

        ImageView ivProfileImage;
        TextView tvScreenName;
        TextView tvHandle;
        TextView tvRelativeDate;
        TextView tvBody;

        ImageView ivMedia;

        ImageButton btnReply;
        ImageButton btnRetweet;
        ImageButton btnHeart;
        TextView retweetCount;
        TextView heartCount;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            client = TwitterApp.getRestClient(context);

            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvHandle = itemView.findViewById(R.id.tvHandle);
            tvRelativeDate = itemView.findViewById(R.id.tvRelativeDate);
            tvBody = itemView.findViewById(R.id.tvBody);

            ivMedia = itemView.findViewById(R.id.ivMedia);

            btnReply = itemView.findViewById(R.id.btnReply);
            btnRetweet = itemView.findViewById(R.id.btnRetweet);
            btnHeart = itemView.findViewById(R.id.btnHeart);
            retweetCount = itemView.findViewById(R.id.retweetCount);
            heartCount = itemView.findViewById(R.id.heartCount);
        }

        public void bind(final Tweet tweet) {
            tvScreenName.setText(tweet.getUser().getName());
            tvHandle.setText(String.format("@%s", tweet.getUser().getHandle()));
            tvRelativeDate.setText(tweet.getRelativeTimeAgo(tweet.getCreatedAt()));
            tvBody.setText(tweet.getBody());

            Glide.with(context).load(tweet.getUser().getProfileImageUrl()).into(ivProfileImage);
            if (tweet.getMediaUrl() != null) {
                ivMedia.setVisibility(View.VISIBLE);

                int radius = 30; // corner radius, higher value = more rounded
                int margin = 10; // crop margin, set to 0 for corners with no crop
                Glide.with(context).load(tweet.getMediaUrl()).transform(new RoundedCornersTransformation(radius, margin)).into(ivMedia);
            } else {
                ivMedia.setVisibility(View.GONE);
            }

            btnRetweet.setSelected(tweet.isRetweeted());
            btnHeart.setSelected(tweet.isHearted());
            retweetCount.setText(String.format("%s", tweet.getRetweetCount()));
            heartCount.setText(String.format("%s", tweet.getHeartCount()));

            btnReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, ComposeActivity.class);
                    i.putExtra("requestCode", Parcels.wrap(REQUEST_CODE_REPLY));
                    i.putExtra("reply_tweet", Parcels.wrap(tweet));
                    ((Activity)context).startActivityForResult(i, REQUEST_CODE_REPLY);
                }
            });

            btnRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnRetweet.setSelected(!btnRetweet.isSelected());
                    if (btnRetweet.isSelected()) {
                        // Make API call to Twitter to favorite the tweet
                        client.retweetTweet(tweet.getId(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess to favorite tweet: " + tweet);
                                retweetCount.setText(String.format("%s", tweet.getRetweetCount() + 1));
                                tweet.setRetweetCount(tweet.getRetweetCount() + 1);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to favorite tweet: " + tweet, throwable);
                                Toast.makeText(context, "Error occurred in retweeting this tweet", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Make API call to Twitter to favorite the tweet
                        client.unretweetTweet(tweet.getId(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess to favorite tweet: " + tweet);
                                retweetCount.setText(String.format("%s", tweet.getRetweetCount() - 1));
                                tweet.setRetweetCount(tweet.getRetweetCount() - 1);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to favorite tweet: " + tweet, throwable);
                                Toast.makeText(context, "Error occurred in unretweeting this tweet", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            btnHeart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnHeart.setSelected(!btnHeart.isSelected());
                    if (btnHeart.isSelected()) {
                        // Make API call to Twitter to favorite the tweet
                        client.heartTweet(tweet.getId(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess to favorite tweet: " + tweet);
                                heartCount.setText(String.format("%s", tweet.getHeartCount() + 1));
                                tweet.setHeartCount(tweet.getHeartCount() + 1);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to favorite tweet: " + tweet, throwable);
                                Toast.makeText(context, "Error occurred in favoriting this tweet", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Make API call to Twitter to unfavorite the tweet
                        client.unheartTweet(tweet.getId(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {
                                Log.i(TAG, "onSuccess to unfavorite tweet: " + tweet);
                                heartCount.setText(String.format("%s", tweet.getHeartCount() - 1));
                                tweet.setHeartCount(tweet.getHeartCount() - 1);
                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                Log.e(TAG, "onFailure to unfavorite tweet: " + tweet, throwable);
                                Toast.makeText(context, "Error occurred in unfavoriting this tweet", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }
}
