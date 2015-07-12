package com.sordsman.tweetmyt;


import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by eit on 7/9/15.
 */
public class TimeLineService extends Service {
    /*twitter authentication key*/
    public final static String TWIT_KEY = "tJyOEOPYGeso4WuwednTPDGQs";
    /*twitter secret*/
    public final static String TWIT_SECRET = "9mBgYjEHCZ0z4hdn5uU9ZMTcqnJZD2kCai8qd0ueDRCIgqjhb1";
    /*twitter object*/
    private Twitter timelineTwitter;

    /*database helper object*/
    private MytDataHelper mytDataHelper;
    /*timeline database*/
    private SQLiteDatabase mytDB;

    /*shared preferences for user details*/
    private SharedPreferences mytPrefs;
    /*handler for updater*/
    private Handler mytHandler;
    /*delay between fetching new tweets*/
    private static int mins = 4; //you can alter
    private static final long FETCH_DELAY = mins * (60*1000);
    /*debugging tag*/
    private String LOG_TAG = "TimelineService";
    /*updater thread object*/
    private TimelineUpdater mytUpdater;

    @Override
    public void onCreate() {
        super.onCreate();
        /*get prefs*/
        mytPrefs = getSharedPreferences("TwitMytPrefs",0);
        /*get database helper*/
        mytDataHelper = new MytDataHelper(this);
        /*get the database*/
        mytDB = mytDataHelper.getWritableDatabase();

        /**creating an instance of twitter4j object
         * so i can fetch tweets**/

        /*get user preferences*/
        String userToken = mytPrefs.getString("user_token", null);
        String user_Secret = mytPrefs.getString("user_secret", null);

        /*creating new configuration*/
        twitter4j.conf.Configuration twitConf = new ConfigurationBuilder()
                .setOAuthConsumerKey(TWIT_KEY)
                .setOAuthConsumerSecret(TWIT_SECRET)
                .setOAuthAccessToken(userToken)
                .setOAuthConsumerSecret(user_Secret)
                .build();
        /*instantiating new twitter*/
        timelineTwitter = new TwitterFactory(twitConf).getInstance();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * TimelineUpdate class implements the runnable interface
     * */
    class TimelineUpdater implements Runnable {
        /*fetch updates*/


        /*run method*/
        @Override
        public void run() {
            boolean statusChanges = false;

            /*checking for updates .. assuming none */
        /*fetching data from the internet*/
            try {
            /*fetching the timeline*/
                /*retrieving the new home timeline tweets as a list*/
                List<Status> homeTimeline = timelineTwitter.getHomeTimeline();
                /*iterate through new status updates*/
                for (Status statusUpdate : homeTimeline)
                {
                    /*call the getValues method of the data helper class, passing the
                    * new updates*/
                    ContentValues timelineValues = mytDataHelper.getValues(statusUpdate);
                    /*if the database already contains the updates they
                    * will not be inserted*/
                    mytDB.insertOrThrow("tweet", null, timelineValues);
                    statusChanges = true;
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Exception: " + e);

            }
            /*if we have new updates, send a Broadcast*/
            if (statusChanges) {
                /*this should be received in the main timeline class*/
                sendBroadcast(new Intent("TWITTER_UPDATES"));
            }

            /*delay fetching new updates*/
            mytHandler.postDelayed(this, FETCH_DELAY);
        }


    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        //get the handler
        mytHandler = new Handler();
        //create an instance of the updater class
        mytUpdater = new TimelineUpdater();
        //add to run queue
        mytHandler.post(mytUpdater);
        //return sticky
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /*stop the updating*/
        mytHandler.removeCallbacks(mytUpdater);
        mytDB.close();
    }
}
