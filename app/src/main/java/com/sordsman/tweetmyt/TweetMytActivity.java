package com.sordsman.tweetmyt;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import twitter4j.ProfileImage;
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

    /* main view for the home timeline*/
    private ListView homeTimeline;
    /*database helper for update data*/
    private MytDataHelper timelineHelper;
    /*update database*/
    private SQLiteDatabase timelineDB;
    /*adapter for mapping data*/
    private UpdateAdapter timelineAdapter;

    /*specify image size*/
    ProfileImage.ImageSize imageSize = ProfileImage.NORMAL;

    /*Broadcast receiver for when new updates are available*/
    private BroadcastReceiver mytStatusReceiver;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        class RunBackgroundStuff extends IntentService{

            /**
             * Creates an IntentService.  Invoked by your subclass's constructor.
             *
             * @param name Used to name the worker thread, important only for debugging.
             */
            public RunBackgroundStuff(String name) {
                super(name);
            }

            @Override
            protected void onHandleIntent(Intent intent) {
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
                    signIn.setOnClickListener((View.OnClickListener) this);

                } else {
            /* user preferences already set - get timeline*/
                    setupTimeline();
                }

            }
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
            /*user had pressed tweet button*/
            case R.id.tweetbtn:
                /*launch tweet activity*/
                startActivity(new Intent(this, MytTweet.class));
                break;
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
                        .apply();

                /*display timeline*/
                setupTimeline();
            }catch (TwitterException te){
                Log.e(LOG_TAG, "Failed to get access token: " + te.getMessage());
            }
        }
    }

    private void setupTimeline(){
        setContentView(R.layout.timeline);
        LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.tweetbtn);
        tweetClicker.setOnClickListener(this);

        try {
            /*get reference to the list view*/
            homeTimeline = (ListView)findViewById(R.id.homeList);
            /*instantiate database helper*/
            timelineHelper = new MytDataHelper(this);
            /*get the database*/
            timelineDB = timelineHelper.getReadableDatabase();
            /*query the database, most recent tweets first*/
            Cursor timelineCursor = timelineDB.query("tweet", null, null, null, null, null, "update_time DESC");
            startManagingCursor(timelineCursor);
            /*this will make the app populate the new update data in the timeline
            * view*/
            homeTimeline.setAdapter(timelineAdapter);
            /*instantiate receiver class for finding out when new updates are available*/
            mytStatusReceiver = new TwitterUpdateReceiver();
            /*register for updates*/
            registerReceiver(mytStatusReceiver, new IntentFilter("TWITTER_UPDATES"));

            /*start the service for updates now*/
            this.getApplicationContext().startService(new Intent(this.getApplicationContext(), TimeLineService.class));

        }catch (Exception e){
            Log.e(LOG_TAG, "Failed to fetch timeline: " + e);
        }
    }

    class TwitterUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*delete records from database when others come in*/
            int rowLimit = 100;
            if (DatabaseUtils.queryNumEntries(timelineDB, "tweet")>rowLimit) {
                String deleteQuery = "DELETE FROM tweet WHERE "+ BaseColumns._ID+" NOT IN "+
                        "(SELECT "+BaseColumns._ID+" From tweet ORDER BY"+"update_time DESC"+
                        "limit "+rowLimit+")";
                timelineDB.execSQL(deleteQuery);

                /*query the database and update the user interface*/
                Cursor timelineCursor = timelineDB.query("tweet", null, null, null, null, null, "update_time DESC");
                startManagingCursor(timelineCursor);
                timelineAdapter = new UpdateAdapter(context, timelineCursor);
                homeTimeline.setAdapter(timelineAdapter);

            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            /*stop the updater service*/
            stopService(new Intent(this, TimeLineService.class));
            /*remove receiver register*/
            unregisterReceiver(mytStatusReceiver);
            /*close the database*/
            timelineDB.close();
        }
        catch (Exception se){
            Log.e(LOG_TAG, "unable to stop Service or receiver");
        }
    }


}
