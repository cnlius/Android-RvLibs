package com.jason.rvlibs;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * common viewHolder
 *
 * @required item layout need support dataBinding
 */
public class ViewHolder extends RecyclerView.ViewHolder {
    private ViewDataBinding mBinding;

    public ViewHolder(View itemView) {
        this(itemView,true);
    }

    public ViewHolder(View itemView,boolean isBindLayout) {
        super(itemView);
        if(isBindLayout) {
            this.mBinding = DataBindingUtil.bind(itemView);
        }
    }

    public ViewDataBinding getBinding() {
        return mBinding;
    }
}