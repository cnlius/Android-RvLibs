package com.jason.app;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.jason.app.databinding.ActivityGridListBinding;

/**
 * Created by liusong on 2018/3/30.
 */

public class GridListActivity extends AppCompatActivity {

    private ActivityGridListBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_grid_list);
    }
}
