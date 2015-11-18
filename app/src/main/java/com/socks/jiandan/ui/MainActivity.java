package com.socks.jiandan.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.socks.jiandan.R;
import com.socks.jiandan.base.BaseActivity;
import com.socks.jiandan.base.JDApplication;
import com.socks.jiandan.model.NetWorkEvent;
import com.socks.jiandan.ui.fragment.FreshNewsFragment;
import com.socks.jiandan.ui.fragment.MainMenuFragment;
import com.socks.jiandan.utils.NetWorkUtil;
import com.socks.jiandan.utils.ShowToast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

/**
 * EventBus是一款针对Android优化的发布/订阅事件总线。主要功能是替代Intent,Handler,BroadCast在Fragment，Activity，Service，
 * 线程之间传递消息.优点是开销小，代码更优雅。
 * 以及将发送者和接收者解耦。
 * EventBus.getDefault().register(this)注册监听
 * EventBus.getDefault().unregister(this)解除监听
 * EventBus.getDefault().post(event)发送事件，也叫发布事件，通知观察者，有事件要发生了。
 * 重写EventBus的onEvent、onEventMainThread、onEventBackgroundThread、onEventAsync，相当于四种不同的订阅方式
 * 消息的接收是根据参数中的类名来决定执行哪一个的
 *
 * 参考：http://blog.csdn.net/harvic880925/article/details/40660137
 */
public class MainActivity extends BaseActivity {

    /**
     * actionbar
     */
    @InjectView(R.id.toolbar)
    Toolbar mToolbar;
    /**
     * 侧边栏
     */
    @InjectView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    /**
     * 侧边栏打开关闭事件监听器
     */
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    /**
     * 网络状态监听器
     */
    private BroadcastReceiver netStateReceiver;
    /**
     * android6.0样式对话框
     */
    private MaterialDialog noNetWorkDialog;
    private long exitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    @Override
    protected void initView() {

        ButterKnife.inject(this);
        mToolbar.setTitleTextColor(Color.WHITE);//设置actionbar的标题文字颜色
        setSupportActionBar(mToolbar);//对当前的activity设置actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.app_name,
                R.string.app_name) {
            @Override
            public void onDrawerClosed(View drawerView) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        mActionBarDrawerToggle.syncState();//该方法会自动和actionBar关联, 将开关的图片显示在了action上，如果不设置，也可以有抽屉的效果，不过是默认的图标
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);

        replaceFragment(R.id.frame_container, new FreshNewsFragment());//主页面Fragment
        replaceFragment(R.id.drawer_container, new MainMenuFragment());//侧边栏Fragment
    }

    @Override
    protected void initData() {

        netStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(
                        ConnectivityManager.CONNECTIVITY_ACTION)) {
                    if (NetWorkUtil.isNetWorkConnected(MainActivity.this)) {
                        EventBus.getDefault().post(new NetWorkEvent(NetWorkEvent.AVAILABLE));
                    } else {
                        EventBus.getDefault().post(new NetWorkEvent(NetWorkEvent.UNAVAILABLE));
                    }
                }
            }
        };

        //注册一个网络状态监听器
        registerReceiver(netStateReceiver, new IntentFilter(
                ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * onEvent:如果使用onEvent作为订阅函数，那么该事件在哪个线程发布出来的，onEvent就会在这个线程中运行，
     * 也就是说发布事件和接收事件线程在同一个线程。使用这个方法时，
     * 在onEvent方法中不能执行耗时操作，如果执行耗时操作容易导致事件分发延迟。
     *
     * onEventMainThread:如果使用onEventMainThread作为订阅函数，那么不论事件是在哪个线程中发布出来的，
     * onEventMainThread都会在UI线程中执行，接收事件就会在UI线程中运行，这个在Android中是非常有用的，
     * 因为在Android中只能在UI线程中跟新UI，所以在onEventMainThread方法中是不能执行耗时操作的。
     *
     * onEventBackground:如果使用onEventBackground作为订阅函数，那么如果事件是在UI线程中发布出来的，
     * 那么onEventBackground就会在子线程中运行，如果事件本来就是子线程中发布出来的，
     * 那么onEventBackground函数直接在该子线程中执行。
     *
     * onEventAsync：使用这个函数作为订阅函数，那么无论事件在哪个线程发布，都会创建新的子线程在执行onEventAsync.
     * @param event 消息的接收是根据参数中的类名来决定执行哪一个的
     */
    public void onEvent(NetWorkEvent event) {

        if (event.getType() == NetWorkEvent.UNAVAILABLE) {

            if (noNetWorkDialog == null) {
                noNetWorkDialog = new MaterialDialog.Builder(MainActivity.this)
                        .title("无网络连接")
                        .content("去开启网络?")
                        .positiveText("是")
                        .backgroundColor(getResources().getColor(JDApplication.COLOR_OF_DIALOG))
                        .contentColor(JDApplication.COLOR_OF_DIALOG_CONTENT)
                        .positiveColor(JDApplication.COLOR_OF_DIALOG_CONTENT)
                        .negativeColor(JDApplication.COLOR_OF_DIALOG_CONTENT)
                        .titleColor(JDApplication.COLOR_OF_DIALOG_CONTENT)
                        .negativeText("否")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Intent intent = new Intent(
                                        Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(intent);
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                            }
                        })
                        .cancelable(false)
                        .build();
            }
            if (!noNetWorkDialog.isShowing()) {
                noNetWorkDialog.show();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netStateReceiver);//注销网络状态监听器
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ShowToast.Short("再按一次退出程序");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Drawer Method
    ///////////////////////////////////////////////////////////////////////////

    /**
     * 菜单键点击的事件处理
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 设备配置改变时
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();//该方法会自动和actionBar关联, 将开关的图片显示在了action上，如果不设置，也可以有抽屉的效果，不过是默认的图标
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }

}
