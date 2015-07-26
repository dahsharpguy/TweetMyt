package com.sordsman.tweetmyt;

/**
 * Created by eit on 7/12/15.
 */
public class StatusData {
    /*tweetID*/
     private long tweetID;
    /*user screen name of tweeter*/
    private String tweetUser;

    public StatusData(long ID, String screenName){
        /*instantiate variables*/
        tweetID = ID;
        tweetUser = screenName;
    }
    /*Get ID of the tweet
    * @return tweetID as a long*/
    public long getID(){
        return tweetID;
    }

    /*Get the user screen name for the tweet
    * @return tweetUser as a String
    * */
    public String getUser(){
        return tweetUser;
    }

}
