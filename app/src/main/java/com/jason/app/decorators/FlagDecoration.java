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
 * 在itemView之上绘制角标
 * Created by frank on 2017/4/12.
 */
public class FlagDecoration extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Bitmap mIcon;
    private float mFlagLeft;

    public FlagDecoration(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);

        mIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_flag_hot);
        mFlagLeft = context.getResources().getDimension(R.dimen.flag_left);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //  super.getItemOffsets(outRect, view, parent, state);
        // 第一个ItemView不需要在上面绘制分割线
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = 0;
        } else {
            outRect.top = 2;
        }
    }

    /**
     * 在itemView之上绘制
     * @param canvas
     * @param parent
     * @param state
     */
    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int index = parent.getChildAdapterPosition(view);
            float top = view.getTop();
            if (index < 2) {
                canvas.drawBitmap(mIcon, mFlagLeft, top, mPaint);
            }
        }
    }
}
