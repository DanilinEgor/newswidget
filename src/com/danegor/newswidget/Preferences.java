package com.danegor.newswidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by egor on 10/9/14.
 */
public class Preferences {
    public static final String PREFERENCES_NAME = "com.danegor.NewsWidget";
    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, context.MODE_PRIVATE);
    }

    public void saveLastUpdateTime(long timestamp) {
        sharedPreferences.edit().putLong("last_update", timestamp).commit();
    }

    public long getLastUpdateTime() {
        return sharedPreferences.getLong("last_update", 0);
    }

    public void saveNews(List<News> newsList) {
        sharedPreferences.edit().putString("news", new Gson().toJson(newsList)).commit();
    }

    public List<News> getNews() {
        return new Gson().fromJson(sharedPreferences.getString("news", "[]"), new TypeToken<List<News>>() {
        }.getType());
    }

    public void saveImage(int id, Bitmap bitmap) {
        sharedPreferences.edit().putString(PREFERENCES_NAME +"image"+id, encodeToBase64(bitmap)).commit();
    }

    public Bitmap getImage(int id) {
        return decodeBase64(sharedPreferences.getString(PREFERENCES_NAME+"image"+id, ""));
    }

    public static String encodeToBase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        if (input.equals(""))
            return null;
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
