package com.danegor.newswidget;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Created by egor on 10/9/14.
 */
public class API {
    private RequestResult handler;
    private String apiUrl = "http://novo.wada.vn/api/news?top=10&format=json&lang=en";

    public API(RequestResult handler) {
        this.handler = handler;
    }

    public void getNews() {
        new Thread() {
            @Override
            public void run() {
                String result = new Request(apiUrl).request();
                if (!result.equals("")) {
                    JsonParser parser = new JsonParser();
                    if (!(parser.parse(result).getAsJsonObject().getAsJsonObject("result").get("error") instanceof JsonNull)) {
                        handler.fail("Ошибка");
                    }
                    else {
                        handler.received(new Gson().fromJson(parser.parse(result).getAsJsonObject().getAsJsonObject("result").getAsJsonArray("news"), new TypeToken<List<News>>() {
                        }.getType()));
                    }
                } else {
                    handler.fail("Проблемы со связью");
                }
            }
        }.start();
    }
}
