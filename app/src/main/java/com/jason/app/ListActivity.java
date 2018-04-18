package com.jason.app;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.app.databinding.ActivityListBinding;
import com.jason.app.databinding.ItemImageTextBinding;
import com.jason.app.databinding.ItemTextTestBinding;
import com.jason.rvlibs.FreeHFAdapter;
import com.jason.rvlibs.ViewHolder;

import java.util.List;

public class ListActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, FreeHFAdapter.OnLoadMoreListener {

    private ActivityListBinding mBinding;
    private FreeHFAdapter<String> adapter;
    private static final int ITEM_TYPE_TEXT = 0x1;
    private static final int ITEM_TYPE_IMAGE_TEXT = 0x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_list);
        initView();
        initData();
    }

    private TextView headerRed, headerGreen, headerYellow, footerCyan, footerDkgray;

    private void initView() {
        // --头部------------------------
        headerRed = (TextView) LayoutInflater.from(this).inflate(R.layout.item_text_test, mBinding.rv, false);
        headerRed.setBackgroundColor(Color.RED);
        headerRed.setText(String.valueOf("头部-red"));
        headerRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                textView.setText("头部-red发生了点击");
                textView.setBackgroundColor(Color.BLUE);
            }
        });

        headerGreen = (TextView) LayoutInflater.from(this).inflate(R.layout.item_text_test, mBinding.rv, false);
        headerGreen.setBackgroundColor(Color.GREEN);
        headerGreen.setText(String.valueOf("头部-green"));
        headerGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                textView.setText("头部-green发生了点击");
                textView.setBackgroundColor(Color.BLUE);
            }
        });

        headerYellow = (TextView) LayoutInflater.from(this).inflate(R.layout.item_text_test, mBinding.rv, false);
        headerYellow.setBackgroundColor(Color.YELLOW);
        headerYellow.setText(String.valueOf("头部-yellow"));
        headerYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                textView.setText("头部-yellow发生了点击");
                textView.setBackgroundColor(Color.BLUE);
            }
        });

        footerCyan = (TextView) LayoutInflater.from(this).inflate(R.layout.item_text_test, mBinding.rv, false);
        footerCyan.setBackgroundColor(Color.CYAN);
        footerCyan.setText(String.valueOf("底部-cyan"));
        footerCyan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                textView.setText("底部-cyan发生了点击");
                textView.setBackgroundColor(Color.BLUE);
            }
        });

        footerDkgray = (TextView) LayoutInflater.from(this).inflate(R.layout.item_text_test, mBinding.rv, false);
        footerDkgray.setBackgroundColor(Color.DKGRAY);
        footerDkgray.setText(String.valueOf("底部-dkgray"));
        footerDkgray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) v;
                textView.setText("底部-dkgray发生了点击");
                textView.setBackgroundColor(Color.BLUE);
            }
        });
        // --------------------------
        mBinding.refresh.setOnRefreshListener(this);
        // --------------------------
        mBinding.rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        // recyclerView的宽高固定，adapter的改变不会影响recyclerView宽高时，调用如下方法，可以提高性能，避免过度绘制；
        mBinding.rv.setHasFixedSize(true);
        // --------------------------
        View emptyView = LayoutInflater.from(this).inflate(R.layout.layout_no_data, mBinding.rv, false);
        adapter = new FreeHFAdapter<String>()
                .setPageSize(5)
//                .setAlwaysShowLoadAll(true)
//                .addHeaderView(headerRed)
//                .addFooterView(footerCyan)
                .setItemLayoutId(R.layout.item_text_test)
//                .addData(DataUtils.createStringList(3))
                .setEmptyView(emptyView)
                .addMultiItemSupport(new FreeHFAdapter.MultiTypeItemSupport() {
                    @Override
                    public int getItemLayoutId(int viewType) {
                        if (viewType == ITEM_TYPE_TEXT) {
                            return R.layout.item_text_test;
                        } else {
                            return R.layout.item_image_text;
                        }
                    }

                    @Override
                    public int getItemViewType(int position) {
                        if (position % 2 == 0) {
                            return ITEM_TYPE_TEXT;
                        } else {
                            return ITEM_TYPE_IMAGE_TEXT;
                        }
                    }
                })
                .addOnDataBindListener(new FreeHFAdapter.OnDataBindListener() {
                    @Override
                    public void onDataBind(ViewHolder holder, Integer position) {
                        if (holder.getBinding() instanceof ItemTextTestBinding) {
                            ItemTextTestBinding binding = (ItemTextTestBinding) holder.getBinding();
                            String content = binding.tvText.getText().toString();
                            binding.tvText.setText(String.format("change %s", content));
                        } else if (holder.getBinding() instanceof ItemImageTextBinding) {
                            ItemImageTextBinding binding = (ItemImageTextBinding) holder.getBinding();
                            String content = binding.tvText.getText().toString();
                            binding.tvText.setText(String.format("change %s", content));
                        }
                    }
                })
                .addOnItemPartUpdateListener(new FreeHFAdapter.OnItemPartUpdateListener() {
                    @Override
                    public void onItemPartUpdate(ViewHolder holder, Integer position) {
                        if (holder.getBinding() instanceof ItemTextTestBinding) {
                            ItemTextTestBinding binding = (ItemTextTestBinding) holder.getBinding();
                            binding.tvText.setText(String.valueOf(position + "位置，发生了局部更新"));
                        } else if (holder.getBinding() instanceof ItemImageTextBinding) {
                            ItemImageTextBinding binding = (ItemImageTextBinding) holder.getBinding();
                            binding.tvText.setText(String.valueOf(position + "位置，发生了局部更新"));
                        }
                    }
                })
                .addOnItemClickListener(new FreeHFAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Integer position) {
                        Toast.makeText(ListActivity.this, String.valueOf("点击了" + position), Toast.LENGTH_SHORT).show();
                        // 触发单个item局部更新
                        adapter.updateItemPart(position);
                    }
                })
                .addOnItemLongClickListener(new FreeHFAdapter.OnItemLongClickListener() {
                    @Override
                    public void onItemLongClick(Integer position) {
                        Toast.makeText(ListActivity.this, String.valueOf("长按了" + position), Toast.LENGTH_SHORT).show();
                        if (position < adapter.getData().size() - 2) {
                            // 触发指定范围item都发生局部更新
                            adapter.updateItemPartRange(position - 1, 2);
                        }
                    }
                })
                .addOnLoadMoreListener(mBinding.rv, this)
                .bindRecyclerView(mBinding.rv);

    }

    private void initData() {
        mBinding.refresh.post(new Runnable() {
            @Override
            public void run() {
                mBinding.refresh.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_red_header:
                headerRed.setText("新增Header");
                headerRed.setBackgroundColor(Color.RED);
                adapter.addHeaderView(headerRed);
                break;
            case R.id.btn_add_yellow_header:
                headerYellow.setText("新增Header");
                headerYellow.setBackgroundColor(Color.YELLOW);
                adapter.addHeaderView(headerYellow);
                break;
            case R.id.btn_add_green_header:
                headerGreen.setText("新增Header");
                headerGreen.setBackgroundColor(Color.GREEN);
                adapter.addHeaderView(headerGreen);
                break;
            case R.id.btn_clear_header:
                adapter.clearHeaderView();
                break;
            case R.id.btn_add_data:
                isRefresh = true;
                adapter.addData(DataUtils.createStringList(adapter.getData().size(), 1));
                adapter.handleLoadStatus(isRefresh, 1);
                break;
            case R.id.btn_clear_data:
                adapter.clearData();
                break;
            case R.id.btn_add_dyan_footer:
                footerCyan.setText("新增Footer");
                footerCyan.setBackgroundColor(Color.CYAN);
                adapter.addFooterView(mBinding.rv, footerCyan);
                break;
            case R.id.btn_add_dkgray_footer:
                footerDkgray.setText("新增Footer");
                footerDkgray.setBackgroundColor(Color.DKGRAY);
                adapter.addFooterView(mBinding.rv, footerDkgray);
                break;
            case R.id.btn_clear_footer:
                adapter.clearFooterView();
            default:
                break;
        }
    }

    private boolean isRefresh;
    private int page = 1;

    @Override
    public void onRefresh() {
        isRefresh = true;
        page = 1;
        requestData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        page++;
        requestData();
    }

    private void requestData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<String> tempData;
                if (isRefresh) { // 刷新
                    mBinding.refresh.setRefreshing(false);
                    adapter.resetLoad(); // 刷新时，必须在clear之前调用
                    if (adapter.getData().size() != 0) {
                        adapter.clearData();
                    }
                    tempData = DataUtils.createStringList(adapter.getData().size(), 3);
                } else { // 加载
                    // 加载更多时，dataSet多了一个null作为loadMoreView的占位
                    tempData = DataUtils.createStringList(adapter.getData().size() - 1, 2);
                }
//                tempData.clear();
                adapter.addData(tempData);
                adapter.handleLoadStatus(isRefresh, tempData.size());
            }
        }, 1000);

    }

}
