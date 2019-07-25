package com.sunrain.timetablev4.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

public class AppWidgetDao extends BaseDao {

    private static final String TABLE_NAME = "app_widget";

    public static void saveAppWidgetConfig(int appWidgetId, int backgroundColor, int timeStyle, int weekStyle) {
        SQLiteDatabase db = DBManager.getDb();

        ContentValues values = new ContentValues(4);
        values.put("backgroundColor", backgroundColor);
        values.put("timeStyle", timeStyle);
        values.put("weekStyle", weekStyle);

        String whereClause = "appWidgetId = ?";
        String[] whereArgs = {String.valueOf(appWidgetId)};

        int number = update(db, TABLE_NAME, values, whereClause, whereArgs);

        if (number == 0) {

            values.put("appWidgetId", appWidgetId);
            insert(db, TABLE_NAME, values);
        }

        DBManager.close(db);
    }

    public static Map<String, Integer> getAppWidgetConfig(int appWidgetId) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"backgroundColor", "timeStyle", "weekStyle"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return null;
        }

        Map<String, Integer> configMap = null;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            configMap = new HashMap<>();
            configMap.put("backgroundColor", cursor.getInt(cursor.getColumnIndex("backgroundColor")));
            configMap.put("timeStyle", cursor.getInt(cursor.getColumnIndex("timeStyle")));
            configMap.put("weekStyle", cursor.getInt(cursor.getColumnIndex("weekStyle")));
        }

        cursor.close();

        return configMap;
    }

    public static int getAppWidgetBackgroundColor(int appWidgetId, int defaultColor) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"backgroundColor"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultColor;
        }

        int backgroundColorIndex = cursor.getColumnIndex("backgroundColor");
        int backgroundColor;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            backgroundColor = cursor.getInt(backgroundColorIndex);
        } else {
            backgroundColor = defaultColor;
        }

        cursor.close();

        return backgroundColor;
    }

    public static int getAppWidgetTimeStyle(int appWidgetId, int defaultTimeStyle) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"timeStyle"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultTimeStyle;
        }

        int timeStyleIndex = cursor.getColumnIndex("timeStyle");
        int timeStyle;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            timeStyle = cursor.getInt(timeStyleIndex);
        } else {
            timeStyle = defaultTimeStyle;
        }

        cursor.close();

        return timeStyle;
    }

    public static int getAppWidgetWeekStyle(int appWidgetId, int defaultWeekStyle) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"weekStyle"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultWeekStyle;
        }

        int weekStyleIndex = cursor.getColumnIndex("weekStyle");
        int weekStyle;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            weekStyle = cursor.getInt(weekStyleIndex);
        } else {
            weekStyle = defaultWeekStyle;
        }

        cursor.close();

        return weekStyle;
    }

    public static void saveAppWidgetCurrentTime(int appWidgetId, long currentTime) {
        SQLiteDatabase db = DBManager.getDb();

        ContentValues values = new ContentValues(2);
        values.put("currentTime", currentTime);

        String whereClause = "appWidgetId = ?";
        String[] whereArgs = {String.valueOf(appWidgetId)};

        int number = update(db, TABLE_NAME, values, whereClause, whereArgs);

        if (number == 0) {
            // 使用insertOrReplace会重置其他列的数据
            values.put("appWidgetId", appWidgetId);
            insert(db, TABLE_NAME, values);
        }

        DBManager.close(db);
    }

    public static long getAppWidgetCurrentTime(int appWidgetId, long defaultTime) {
        SQLiteDatabase db = DBManager.getDb();
        String selection = "appWidgetId = ?";
        String[] selectionArgs = {String.valueOf(appWidgetId)};
        String[] columns = {"currentTime"};
        Cursor cursor = queryComplex(db, TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
        int count = cursor.getCount();

        if (count == 0) {
            cursor.close();
            return defaultTime;
        }

        int currentTimeIndex = cursor.getColumnIndex("currentTime");
        long currentTime = 0;

        if (cursor.moveToNext()) {// id只存在一个，所以不用while
            currentTime = cursor.getLong(currentTimeIndex);
        }

        if (currentTime == 0) {
            currentTime = defaultTime;
        }

        cursor.close();

        return currentTime;
    }

    public static void deleteAppWidget(int appWidgetId) {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, "appWidgetId = ?", new String[]{String.valueOf(appWidgetId)});
        DBManager.close(db);
    }

    public static void clear() {
        SQLiteDatabase db = DBManager.getDb();
        delete(db, TABLE_NAME, null, null);
        DBManager.close(db);
    }
}
