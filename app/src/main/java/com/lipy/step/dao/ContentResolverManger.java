package com.lipy.step.dao;

import com.lipy.step.common.BaseApplication;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by lipy on 2017/4/25 0025.
 */
public class ContentResolverManger {

    private static ContentResolverManger ourInstance;
    private final ContentResolver mResolver;

    public static ContentResolverManger getInstance() {
        if (ourInstance == null) {
            ourInstance = new ContentResolverManger();
        }
        return ourInstance;
    }

    private ContentResolverManger() {
        mResolver = BaseApplication.getInstances().getContentResolver();
    }

    public void add(String date, String dailyStep, String totalSteps, String tagStep, boolean reStart, boolean punchCard) {
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("dailyStep", dailyStep);
        values.put("totalSteps", totalSteps);
        values.put("tagStep", tagStep);
        values.put("reStart", reStart);
        values.put("punchCard", punchCard);
        mResolver.insert(PedometerProvider.CONTENT_URI, values);
    }

    public void update(String date, String dailyStep, String totalSteps, String tagStep, boolean reStart, boolean punchCard) {
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("dailyStep", dailyStep);
        values.put("totalSteps", totalSteps);
        values.put("tagStep", tagStep);
        values.put("reStart", reStart);
        values.put("punchCard", punchCard);
        mResolver.update(PedometerProvider.CONTENT_URI, values, null, null);
    }

    public void query() {
        Cursor cursor = mResolver.query(PedometerProvider.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()) {
            cursor.getString(0);
            cursor.getString(1);
            cursor.getString(2);
            cursor.getString(3);
        }

    }
}
