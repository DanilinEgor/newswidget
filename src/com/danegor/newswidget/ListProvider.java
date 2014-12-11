package com.danegor.newswidget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by egor on 10/9/14.
 */
public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private List<News> newsList = new ArrayList();
    private Context context = null;

    public ListProvider(final Context context, Intent intent) {
        this.context = context;
        newsList = new Preferences(context).getNews();
        Collections.sort(newsList, new Comparator<News>() {
            @Override
            public int compare(News lhs, News rhs) {
                return (int) (rhs.timestamp - lhs.timestamp);
            }
        });
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        newsList = new Preferences(context).getNews();
        Collections.sort(newsList, new Comparator<News>() {
            @Override
            public int compare(News lhs, News rhs) {
                return (int) (rhs.timestamp - lhs.timestamp);
            }
        });
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return newsList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.list_row);
        News news = newsList.get(position);

        remoteView.setTextViewText(R.id.title, news.title);
        remoteView.setTextViewText(R.id.source, news.source.name);
        remoteView.setTextViewText(R.id.date, new SimpleDateFormat("dd.MM\nHH:mm").format(new Date(news.timestamp * 1000)));

        Intent active = new Intent(Intent.ACTION_VIEW);
        active.setData(Uri.parse(news.url));

        PendingIntent actionPendingIntent = PendingIntent.getActivity(context, 0, active, 0);

        remoteView.setOnClickPendingIntent(R.id.rl, actionPendingIntent);
        Bitmap image = new Preferences(context).getImage(news.id);
        if (image != null)
            remoteView.setImageViewBitmap(R.id.imageView, getRoundedCornerBitmap(image, image.getWidth() / 2));
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}