package com.example.administrator.application01;

import android.os.Build;
import android.util.Log;

import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LeakUploadService extends DisplayLeakService {
    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        String classname = result.className;
        String leaktrace = result.leakTrace.toString();
        String brand = getBrand();
        String version = getVersion();

        Log.i("------testme-----------",leaktrace);
        Log.i("------testme-----------",leakInfo);

        //上传数据
        String url = "http://192.168.1.102:8080/MemoryLeakAnalysis/addmemoryleakinfo?" ;
        String para = "classname="+classname+"&leaktrace="+leaktrace+"&brand="+brand+"&version="+version;
        Log.e("发生内存泄露，开始上报",para);
        TransferData(url,para);

    }


    private String getBrand(){
        return Build.MODEL;
    }

    private String getVersion(){
        return Build.VERSION.RELEASE;
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
            return  strRes;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }
}

