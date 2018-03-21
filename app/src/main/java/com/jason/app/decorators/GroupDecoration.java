package com.jason.app.decorators;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.View;

import com.jason.app.R;
import com.jason.app.vo.GroupInfo;

/**
 * Created by frank on 2017/4/11.
 */
public class GroupDecoration extends RecyclerView.ItemDecoration {
    private GroupInfoCallback mCallback; // 外部处理分组对象，由外部处理
    private int mHeaderHeight;
    private int mDividerHeight;

    //用来绘制Header上的文字
    private TextPaint mTextPaint; // header文字的画笔
    private Paint mPaint; // header矩形的画笔
    private float mTextSize;
    private Paint.FontMetrics mFontMetrics; // 字体测量
    private float mTextOffsetX = 0; // 文字x轴偏移量

    public GroupDecoration(Context context, GroupInfoCallback callback) {
        this.mCallback = callback;
        // 默认的header内容设置
        mDividerHeight = context.getResources().getDimensionPixelOffset(R.dimen.header_divider_height);
        mHeaderHeight = context.getResources().getDimensionPixelOffset(R.dimen.header_height);
        mTextSize = context.getResources().getDimensionPixelOffset(R.dimen.header_text_size);

        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(mTextSize);
        mFontMetrics = mTextPaint.getFontMetrics();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.YELLOW);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // 获取itemView的真实索引
        int position = parent.getChildAdapterPosition(view);
        if (mCallback != null) {
            // 获取对应位置item实体的组对象
            GroupInfo groupInfo = mCallback.getGroupInfo(position);
            // 如果是组内的第一个则将间距撑开为一个Header的高度，或者就是普通的分割线高度
            if (groupInfo != null && groupInfo.isFirstViewInGroup()) {
                outRect.top = mHeaderHeight;
            } else {
                outRect.top = mDividerHeight;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(canvas, parent, state);
        // 遍历所有itemView
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            // 获取itemView的真实索引
            int index = parent.getChildAdapterPosition(view);
            if (mCallback != null) {
                GroupInfo groupInfo = mCallback.getGroupInfo(index);
                // 只有组内的第一个ItemView之上才绘制
                if (groupInfo.isFirstViewInGroup()) {
                    // header的坐标
                    int left = parent.getPaddingLeft();
                    int top = view.getTop() - mHeaderHeight;
                    int right = parent.getWidth() - parent.getPaddingRight();
                    int bottom = view.getTop();
                    // 绘制Header
                    canvas.drawRect(left, top, right, bottom, mPaint);
                    // 文字基线坐标
                    float titleX = left + mTextOffsetX;
                    float titleY = bottom - mFontMetrics.descent; // descent:文字baseline之下至字符最低处的距离
                    // 绘制Title
                    canvas.drawText(groupInfo.getTitle(), titleX, titleY, mTextPaint);
                }
            }
        }
    }

    public void setCallback(GroupInfoCallback callback) {
        this.mCallback = callback;
    }

    public interface GroupInfoCallback {
        GroupInfo getGroupInfo(int position);
    }
}
