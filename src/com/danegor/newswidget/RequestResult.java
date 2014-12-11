package com.danegor.newswidget;

/**
 * Created by egor on 17.03.14.
 */
public interface RequestResult<T> {
    public void received(T t);

    public void fail(String s);
}
