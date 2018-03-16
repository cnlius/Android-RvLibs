package com.jason.app.decorators;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liusong on 2018/3/16.
 */

public class SpaceDecoration extends RecyclerView.ItemDecoration {

    private float mDividerHeight; // 分割线的高度
    private Paint mPaint; // 画笔

    public SpaceDecoration() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
    }

    /**
     * 设置分割线大小
     * getItemOffsets通过outRect撑开ItemView的上下左右间隔区域
     *
     * @param outRect itemView的容器矩形，通过设置left、top、right、bottom四个方向偏移距离来确定
     * @param view    itemView
     * @param parent  RecyclerView
     * @param state   状态
     * @attention getItemOffsets针对的是每一个ItemView
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 第一个item不设置偏移距离，这样就保持了列表的首尾没有分割线
        if (parent.getChildAdapterPosition(view) != 0) {
            // 设置outRect的top方向的偏移距离
            outRect.top = 10;
            mDividerHeight = 10;
        }
    }


    /**
     * 分割线着色
     * onDraw方法针对RecyclerView本身
     * 在onDraw方法中遍历屏幕上可见的ItemView，分别获取它们的位置信息，然后分别的绘制对应的分割线
     *
     * @param canvas 绘制图像
     * @param parent
     * @param state
     */
    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(view);
            // 第一个ItemView不需要绘制
            if (index == 0) {
                continue;
            }
            float dividerTop = view.getTop() - mDividerHeight;
            float dividerLeft = parent.getPaddingLeft();
            float dividerBottom = view.getTop();
            float dividerRight = parent.getWidth() - parent.getPaddingRight();
            // 绘制分割线
            canvas.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, mPaint);
        }
    }
}
