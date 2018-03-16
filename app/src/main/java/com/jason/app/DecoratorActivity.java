package com.jason.app;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jason.app.databinding.ActivityDecoratorBinding;
import com.jason.app.decorators.SpaceDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liusong on 2018/3/16.
 */

public class DecoratorActivity extends AppCompatActivity {

    private ActivityDecoratorBinding mBinding;
    private List<String> list = new ArrayList<>();
    private TestAdapter testAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_decorator);
        initView();
        initData();
    }

    private void initView() {
        mBinding.rv.addItemDecoration(new SpaceDecoration());

        testAdapter = new TestAdapter();
        testAdapter.addData(list);
        mBinding.rv.setAdapter(testAdapter);
    }

    private void initData() {
        list.addAll(DataUtils.createStringList(5));
        testAdapter.notifyDataSetChanged();
    }


    public class TestAdapter extends RecyclerView.Adapter<TestAdapter.TestViewHolder> {

        private List<String> data = new ArrayList<>();

        public void addData(List<String> data) {
            this.data = data;
        }

        @Override
        public TestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_test, parent, false);
            return new TestViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TestViewHolder holder, int position) {
            TextView textView = holder.itemView.findViewById(R.id.tv_text);
            textView.setText(data.get(position));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class TestViewHolder extends RecyclerView.ViewHolder {

            public TestViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
