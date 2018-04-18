package com.jason.rvlibs;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jason.rvlibs.loadmore.DefaultLoadMoreView;
import com.jason.rvlibs.loadmore.LoadMoreView;

import java.util.ArrayList;
import java.util.List;

/**
 * 公共的adapter
 * Created by liusong on 2017/12/8.
 */
public class CommonAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    // 常量
    private static final String PAY_LOAD = "PAY_LOAD"; // 局部刷新标记
    private static final int VIEW_TYPE_EMPTY = 0x2710; // 空view的类型(10000)
    private static final int VIEW_TYPE_LOADING = 0xD903; // 加载view的类型(55555)
    // 成员变量
    private List<T> dataSet = new ArrayList<>();
    private int itemLayoutId; // 默认item的layout
    private int pageSize = 10; // 每页条目数
    private boolean isLoadEnabled; // 加载时置为不可用的标识
    private MultiTypeItemSupport multiTypeItemSupport; // 多类型item支持

    // 监听事件绑定
    private OnDataBindListener onDataBindListener;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemPartUpdateListener onItemPartUpdateListener;
    private OnLoadMoreListener onLoadMoreListener;

    // view
    private View emptyView; // 空数据
    private LoadMoreView loadMoreView; //加载更多的view

    public CommonAdapter() {
    }

    public CommonAdapter(int itemLayoutId) {
        this.itemLayoutId = itemLayoutId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (isEmptyView()) {
            return new ViewHolder(emptyView, false);
        } else if (viewType == VIEW_TYPE_LOADING) {
            return new ViewHolder(loadMoreView, false);
        } else if (multiTypeItemSupport != null) {
            View view = LayoutInflater.from(context).inflate(multiTypeItemSupport.getItemLayoutId(viewType), parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(itemLayoutId, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isOtherView(position)) return;
        // dataBinding设置数据到布局中的item
        ViewDataBinding binding = holder.getBinding();
        binding.setVariable(BR.item, dataSet.get(position));
        binding.executePendingBindings();
        // 事件监听
        initItemViewListener(holder, position);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (isOtherView(position)) return;
        // 根据payloads不为空，设置局部更新监听
        if (onItemPartUpdateListener != null && payloads != null && !payloads.isEmpty()) {
            onItemPartUpdateListener.onItemPartUpdate(holder, position);
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    private void initItemViewListener(ViewHolder holder, final int position) {
        if (onDataBindListener != null) {
            onDataBindListener.onDataBind(holder, position);
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(position);
                }
            });
        }

        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemLongClickListener.onItemLongClick(position);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (isEmptyView()) {
            return 1;
        } else {
            return dataSet.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isEmptyView()) {
            return VIEW_TYPE_EMPTY;
        } else if (isLoadMoreView(position)) {
            return VIEW_TYPE_LOADING;
        } else if (multiTypeItemSupport != null) {
            return multiTypeItemSupport.getItemViewType(position);
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        // item之外的其它view兼容跨整行
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            compatGridLayoutManager(layoutManager);
        }
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isOtherView(holder.getLayoutPosition())) {
            compatStaggeredGridLayoutManager(holder);
        }
    }

    /**
     * item占整满整行时，兼容gridLayoutManager
     *
     * @param gridLayoutManager layoutManager
     */
    private void compatGridLayoutManager(final GridLayoutManager gridLayoutManager) {
        // 记录原始跨度对象
        final GridLayoutManager.SpanSizeLookup oldLookup = gridLayoutManager.getSpanSizeLookup();
        // gridLayoutManager.getSpanCount()=gridLayoutManager的列数
        gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());

        // 回调设置item的跨度（即表格中的某列跨几列）
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) { //返回单元格跨度
                if (isOtherView(position)) {
                    return gridLayoutManager.getSpanCount();
                } else { // 不需要兼容时，返回原始跨度
                    if (oldLookup != null) {
                        return oldLookup.getSpanSize(position);
                    } else {
                        return 1;
                    }
                }
            }
        });
    }

    /**
     * item占整满整行时,兼容StaggeredGridLayoutManager
     * StaggeredGridLayoutManager并没有setSpanSizeLookup方法
     * 设置单元格跨度占据整行
     *
     * @param holder
     */
    private void compatStaggeredGridLayoutManager(RecyclerView.ViewHolder holder) {
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    /**
     * 是否是非item
     *
     * @param position
     * @return
     */
    private boolean isOtherView(int position) {
        if (isEmptyView() || isLoadMoreView(position)) {
            return true;
        }
        return false;
    }

    private boolean isLoadMoreView(int position) {
        return onLoadMoreListener != null && position == dataSet.size() - 1 && dataSet.size() > 0 && dataSet.get(position) == null;
    }

    public List<T> getData() {
        return dataSet;
    }

    public CommonAdapter<T> addData(List<T> data) {
        if (data != null && !data.isEmpty()) {
            if (isEmptyView()) { // 空数据
                dataSet.addAll(data);
                // 上个状态是显示空view时，需要全部重刷；
                notifyDataSetChanged();
            } else if (isLoadMoreView(dataSet.size() - 1)) { // 加载更多
                int oldSize = dataSet.size();
                dataSet.addAll(oldSize - 1, data);
                notifyItemRangeInserted(oldSize - 1, data.size());
            } else {
                int oldSize = dataSet.size();
                dataSet.addAll(data);
                notifyItemRangeInserted(oldSize, data.size());
            }
        }
        return this;
    }

    public void clearData() {
        if (dataSet.isEmpty()) return;
        dataSet.clear();
        notifyDataSetChanged();
    }

    /**
     * 是否应该显示空数据view
     *
     * @return
     */
    private boolean isEmptyView() {
        return emptyView != null && dataSet.isEmpty();
    }

    public void updateItemPart(int position) {
        notifyItemChanged(position, PAY_LOAD);
    }

    public void updateItemPartRange(int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount, PAY_LOAD);
    }

    public CommonAdapter<T> bindRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        return this;
    }

    public CommonAdapter<T> setItemLayoutId(int itemLayoutId) {
        this.itemLayoutId = itemLayoutId;
        return this;
    }

    public CommonAdapter<T> addOnDataBindListener(OnDataBindListener onDataBindListener) {
        this.onDataBindListener = onDataBindListener;
        return this;
    }

    public CommonAdapter<T> addOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public CommonAdapter<T> addOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

    public CommonAdapter<T> addOnItemPartUpdateListener(OnItemPartUpdateListener onItemPartUpdateListener) {
        this.onItemPartUpdateListener = onItemPartUpdateListener;
        return this;
    }

    public CommonAdapter<T> setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        return this;
    }

    public CommonAdapter<T> setLoadMoreView(LoadMoreView loadMoreView) {
        this.loadMoreView = loadMoreView;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public CommonAdapter<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public void setLoadEnabled(boolean loadEnabled) {
        isLoadEnabled = loadEnabled;
    }

    public CommonAdapter<T> addOnLoadMoreListener(final RecyclerView recyclerView, final OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        isLoadEnabled = true;
        if (loadMoreView == null) {
            loadMoreView = new DefaultLoadMoreView(recyclerView.getContext());
        }
        recyclerView.addOnScrollListener(new OnScrollFooterListener(){
            @Override
            public void onBottom() {
                if (isLoadEnabled && dataSet.size() >= pageSize) {
                    // 加载开始时的状态
                    loadMoreView.onReset();
                    loadMoreView.onLoading();

                    isLoadEnabled = false;
                    dataSet.add(null); // 加入一个null代表load item
                    int position = dataSet.size() - 1;
                    notifyItemInserted(position);
                    recyclerView.smoothScrollToPosition(position);
                    onLoadMoreListener.onLoadMore();
                }
            }
        });
        return this;
    }

    /**
     * 完成加载更多
     */
    public void loaded() {
        loadMoreView.onLoaded();
        resetLoad();
    }

    /**
     * 重置加载更多
     * <p>
     * 刷新时，必须在clear之前调用
     */
    public void resetLoad() {
        isLoadEnabled = true;
        loadMoreView.onReset();
        if (dataSet.size() > 0 && dataSet.get(dataSet.size() - 1) == null) {
            dataSet.remove(dataSet.size() - 1);
            notifyItemRemoved(dataSet.size() - 1);
        }
    }

    /**
     * 完成所有数据的加载
     */
    public void loadedAll() {
        isLoadEnabled = false;
        loadMoreView.onLoadedAll();
    }

    /**
     * 处理刷新和加载后加载状态(必须在加载后调用)
     *
     * @param isRefresh   是否是刷新
     * @param addDataSize 新增的数据个数
     */
    public void handleLoadStatus(boolean isRefresh, int addDataSize) {
        if (onLoadMoreListener == null) return;
        if (isRefresh) { // 刷新
            if (addDataSize == getPageSize()) {
                isLoadEnabled = true;
            } else {
                isLoadEnabled = false;
                if (isAlwaysShowLoadAll) {
                    dataSet.add(null); // 加入一个null代表load item
                    loadedAll();
                    notifyDataSetChanged();
                }
            }
        } else { // 加载
            if (addDataSize == getPageSize()) {
                loaded();
            } else {
                loadedAll();
            }
        }
    }

    private boolean isAlwaysShowLoadAll; // 每次加载完成后是否一直显示加载完毕

    /**
     * 每次刷新结束判断是否加载完毕
     *
     * @param isAlways
     */
    public CommonAdapter<T> setAlwaysShowLoadAll(boolean isAlways) {
        this.isAlwaysShowLoadAll = isAlways;
        return this;
    }

    public CommonAdapter<T> addMultiItemSupport(MultiTypeItemSupport multiItemSupport) {
        this.multiTypeItemSupport = multiItemSupport;
        return this;
    }

    public interface MultiTypeItemSupport {
        int getItemLayoutId(int viewType);

        int getItemViewType(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(Integer position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Integer position);
    }

    public interface OnDataBindListener {
        void onDataBind(ViewHolder holder, Integer position);
    }

    public interface OnItemPartUpdateListener {
        void onItemPartUpdate(ViewHolder holder, Integer position);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

}