package com.sordsman.tweetmyt;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by eit on 7/12/15.
 */
public class MytTweet extends Activity implements View.OnClickListener {
    /*shared preferences for user twitter details*/
    private SharedPreferences tweetPrefs;
    /*twitter object*/
    private Twitter tweetTwitter;

    /*twitter key*/
    public final static String TWIT_KEY = "tJyOEOPYGeso4WuwednTPDGQs";
    /*twitter secret*/
    public final static String TWIT_SECRET = "9mBgYjEHCZ0z4hdn5uU9ZMTcqnJZD2kCai8qd0ueDRCIgqjhb1";

    /*the update ID for this tweet if it is a reply*/
    private long tweetID = 0;
    /*the username for the tweet if it is a reply*/
    private String tweetName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*set tweet layout*/
        setContentView(R.layout.tweet);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*call helper method*/
        setupTweet();
    }

    /**Method called whenever this Activity starts
     * - get ready to tweet
     * Sets up twitter and onClick listeners
     * - also sets up for replies**/
    private void setupTweet(){
        /*prepare to tweet*/
        tweetPrefs = getSharedPreferences("TweetMytPrefs", 0);
        /*get user token and secret for authentication*/
        String userToken = tweetPrefs.getString("user_token", null);
        String userSecret = tweetPrefs.getString("user_secret", null);

        /*create a new twitter configuration using user details*/
        twitter4j.conf.Configuration twitConf = new ConfigurationBuilder()
                .setOAuthConsumerKey(TWIT_KEY)
                .setOAuthConsumerSecret(TWIT_SECRET)
                .setOAuthAccessToken(userToken)
                .setOAuthAccessTokenSecret(userSecret)
                .build();

        /*create a new twitter instance*/
        tweetTwitter = new TwitterFactory(twitConf).getInstance();

        /*get any data passed to this intent for a reply*/
        Bundle extras = getIntent().getExtras();

        /*if there are no extras, its just a simple tweet,
        else - its a reply*/
        if (extras != null){
            /*get the ID of the tweet we are replying to*/
            tweetID = extras.getLong("tweetID");
            /*get the user screen name for the tweet we are replying to*/
            tweetName = extras.getString("tweetUser");
            /*get a reference to the text field for tweeting*/
            EditText theReply = (EditText)findViewById(R.id.tweettext);
            theReply.setText("@"+tweetName+" ");
            /*set the cursor to the end of the text for entry*/
            theReply.setSelection(theReply.getText().length());
        }
        else{
            EditText theReply = (EditText)findViewById(R.id.tweettext);
            theReply.setText("");
        }

        /*setup listener for choosing home button to go to timeline*/
        LinearLayout tweetClicker = (LinearLayout)findViewById(R.id.homebtn);
        tweetClicker.setOnClickListener(this);

        /*set up listener for send tweet button*/
        Button tweetButton = (Button)findViewById(R.id.dotweet);
        tweetButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        /*handle home and send buttons*/
        EditText tweetTxt = (EditText)findViewById(R.id.tweettext);
        /*find out which view was clicked*/
        switch (v.getId()){
            case R.id.dotweet:
            /*send tweet*/
            String toTweet = tweetTxt.getText().toString();
                try{
                    /*handle replies*/
                    if(tweetName.length()>0){
                        tweetTwitter.updateStatus(new StatusUpdate(toTweet).inReplyToStatusId(tweetID));
                        /*handle normal tweets*/

                    } else {
                        tweetTwitter.updateStatus(toTweet);

                            /*reset the edit text*/
                        tweetTxt.setText("");
                    }
                } catch (TwitterException te){
                    Log.e("MytTweet", te.getMessage());
                }
            break;

            case R.id.homebtn:
                /*go to the home timeline*/
                tweetTxt.setText("");

                break;
            default:
                break;
        }
        finish();
    }
}
