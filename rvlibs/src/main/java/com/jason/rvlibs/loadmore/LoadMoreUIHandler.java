package com.jason.rvlibs.loadmore;

/**
 * Created by liusong on 2018/1/2.
 */

public interface LoadMoreUIHandler {
    void onLoading();

    void onLoaded();

    void onLoadedAll();

    void onReset();
}
