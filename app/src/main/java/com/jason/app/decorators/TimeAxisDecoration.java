package com.jason.app.decorators;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jason.app.R;

/**
 * 垂直列表时光轴
 * Created by liusong on 2018/3/16.
 */

public class TimeAxisDecoration extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Bitmap mIcon;

    // ItemView左边的间距
    private float mOffsetLeft;
    // ItemView上边的间距
    private float mOffsetTop;
    // 时间轴结点的半径
    private float mNodeRadius;

    public TimeAxisDecoration(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);

        mOffsetLeft = context.getResources().getDimension(R.dimen.timeline_item_offset_left);
        mNodeRadius = context.getResources().getDimension(R.dimen.timeline_item_node_radius);

        // 使用shape时
//        Drawable dAxis = ContextCompat.getDrawable(context, R.drawable.shape_circle_red);
//        mIcon= ImageUtils.drawable2Bitmap(dAxis);
        mIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.clock);
        // 图片缩放于时间轴中间
        mIcon = Bitmap.createScaledBitmap(mIcon, (int) mNodeRadius * 2, (int) mNodeRadius * 2, false);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = 8;
            mOffsetTop = 8;
        }
        outRect.left = (int) mOffsetLeft;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(view);

            float dividerLeft = parent.getPaddingLeft();
            float dividerTop;
            //第一个ItemView 没有上方分割线
            if (index == 0) {
                dividerTop = view.getTop();
            } else {
                dividerTop = view.getTop() - mOffsetTop;
            }
//            float dividerRight = parent.getWidth() - parent.getPaddingRight();
            float dividerBottom = view.getBottom();

            // itemView左侧空白区的中心点坐标
            float centerX = dividerLeft + mOffsetLeft / 2;
            float centerY;
            // 处理第一个itemView顶部没有分割线
            if (index == 0) {
                centerY = dividerTop + (dividerBottom - dividerTop) / 2;
            } else {
                centerY = dividerTop + mOffsetTop + (dividerBottom - (dividerTop + mOffsetTop)) / 2;
            }

            // 中心图上半部分轴线坐标
            float upLineTopX = centerX;
            float upLineTopY = dividerTop;
            float upLineBottomX = centerX;
            float upLineBottomY = centerY - mNodeRadius;

            //绘制上半部轴线
            canvas.drawLine(upLineTopX, upLineTopY, upLineBottomX, upLineBottomY, mPaint);
            // 绘制圆形作为时间轴的节点
//            mPaint.setStyle(Paint.Style.STROKE); // 空心圆
            canvas.drawCircle(centerX, centerY, mNodeRadius, mPaint);

            // 绘制图片作为时间轴的节点
//            canvas.drawBitmap(mIcon, centerX - mNodeRadius, centerY - mNodeRadius, mPaint);

            // 中心图下半部分轴线坐标
            float downLineTopX = centerX;
            float downLineTopY = centerY + mNodeRadius;
            float downLineBottomX = centerX;
            float downLineBottomY = dividerBottom;

            //绘制下半部轴线
            canvas.drawLine(downLineTopX, downLineTopY, downLineBottomX, downLineBottomY, mPaint);
        }
    }
}
