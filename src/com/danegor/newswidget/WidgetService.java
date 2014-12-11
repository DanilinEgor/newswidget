package com.danegor.newswidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by egor on 10/9/14.
 */
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return (new ListProvider(this.getApplicationContext(), intent));
    }

}