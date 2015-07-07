package com.sordsman.tweetmyt;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.widget.Button;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


public class TweetMytActivity extends Activity implements View.OnClickListener {

    //developer account
    public final static String TWIT_KEY = "tJyOEOPYGeso4WuwednTPDGQs";
    /* developer secret */
    public final static String TWIT_SECRET = "9mBgYjEHCZ0z4hdn5uU9ZMTcqnJZD2kCai8qd0ueDRCIgqjhb1";
    /* app url*/
    public final static String TWIT_URL = "tnice-android:///";

    /* Twitter instance*/
    private Twitter niceTwitter;
    /* request token for accessing user account */
    private RequestToken niceRequestToken;

    /*shared preferences to store user details*/
    private SharedPreferences sharedPreferences;

    /*error logging*/
    private String LOG_TAG = "TweetMytActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        /*getting preferences for the app*/
        sharedPreferences = getSharedPreferences("TweetMytPrefs", 0);

        /*find out if user preferences are set*/
        if(sharedPreferences.getString("user_token", null)==null){
            /*prompt to sign in*/
            setContentView(R.layout.activity_main);

            /*get twitter instance for authentication */
            niceTwitter = new TwitterFactory().getInstance();

            /*pass developer key and secret*/
            niceTwitter.setOAuthConsumer(TWIT_KEY, TWIT_SECRET);

            /*try to get request token*/
            try{
                /*get authentication token*/
                niceRequestToken = niceTwitter.getOAuthRequestToken(TWIT_URL);
            } catch (TwitterException te){
                Log.e(LOG_TAG, "TE " + te.getMessage());
            }

            /*setup button for click listener*/

            Button signIn = (Button)findViewById(R.id.signin);
            signIn.setOnClickListener(this);

        } else {
            /* user preferences already set - get timeline*/
            setupTimeline();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* handles sign in and tweet button presses*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            /*when signin button is pressed*/
            case R.id.signin:
                /*take user to twitter authentication web page to allow access*/
                String authURL = niceRequestToken.getAuthenticationURL();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));
                break;

            /*other listeners*/
            default:
                break;
        }

    }

    /** this method is fired when user returns from Twitter
     * authentication web page**/
    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        /*get retrieved data*/
        Uri twitURI = intent.getData();
        /*making sure the url is correct*/
        if(twitURI!=null && twitURI.toString().startsWith(TWIT_URL)){
            /*verification - get the returned data*/
            String oaVerifier = twitURI.getQueryParameter("oauth_verifier");

            /*attempt to retrieve access token*/
            try{
                /*try to get an access token using the returned data from the verification page*/
                AccessToken accessToken = niceTwitter.getOAuthAccessToken(niceRequestToken, oaVerifier);

                /*add token and secret to shared prefs for future reference*/
                sharedPreferences.edit()
                        .putString("user_token", accessToken.getToken())
                        .putString("user_secret", accessToken.getTokenSecret())
                        .commit();

                /*display timeline*/
                setupTimeline();
            }catch (TwitterException te){
                Log.e(LOG_TAG, "Failed to get access token: " + te.getMessage());
            }
        }
    }

}
