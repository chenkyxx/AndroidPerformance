package com.example.administrator.application01;

import android.content.Context;
import android.util.Log;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.internal.BlockInfo;
import com.squareup.okhttp.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2019/3/24.
 */
public class AppBlockCanaryContext extends BlockCanaryContext {

    @Override
    public int provideBlockThreshold() {
        return 1000;
    }

    @Override
    public boolean displayNotification() {
        return BuildConfig.DEBUG;
    }

    @Override
    public String providePath() {
        return "/blockcanary";
    }

    @Override
    public void onBlock(Context context, BlockInfo blockInfo) {
        super.onBlock(context, blockInfo);
        String paras;
        paras =  "blockstacktrace="+blockInfo.threadStackEntries.get(1)
                +"&brandname="+blockInfo.model
                +"&packagename="+blockInfo.processName
                +"&uptime="+blockInfo.getTimeString();
        Log.i("testme",blockInfo.threadStackEntries.get(1));
        String url = "http://192.168.1.102:8080/block/AddBlockInfo?";

        OkhttpManager.getAsync(url + paras, new OkhttpManager.DataCallBack() {
            @Override
            public void requestFailure(Request request, Exception e) {
            }
            @Override
            public void requestSuccess(String result) {
            }
        });
    }

    public static String TransferData(String serverUrl, String strd){
        try{
            URL url = new URL(serverUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setDoOutput(true);
            conn.setReadTimeout(90000);
            conn.setRequestMethod("GET");
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(strd);
            out.flush();
            out.close();
            BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String strRes = "";
            while( (line=read.readLine())!= null)
            {
                strRes += line;
            }
            read.close();
            Log.i("strRes:",strRes);
            return  strRes;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
}
