package com.example.administrator.application01;

import android.app.Application;
import android.view.WindowManager;

import com.github.moduth.blockcanary.BlockCanary;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Administrator on 2019/3/3.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.print("MyApplication--onCreate");
        //fps
        Takt.stock(this).hide().play();

        //crash
        CrashCatcher.getInstance().init(getApplicationContext());

        //leak canary
        if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }
        RefWatcher refWatcher = LeakCanary.refWatcher(this)
                .listenerServiceClass(LeakUploadService.class)
                .buildAndInstall();

        // block

        BlockCanary.install(this, new AppBlockCanaryContext()).start();
    }

    @Override
    public void onTerminate() {
        Takt.finish();
        super.onTerminate();
    }

    //提供过去内存要使用的LayoutParams
    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
    public WindowManager.LayoutParams getMywmParams() {
        return wmParams;
    }
}
