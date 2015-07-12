package com.sordsman.tweetmyt;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import twitter4j.Status;

/**
 * Created by eit on 7/7/15.
 */
public class MytDataHelper extends SQLiteOpenHelper {
    /*database version*/
    private static final int DATABASE_VERSION = 1;
    /* database name */
    private static final String DATABASE_NAME = "tweet.db";
    /*ID Column*/
    private static final String HOME_COL = BaseColumns._ID;
    /**tweet text**/
    private static final String UPDATE_COL = "update_text";
    /*twitter screen name*/
    private static final String USER_COL = "user_screen";
    /* time user tweeted */
    private static final String TIME_COL = "update_time";
    /*user profile image*/
    private static final String USER_IMG = "user_img";

    /*creating a database -- string*/
    private static final String DATABASE_CREATE = "CREATE TABLE tweet (" + HOME_COL + " INTEGER NOT NULL PRIMARY KEY, " +
            UPDATE_COL + " TEXT, " + USER_COL + " TEXT, " + TIME_COL + " INTEGER, " + USER_IMG + " TEXT);";

    /* constructor method @param context*/
    MytDataHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*creates database string*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS tweet");
        db.execSQL("VACUUM");
        onCreate(db);
    }

    /**
     * getValues retrieves the database records
     * - called from TimelineUpdater in TimelineService
     * - this is a static method that can be called without an instance of the class
     *
     * @param status
     * @return ContentValues result
     */

    public static ContentValues getValues(Status status){
        ContentValues homeValues = new ContentValues();

        /*get each value from the table*/
        try{
            homeValues.put(HOME_COL, status.getId());
            homeValues.put(UPDATE_COL, status.getText());
            homeValues.put(USER_COL, status.getUser().getScreenName());
            homeValues.put(TIME_COL, status.getCreatedAt().getTime());
            homeValues.put(USER_IMG, String.valueOf(status.getUser().getProfileImageURL())); /*convert to .toString()?*/

        } catch (Exception e){
            Log.e("MytDataHelper", e.getMessage());
        }

        return homeValues;
    }


}
