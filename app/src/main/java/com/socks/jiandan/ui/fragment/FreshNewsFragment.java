package com.socks.jiandan.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.socks.jiandan.R;
import com.socks.jiandan.adapter.FreshNewsAdapter;
import com.socks.jiandan.base.BaseFragment;
import com.socks.jiandan.base.ConstantString;
import com.socks.jiandan.callback.LoadMoreListener;
import com.socks.jiandan.callback.LoadResultCallBack;
import com.socks.jiandan.utils.ShowToast;
import com.socks.jiandan.view.AutoLoadRecyclerView;
import com.victor.loading.rotate.RotateLoading;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * 首次加载，使用RotateLoading来显示加载等待
 * 下拉刷新使用SwipeRefreshLayout的setOnRefreshListener处理
 * 加载更多是通过AutoLoadRecyclerView来实现
 */
public class FreshNewsFragment extends BaseFragment implements LoadResultCallBack {

    @InjectView(R.id.recycler_view)
    AutoLoadRecyclerView mRecyclerView;
    @InjectView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @InjectView(R.id.loading)
    RotateLoading loading;

    private FreshNewsAdapter mAdapter;

    public FreshNewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定onCreateOptionsMenu方法加载的菜单
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_load, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * 如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
         * 因为新鲜事的大小是不固定的，所以这里设置为false
         */
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void loadMore() {
                //数据加载过程放在mAdapter中，可以更好的实现解耦
                mAdapter.loadNextPage();
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mAdapter.loadFirst();
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setOnPauseListenerParams(false, true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isLargeMode = sp.getBoolean(SettingFragment.ENABLE_FRESH_BIG, true);

        mAdapter = new FreshNewsAdapter(getActivity(), mRecyclerView, this, isLargeMode);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.loadFirst();
        loading.start();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            mSwipeRefreshLayout.setRefreshing(true);
            mAdapter.loadFirst();
            return true;
        }
        return false;
    }

    /**
     * adapter中成功加载数据回调，无论是加载本地数据，还是网络数据
     * 不管是下拉刷新，还是首次加载数据，都让RotateLoading停止
     * 并且让SwipeRefreshLayout的刷新动作停止
     * @param result
     * @param object
     */
    @Override
    public void onSuccess(int result, Object object) {
        loading.stop();
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * adapter中成功加载数据回调，无论是加载本地数据，还是网络数据
     * 不管是下拉刷新，还是首次加载数据，都让RotateLoading停止
     * 并且让SwipeRefreshLayout的刷新动作停止
     * @param result
     * @param object
     */
    @Override
    public void onError(int code, String msg) {
        loading.stop();
        ShowToast.Short(ConstantString.LOAD_FAILED);
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}