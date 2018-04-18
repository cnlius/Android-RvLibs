package com.jason.rvlibs;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
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
public class FreeHFAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    private static final String PAY_LOAD = "PAY_LOAD"; // 局部刷新标记
    private static final int VIEW_TYPE_EMPTY = 0x2710; // 空view的类型(10000)
    private static final int VIEW_TYPE_HEADER = 0x186A0; // 头部view类型(100000)
    private static final int VIEW_TYPE_FOOTER = 0x30D40; // 尾部view类型(200000)
    private static final int VIEW_TYPE_LOADING = 0x87A23; // 加载view的类型(555555)

    private List<T> dataSet = new ArrayList<>();
    private int itemLayoutId; // 默认item的layout
    private int pageSize = 10; // 每页条目数
    private boolean isLoadEnabled; // 加载时置为不可用的标识
    private int headerNum = 0; // 已有header view数量(限制100000个)
    private int footerNum = 0; // 已有footer view数量(限制100000个)
    // SparseArray删除某条key对应数据时，此key之后的所有数据对应的索引都前移一个单位,末尾索引对应的key缓存，但是无value;
    private SparseArray<View> mHeaderViews = new SparseArray<>();
    private SparseArray<View> mFootViews = new SparseArray<>();
    private MultiTypeItemSupport multiTypeItemSupport; // 多类型item支持

    private OnDataBindListener onDataBindListener;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnItemPartUpdateListener onItemPartUpdateListener;
    private OnLoadMoreListener onLoadMoreListener;

    private View emptyView; // 空数据
    private LoadMoreView loadMoreView; //加载更多的view
    private boolean isAlwaysShowLoadAll; // 每次加载完成后是否一直显示加载完毕

    public FreeHFAdapter() {
    }

    public FreeHFAdapter(int itemLayoutId) {
        this.itemLayoutId = itemLayoutId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        if (isEmptyView()) { // 空数据
            return new ViewHolder(emptyView, false);
        } else if (mHeaderViews.get(viewType) != null) { // 头部
            return new ViewHolder(mHeaderViews.get(viewType), false);
        } else if (mFootViews.get(viewType) != null) { // 底部
            return new ViewHolder(mFootViews.get(viewType), false);
        } else if (viewType == VIEW_TYPE_LOADING) { // 加载更多
            return new ViewHolder(loadMoreView, false);
        } else { // 数据item
            if (multiTypeItemSupport != null) {
                View view = LayoutInflater.from(context).inflate(multiTypeItemSupport.getItemLayoutId(viewType), parent, false);
                return new ViewHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(itemLayoutId, parent, false);
                return new ViewHolder(view);
            }
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (isOtherView(position)) return;
        // dataBinding设置数据到布局中的item
        ViewDataBinding binding = holder.getBinding();
        binding.setVariable(BR.item, dataSet.get(getDataItemPosition(position)));
        binding.executePendingBindings();
        // 事件监听
        initItemViewListener(holder, getDataItemPosition(position));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
        if (isOtherView(position)) return;
        // 根据payloads不为空，设置局部更新监听
        if (onItemPartUpdateListener != null && payloads != null && !payloads.isEmpty()) {
            onItemPartUpdateListener.onItemPartUpdate(holder, position - getHeaderCount());
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isEmptyView()) {
            return VIEW_TYPE_EMPTY;
        } else if (isHeaderView(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterView(position)) {
            return mFootViews.keyAt(position - getHeaderCount() - dataSet.size());
        } else if (isLoadMoreView(position)) {
            return VIEW_TYPE_LOADING;
        } else { // 数据类型的item
            if (multiTypeItemSupport != null) {
                return multiTypeItemSupport.getItemViewType(getDataItemPosition(position));
            } else {
                return super.getItemViewType(getDataItemPosition(position));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (isEmptyView()) {
            return 1;
        } else {
            return getAllItemCount();
        }
    }

    /**
     * 获取adapter的所有item数量
     *
     * @return
     */
    private int getAllItemCount() {
        return getHeaderCount() + dataSet.size() + getFooterCount();
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isOtherView(holder.getLayoutPosition())) {
            compatStaggeredGridLayoutManager(holder);
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

    /**
     * 是否应该显示空数据view
     *
     * @return
     */
    private boolean isEmptyView() {
        return emptyView != null && getHeaderCount() == 0 && dataSet.isEmpty() && getFooterCount() == 0;
    }

    public int getHeaderCount() {
        return mHeaderViews.size();
    }

    public int getFooterCount() {
        return mFootViews.size();
    }

    public FreeHFAdapter<T> setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        return this;
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
        if (isEmptyView() || isHeaderView(position)
                || isFooterView(position) || isLoadMoreView(position)) {
            return true;
        }
        return false;
    }

    public List<T> getData() {
        return dataSet;
    }

    public FreeHFAdapter<T> addData(List<T> data) {
        if (data != null && !data.isEmpty()) {
            if (isEmptyView()) { // 空数据
                dataSet.addAll(data);
                // 上个状态是显示空view时，需要全部重刷；
                notifyDataSetChanged();
            } else if (isLoadMoreView(getHeaderCount() + dataSet.size() - 1)) { // 加载更多
                int oldSize = dataSet.size();
                dataSet.addAll(oldSize - 1, data);
                notifyItemRangeInserted(getHeaderCount() + oldSize - 1, data.size());
            } else {
                int oldSize = dataSet.size();
                dataSet.addAll(data);
                notifyItemRangeInserted(getHeaderCount() + oldSize, data.size());
            }
        }
        return this;
    }

    /**
     * 是否是loadMoreView
     *
     * @param position adapter的position
     * @return loadMoreView是dataItem的最后一项
     */
    private boolean isLoadMoreView(int position) {
        return onLoadMoreListener != null && position == getHeaderCount() + dataSet.size() - 1
                && dataSet.size() > 0 && dataSet.get(getDataItemPosition(position)) == null;
    }

    /**
     * 获取数据item在dataSet中的索引
     *
     * @param position adapter的position
     * @return
     */
    private int getDataItemPosition(int position) {
        return position - getHeaderCount();
    }

    public void clearData() {
        if (dataSet.isEmpty()) return;
        dataSet.clear();
        notifyDataSetChanged();
    }

    public FreeHFAdapter<T> addHeaderView(View view) {
        return addHeaderView(null, view);
    }

    /**
     * 添加头部view,重复添加相同的对象无效
     *
     * @param recyclerView 不为null时，滑动到添加的header
     * @param view         headerView
     */
    public FreeHFAdapter<T> addHeaderView(RecyclerView recyclerView, View view) {
        try {
            if (view == null) return this;
            // view在集合中的索引
            int index = mHeaderViews.indexOfValue(view);
            if (index < 0) {
                if (isEmptyView()) {
                    mHeaderViews.put(VIEW_TYPE_HEADER + headerNum, view);
                    headerNum++;
                    notifyDataSetChanged();
                } else {
                    mHeaderViews.put(VIEW_TYPE_HEADER + headerNum, view);
                    headerNum++;
                    notifyItemInserted(getHeaderCount() - 1);
                    if (recyclerView != null && getItemCount() > 0) {
                        recyclerView.smoothScrollToPosition(getHeaderCount() - 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public void updateHeaderView(View view) {
        int index = mHeaderViews.indexOfValue(view);
        if (index >= 0) {
            notifyItemChanged(index, PAY_LOAD);
        }
    }

    public void removeHeaderView(View view) {
        if (view == null) return;
        int index = mHeaderViews.indexOfValue(view);
        if (index < 0) return;
        mHeaderViews.removeAt(index);
        notifyDataSetChanged();
    }

    /**
     * 如果用notifyXXRemoved清除，动画没结束前进行添加会报错
     */
    public void clearHeaderView() {
        if (mHeaderViews.size() == 0) return;
        mHeaderViews.clear();
        notifyDataSetChanged();
    }

    private boolean isHeaderView(int position) {
        if (isEmptyView()) { //header 上次显示的可能是空数据
            return false;
        }
        return position < getHeaderCount();
    }

    public FreeHFAdapter<T> addFooterView(View view) {
        return addFooterView(null, view);
    }

    /**
     * @param recyclerView null时不自动滑动到底部
     */
    public FreeHFAdapter<T> addFooterView(RecyclerView recyclerView, View view) {
        if (getAllItemCount() > 0 && isLoadMoreView(getAllItemCount() - 1)) {
            throw new IllegalStateException("can't add footer when there is load more view");
        }
        if (view == null) return this;
        int index = mFootViews.indexOfValue(view);
        if (index < 0) {
            if (isEmptyView()) {
                mFootViews.put(VIEW_TYPE_FOOTER + footerNum, view);
                footerNum++;
                notifyDataSetChanged();
            } else {
                mFootViews.put(VIEW_TYPE_FOOTER + footerNum, view);
                footerNum++;
                notifyItemInserted(getAllItemCount() - 1);
                if (recyclerView != null && getAllItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(getAllItemCount() - 1);
                }
            }
        }
        return this;
    }

    public void updateFooterView(View view) {
        int index = mFootViews.indexOfValue(view);
        if (index >= 0) {
            notifyItemChanged(getHeaderCount() + dataSet.size() + index, PAY_LOAD);
        }
    }

    public void removeFooterView(View view) {
        if (view == null) return;
        int index = mFootViews.indexOfValue(view);
        if (index < 0) return;
        mFootViews.removeAt(index);
        notifyDataSetChanged();
    }

    public void clearFooterView() {
        if (mFootViews.size() == 0) return;
        mFootViews.clear();
        notifyDataSetChanged();
    }

    private boolean isFooterView(int position) {
        if (isEmptyView()) {
            return false;
        }
        return position >= getHeaderCount() + dataSet.size();
    }

    public void updateItemPart(int position) {
        notifyItemChanged(getHeaderCount() + position, PAY_LOAD);
    }

    public void updateItemPartRange(int positionStart, int itemCount) {
        notifyItemRangeChanged(getHeaderCount() + positionStart, itemCount, PAY_LOAD);
    }

    public FreeHFAdapter<T> bindRecyclerView(RecyclerView recyclerView) {
        recyclerView.setAdapter(this);
        return this;
    }

    public FreeHFAdapter<T> setItemLayoutId(int itemLayoutId) {
        this.itemLayoutId = itemLayoutId;
        return this;
    }

    public FreeHFAdapter<T> addOnDataBindListener(OnDataBindListener onDataBindListener) {
        this.onDataBindListener = onDataBindListener;
        return this;
    }

    public FreeHFAdapter<T> addOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public FreeHFAdapter<T> addOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
        return this;
    }

    public FreeHFAdapter<T> addOnItemPartUpdateListener(OnItemPartUpdateListener onItemPartUpdateListener) {
        this.onItemPartUpdateListener = onItemPartUpdateListener;
        return this;
    }

    public FreeHFAdapter<T> setLoadMoreView(LoadMoreView loadMoreView) {
        this.loadMoreView = loadMoreView;
        return this;
    }

    public void setLoadEnabled(boolean loadEnabled) {
        isLoadEnabled = loadEnabled;
    }

    /**
     * 包含footer时禁止上拉加载动作
     */
    public FreeHFAdapter<T> addOnLoadMoreListener(final RecyclerView recyclerView, final OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
        isLoadEnabled = true;
        if (loadMoreView == null) {
            loadMoreView = new DefaultLoadMoreView(recyclerView.getContext());
        }
        recyclerView.addOnScrollListener(new OnScrollFooterListener() {
            @Override
            public void onBottom() {
                if (getFooterCount() == 0 && isLoadEnabled && dataSet.size() >= pageSize) {
                    // 加载开始时的状态
                    loadMoreView.onReset();
                    loadMoreView.onLoading();

                    isLoadEnabled = false;
                    dataSet.add(null); // 加入一个null代表load item
                    int position = getHeaderCount() + dataSet.size() - 1;
                    notifyItemInserted(position);
                    recyclerView.smoothScrollToPosition(position);
                    onLoadMoreListener.onLoadMore();
                }
            }
        });
        return this;
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
                if (getFooterCount() == 0 && isAlwaysShowLoadAll) {
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

    public int getPageSize() {
        return pageSize;
    }

    public FreeHFAdapter<T> setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    /**
     * 完成加载更多
     */
    public void loaded() {
        if (onLoadMoreListener == null) return;
        loadMoreView.onLoaded();
        resetLoad();
    }

    /**
     * 重置加载更多
     * <p>
     * 刷新时，必须在clear之前调用
     */
    public void resetLoad() {
        if (onLoadMoreListener == null) return;
        isLoadEnabled = true;
        loadMoreView.onReset();
        if (dataSet.size() > 0 && dataSet.get(dataSet.size() - 1) == null) {
            dataSet.remove(dataSet.size() - 1);
            notifyItemRemoved(getHeaderCount() + dataSet.size() - 1);
        }
    }

    /**
     * 完成所有数据的加载
     */
    public void loadedAll() {
        if (onLoadMoreListener == null) return;
        isLoadEnabled = false;
        loadMoreView.onLoadedAll();
    }

    /**
     * 每次刷新结束判断是否加载完毕
     *
     * @param isAlways
     */
    public FreeHFAdapter<T> setAlwaysShowLoadAll(boolean isAlways) {
        this.isAlwaysShowLoadAll = isAlways;
        return this;
    }

    public FreeHFAdapter<T> addMultiItemSupport(MultiTypeItemSupport multiItemSupport) {
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
