package com.jason.app;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.jason.app.databinding.ActivityGridListBinding;
import com.jason.rvlibs.CommonAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liusong on 2018/3/30.
 */

public class GridListActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGridListBinding mBinding;
    private CommonAdapter<String> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_grid_list);
        initView();
    }

    private void initView() {
        mBinding.rv.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new CommonAdapter<String>(R.layout.item_grid_text)
                .bindRecyclerView(mBinding.rv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                List<String> data = new ArrayList<>();
                for (int i = 0; i < 10; i++) {
                    data.add(String.valueOf(i));
                }
                adapter.clearData();
                adapter.addData(data);
                break;
        }
    }
}
