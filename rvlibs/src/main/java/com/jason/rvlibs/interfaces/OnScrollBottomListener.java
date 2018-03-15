package com.jason.rvlibs.interfaces;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;

/**
 * recyclerView scroll to bottom listener
 *
 * @attention compat LinearLayoutManager、GridLayoutManager、StaggeredGridLayoutManager
 * Created by liusong on 2017/12/29.
 */

public abstract class OnScrollBottomListener extends RecyclerView.OnScrollListener {
    //layoutManager type
    private static final int MANAGER_LINEAR = 1;
    private static final int MANAGER_GRID = 2;
    private static final int MANAGER_STAGGERED = 3;

    // type of layoutManager
    private int layoutManagerType;
    // last position
    private int[] lastPositions;
    // last visible position
    private int lastVisibleItemPosition;
    // current scrolling state
    private int currentScrollState = 0;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        Log.i("onScrolled","dx="+dx+",dy="+dy);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        //  int lastVisibleItemPosition = -1;
        if (layoutManager instanceof LinearLayoutManager) {
            layoutManagerType = MANAGER_LINEAR;
        } else if (layoutManager instanceof GridLayoutManager) {
            layoutManagerType = MANAGER_GRID;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            layoutManagerType = MANAGER_STAGGERED;
        } else {
            throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }

        switch (layoutManagerType) {
            case MANAGER_LINEAR:
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case MANAGER_GRID:
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case MANAGER_STAGGERED:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
                break;
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        currentScrollState = newState;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if ((visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItemPosition >= totalItemCount - 1)) {
            onBottom();
        }
    }

    public abstract void onBottom();

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }
}
