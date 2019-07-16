package com.example.administrator.application01;

import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.squareup.okhttp.Request;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Administrator on 2019/3/24.
 * 崩溃
 */
public class CrashCatcher implements Thread.UncaughtExceptionHandler {

    public static final String TAG = "CrashHandlerUtil";
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //CrashHandler实例
    private static CrashCatcher INSTANCE = new CrashCatcher();
    //程序的Context对象
    private Context mContext;
    //用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<>();

    //用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
    private String crashTip = "应用开小差了，稍后重启下，亲！";

    public String getCrashTip() {
        return crashTip;
    }

    public void setCrashTip(String crashTip) {
        this.crashTip = crashTip;
    }

    private CrashCatcher() {
    }

    public static CrashCatcher getInstance() {
        return INSTANCE;
    }


    public void init(Context context) {
        mContext = context;
        //获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     *
     * @param thread 线程
     * @param ex     异常
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //退出程序
            //退出JVM(java虚拟机),释放所占内存资源,0表示正常退出(非0的都为异常退出)
            System.exit(0);
            //从操作系统中结束掉当前程序的进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param throwable 异常
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(final Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        //handler+looper
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                throwable.printStackTrace();
                //StringUtils.showMsgAsCenter(mContext,getCrashTip());
                Looper.loop();
            }
        }.start();
        //保存日志文件
        saveCrashInfo2File(throwable);
        return true;
    }


    /**
     * 保存错误信息到文件中
     *
     * @param ex 异常
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);

        /*
        这个 crashInfo 就是我们收集到的所有信息，可以做一个异常上报的接口用来提交用户的crash信息
         */
        String crashInfo = sb.toString();
        Log.i("------testme-----------",crashInfo);

        String brand = getBrand();
        String version = getVersion();
        final String url = "http://192.168.1.102:8080/crash/postcrash?";
        final String para = "crashInfo="+crashInfo+"&brand="+brand+"&version="+version;
        Log.e("canshu",para);
//        OkhttpManager.getAsync(url+para, new OkhttpManager.DataCallBack() {
//            @Override
//            public void requestSuccess(String result) {
//            }
//            @Override
//            public void requestFailure(Request request, Exception e) {
//            }
//        });
        new Thread(new Runnable() {
            @Override
            public void run() {
//                try{
//                    TransferData(url,para);
//                }catch (Exception e){
//                    Log.e("出现异常",e.toString());
//                }
                //TransferData(url,para);
                OkhttpManager.getAsync(url+para, new OkhttpManager.DataCallBack() {
                @Override
                public void requestSuccess(String result) {
                }
                @Override
                public void requestFailure(Request request, Exception e) {
                }
        });
            }
        }).start();


        return null;
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
