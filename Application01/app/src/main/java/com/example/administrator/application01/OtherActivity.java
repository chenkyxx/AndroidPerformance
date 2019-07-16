package com.example.administrator.application01;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2019/3/3.
 * fps
 * cpu
 * mem
 * crash
 * leak
 */
public class OtherActivity extends Activity {
    private Intent MonitorService;
    private boolean isTesting = true;
    Button btn,crashbtn,leakbtn,blockbtn;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity);

        btn = (Button) this.findViewById(R.id.cpubtn);
        crashbtn = (Button) this.findViewById(R.id.crashbtn);
        leakbtn = (Button) this.findViewById(R.id.leakbtn);
        blockbtn = (Button) this.findViewById(R.id.blockbtn);

        crashbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发生crash
                List list = new ArrayList();
                list.get(-1);
            }
        });

        leakbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //发生内存泄露
                mHandler.sendMessageDelayed(Message.obtain(),600000);
            }
        });

        blockbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 发生app卡顿现象
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTesting){
                    MonitorService = new Intent();
                    MonitorService.setClass(OtherActivity.this, Sampler.class);
                    startService(MonitorService);
                    btn.setText("stop");
                    isTesting = false;
                }else{
                    btn.setText("start");
                    isTesting = true;
                    stopService(MonitorService);
                }
            }
        });
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };
}
