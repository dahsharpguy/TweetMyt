package com.sordsman.tweetmyt;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by eit on 7/7/15.
 */
public class UpdateAdapter extends SimpleCursorAdapter {
    /*twitter developer key*/
    public final static String TWIT_KEY = "tJyOEOPYGeso4WuwednTPDGQs";
    /*twitter developer secret*/
    public final static String TWIT_SECRET = "9mBgYjEHCZ0z4hdn5uU9ZMTcqnJZD2kCai8qd0ueDRCIgqjhb1";

    /*strings for database column names to map to views*/
    static final String[] from = {"update_text", "user_screen", "update_time", "user_img"};

    /*view item IDs for mapping database record values to*/
    static final int[] to = { R.id.updateText, R.id.userScreen, R.id.updateTime, R.id.updateTime, R.id.userImg };

    private String LOG_TAG = "UpdateAdapter";



    public UpdateAdapter(Context context, Cursor c) {
        super(context, R.layout.update, c, from, to);
    }

    @Override
    public void bindView(View row, Context context, Cursor cursor){
        super.bindView(row, context, cursor);

        try {
            /*get profile image*/
            URL profileURL = new URL(cursor.getString(cursor.getColumnIndex("user_img")));

            /*set the image in the view for the current tweet*/
            ImageView profPic = (ImageView)row.findViewById(R.id.userImg);
            profPic.setImageDrawable(Drawable.createFromStream((InputStream)profileURL.getContent(), ""));
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage());
        }

        //get the update time
        long createdAt = cursor.getLong(cursor.getColumnIndex("update_time"));
        //get the update time view
        TextView textCreatedAt = (TextView)row.findViewById(R.id.updateTime);
        //adjust the way the time is displayed to make it human-readable
        textCreatedAt.setText(DateUtils.getRelativeTimeSpanString(createdAt) + " ");
    }
}
