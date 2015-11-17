package com.socks.jiandan.utils;

import android.os.Build;
import android.os.StrictMode;

import com.socks.jiandan.BuildConfig;

/**
 * 开启严格模式，检测内存、硬盘等敏感操作，线程监控出现问题会出对话框提示
 * detectAll是检测所有项目
 * detectDiskWrites
 * detectDiskReads
 * detectNetwork
 * Created by zhaokaiqiang on 15/11/9.
 */
public class StrictModeUtil {

    /**
     * 是否使用StrictMode检测代码的成熟度
     * 应该是在测试生版本中使用该方法，正式发布的版本不要用该方法收集代码质量
     */
    private static boolean isShow = false;

    public static void init() {
        if (isShow && BuildConfig.DEBUG && Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {

            //线程监控，会弹框哦
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .penaltyDialog()//弹框
                    .build());

            //VM监控
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

}

/**
 * 忽略规则有两种方法，一种是单纯在代码中把Strictmode的代码注释掉，
 * 另外一种比较好的方法是，在需要忽略的时候和地方，增加相应的代码去让系统停止使用这些规则去检查，
 * 等开发者认为有必要检查时，再重新应用这些规则，比如：

 　　StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();

 　　StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)

 　　.permitDiskWrites()

 　　.build());

 　　doCorrectStuffThatWritesToDisk();

 　　StrictMode.setThreadPolicy(old);

 　　这里首先用old来保存了当前的策略规则，然后doCorrectStuffThatWritesToDisk();

 　　这里，执行了一些向磁盘快速读写的操作，最后又重新启用了这些规则。
 */
