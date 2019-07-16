package com.example.administrator.application01;


import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareup.okhttp.Request;

import java.io.RandomAccessFile;
public class Sampler extends Service {
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams wmParams = null;
    private View viFloatingWindow;
    private float mTouchStartX;
    private float mTouchStartY;
    private float startX;
    private float startY;
    private float x;
    private float y;
    private TextView txtTotalMem;
    private TextView txtUnusedMem;
    private Handler handler = new Handler();

    private ActivityManager activityManager;
    private Long lastCpuTime;
    private Long lastAppCpuTime;
    private RandomAccessFile procStatFile;
    private RandomAccessFile appStatFile;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
            activityManager = (ActivityManager)  getApplication().getSystemService(Context.ACTIVITY_SERVICE);

            viFloatingWindow = LayoutInflater.from(this).inflate(R.layout.floating, null);
            txtUnusedMem = (TextView) viFloatingWindow.findViewById(R.id.memunused);
            txtTotalMem = (TextView) viFloatingWindow.findViewById(R.id.memtotal);

            txtUnusedMem.setText("计算中,请稍后...");
            txtUnusedMem.setTextColor(Color.BLUE);
            txtTotalMem.setTextColor(Color.BLUE);

            createFloatingWindow();
            handler.postDelayed(task, 1000);
    }

    private void createFloatingWindow() {
        windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wmParams = ((MyApplication) getApplication()).getMywmParams();
        wmParams.type = 2002;
        wmParams.flags |= 8;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.format = 1;
        windowManager.addView(viFloatingWindow, wmParams);
        viFloatingWindow.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                x = event.getRawX();
                y = event.getRawY() - 25;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = x;
                        startY = y;
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updateViewPosition();
                        break;

                    case MotionEvent.ACTION_UP:
                        updateViewPosition();
                        mTouchStartX = mTouchStartY = 0;
                        break;
                }
                return true;
            }
        });
    }

    private Runnable task = new Runnable() {
        public void run() {
            dataRefresh();
            handler.postDelayed(this, 1000);
            windowManager.updateViewLayout(viFloatingWindow, wmParams);
        }
    };

    /**
     * refresh the data showing in floating window
     */
    private void dataRefresh() {
              double cpu = sampleCPU();
              double mem = sampleMemory();

              //
        String url = "http://192.168.1.102:8080/cpumemory/postcpumemory?cpuuserd="+cpu+"&memoryused="+mem;
        OkhttpManager.getAsync(url, new OkhttpManager.DataCallBack() {
            @Override
            public void requestSuccess(String result) {
            }
            @Override
            public void requestFailure(Request request, Exception e) {
            }
        });


              txtUnusedMem.setText("cpu:" + (double)Math.round(cpu*100)/100 + "%");
              txtTotalMem.setText("mem:" + (double)Math.round(mem*100)/100 + "M");
    }

    /**
     * update the position of floating window
     */
    private void updateViewPosition() {
        wmParams.x = (int) (x - mTouchStartX);
        wmParams.y = (int) (y - mTouchStartY);
        windowManager.updateViewLayout(viFloatingWindow, wmParams);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (windowManager != null)
            windowManager.removeView(viFloatingWindow);
        handler.removeCallbacks(task);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private double sampleCPU() {
        long cpuTime;
        long appTime;
        double sampleValue = 0.0D;
        try {
            if (procStatFile == null || appStatFile == null) {
                procStatFile = new RandomAccessFile("/proc/stat", "r");
                appStatFile = new RandomAccessFile("/proc/" + Process.myPid() + "/stat", "r");
            } else {
                procStatFile.seek(0L);
                appStatFile.seek(0L);
            }
            String procStatString = procStatFile.readLine();
            String appStatString = appStatFile.readLine();
            String procStats[] = procStatString.split(" ");
            String appStats[] = appStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (lastCpuTime == null && lastAppCpuTime == null) {
                lastCpuTime = cpuTime;
                lastAppCpuTime = appTime;
                return sampleValue;
            }
            sampleValue = ((double) (appTime - lastAppCpuTime) / (double) (cpuTime - lastCpuTime)) * 100D;
            lastCpuTime = cpuTime;
            lastAppCpuTime = appTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sampleValue;
    }

    private double sampleMemory() {
        double mem = 0.0D;
        try {
            final Debug.MemoryInfo[] memInfo = activityManager.getProcessMemoryInfo(new int[]{Process.myPid()});
            if (memInfo.length > 0) {
                // TotalPss = dalvikPss + nativePss + otherPss, in KB
                final int totalPss = memInfo[0].getTotalPss();
                if (totalPss >= 0) {
                    mem = totalPss / 1024.0D;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mem;
    }
}
