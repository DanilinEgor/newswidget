package com.danegor.newswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by egor on 10/9/14.
 */
public class NewsWidget extends AppWidgetProvider {
    public static String ACTION_UPDATE_NEWS_FROM_INTERNET = "com.danegor.newswidget.updateinternet";
    public static String ACTION_UPDATE_NEWS_LIST = "com.danegor.newswidget.updatelist";

    @Override
    public void onEnabled(Context context) {
        Intent updateIntent = new Intent(context, NewsWidget.class);
        updateIntent.setAction(ACTION_UPDATE_NEWS_FROM_INTERNET);
        context.sendBroadcast(updateIntent);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent active = new Intent(context, NewsWidget.class);
        active.setAction(ACTION_UPDATE_NEWS_FROM_INTERNET);

        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

        remoteViews.setOnClickPendingIntent(R.id.update_panel, actionPendingIntent);

        long lastUpdateTime = new Preferences(context).getLastUpdateTime();
        if (lastUpdateTime != 0)
            remoteViews.setTextViewText(R.id.last_update,
                    "Обновлено " + new SimpleDateFormat("dd.MM в HH:mm").format(new Date(lastUpdateTime)));
        else
            remoteViews.setTextViewText(R.id.last_update, "Не обновлялось");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, NewsWidget.class));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        ComponentName thisWidget = new ComponentName(context, NewsWidget.class);
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews1 = updateWidgetListView(context, appWidgetId);
            appWidgetManager.updateAppWidget(thisWidget, remoteViews1);
        }

        super.onEnabled(context);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (ACTION_UPDATE_NEWS_FROM_INTERNET.equals(action)) {
            final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
            final ComponentName thisWidget = new ComponentName(context, NewsWidget.class);
            new API(new RequestResult<List<News>>() {
                @Override
                public void received(List<News> newsList) {
                    final Preferences preferences = new Preferences(context);
                    preferences.saveNews(newsList);
                    preferences.saveLastUpdateTime(new Date().getTime());
                    ExecutorService taskExecutor = Executors.newFixedThreadPool(newsList.size());
                    for (final News news : newsList) {
                        taskExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Bitmap image = new Preferences(context).getImage(news.id);
                                    if (image == null) {
                                        InputStream in = new java.net.URL(news.image).openStream();
                                        Bitmap bitmap = BitmapFactory.decodeStream(in);
                                        preferences.saveImage(news.id, bitmap);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    taskExecutor.shutdown();
                    try {
                        taskExecutor.awaitTermination(15, TimeUnit.SECONDS);
                        remoteViews.setTextViewText(R.id.last_update,
                                "Обновлено " + new SimpleDateFormat("dd.MM в HH:mm").format(new Date().getTime()));
                        AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, remoteViews);
                        Intent updateIntent = new Intent(context, NewsWidget.class);
                        updateIntent.setAction(ACTION_UPDATE_NEWS_LIST);
                        context.sendBroadcast(updateIntent);
                    } catch (InterruptedException e) {
                    }
                }

                @Override
                public void fail(String s) {
                    remoteViews.setTextViewText(R.id.last_update, s);
                    AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, remoteViews);
                }
            }).getNews();

            remoteViews.setTextViewText(R.id.last_update, "Обновляется...");
            AppWidgetManager.getInstance(context).updateAppWidget(thisWidget, remoteViews);
        } else if (ACTION_UPDATE_NEWS_LIST.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(context, NewsWidget.class));
            for (int appWidgetId : appWidgetIDs)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);
        }
        super.onReceive(context, intent);
    }

    private RemoteViews updateWidgetListView(Context context, int appWidgetId) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent svcIntent = new Intent(context, WidgetService.class);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
        remoteViews.setRemoteAdapter(appWidgetId, R.id.list, svcIntent);
        return remoteViews;
    }
}
