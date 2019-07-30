package com.example.zhang.videoplayer.utile;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by zhang on 2019/5/10.
 */

public class TimedOff extends Application implements Runnable{
    // 此处采用 LinkedList作为容器，增删速度快
    private static final String TAG = "TimedOff";
    private static LinkedList<Activity> activityLinkedList;
    private int time;
    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        activityLinkedList = new LinkedList<>();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.d(TAG, "onActivityCreated: " + activity.getLocalClassName());
                activityLinkedList.add(activity);
                // 在Activity启动时（onCreate()） 写入Activity实例到容器内
            }
            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.d(TAG, "onActivityDestroyed: " + activity.getLocalClassName());
                activityLinkedList.remove(activity);
                // 在Activity结束时（Destroyed（）） 写出Activity实例
            }
            @Override
            public void onActivityStarted(Activity activity) {
            }
            @Override
            public void onActivityResumed(Activity activity) {
            }
            @Override
            public void onActivityPaused(Activity activity) {
            }
            @Override
            public void onActivityStopped(Activity activity) {
            }
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }
        });
    }

    public  void exitApp() {

        Log.d(TAG, "容器内的Activity列表如下 ");
        // 先打印当前容器内的Activity列表
        for (Activity activity : activityLinkedList) {
            Log.d(TAG, activity.getLocalClassName());
        }

        Log.d(TAG, "正逐步退出容器内所有Activity");

        // 逐个退出Activity
        for (Activity activity : activityLinkedList) {
            activity.finish();
        }
        //  结束进程
         System.exit(0);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time*60*1000);
            exitApp();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
