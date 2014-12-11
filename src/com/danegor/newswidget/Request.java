package com.danegor.newswidget;

import android.util.Log;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by egor on 19.05.14.
 */
public class Request {
    String urlString;

    public Request(String url) {
        urlString = url;
    }

    public String request() {
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
//            urlConnection.setConnectTimeout(10000);
//            urlConnection.setReadTimeout(10000);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String result, line = reader.readLine();
                result = line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
                return result;
            } finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}

