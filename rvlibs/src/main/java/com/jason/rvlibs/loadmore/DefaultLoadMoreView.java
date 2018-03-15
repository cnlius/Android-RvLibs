package com.jason.rvlibs.loadmore;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import com.jason.rvlibs.R;
import com.jason.rvlibs.databinding.DefaultLoadingBinding;

/**
 * Created by liusong on 2018/1/2.
 */
public class DefaultLoadMoreView extends LoadMoreView {
    private DefaultLoadingBinding mBinding;

    public DefaultLoadMoreView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
//        inflate(context, R.layout.default_loading, this); //或者addView()
        View view = LayoutInflater.from(context).inflate(R.layout.default_loading, this, false);
        mBinding = DataBindingUtil.bind(view);
        addView(view);
    }

    @Override
    public void onLoading() {
        mBinding.text.setText("正在加载...");
    }

    @Override
    public void onLoaded() {
        mBinding.progress.setVisibility(GONE);
    }

    @Override
    public void onLoadedAll() {
        mBinding.text.setText("已加载完毕");
        mBinding.progress.setVisibility(GONE);
    }

    @Override
    public void onReset() {
        mBinding.progress.setVisibility(VISIBLE);
    }

}
