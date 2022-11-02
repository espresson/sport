package com.kyhero.sport.communal;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by elena on 2017/9/4.
 */

public class LogcatHelper {

    private final String TAG = "LogcatHelper";
    private static LogcatHelper INSTANCE = null;
    private static String PATH_LOGCAT = "sdcard/APPLog/";
    private LogDumper mLogDumper = null;
    private int mPId;


    private static String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File external = context.getExternalFilesDir(null);
            if (external != null) {
                return external.getAbsolutePath();
            }
        }
        return context.getFilesDir().getAbsolutePath();
    }

    /**
     * 初始化目录
     */
    public void init() {
        Log.d(TAG, "init  " + PATH_LOGCAT);
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            boolean ok = file.mkdirs();
            Log.d(TAG, "init  mkdirs " + ok);
        }
    }

    public static LogcatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LogcatHelper(context);
        }
        return INSTANCE;
    }

    private LogcatHelper(Context context) {
        init();
        mPId = android.os.Process.myPid();
    }

    public void start() {
        if (mLogDumper == null) {
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);
            mLogDumper.start();
        }
    }

    public void stop() {
        if (mLogDumper != null) {
            mLogDumper.stopLogs();
            mLogDumper = null;
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds;
        private String mPID;
        private FileOutputStream out = null;
        public String type = "situp";

        public LogDumper(String pid, String dir) {
            mPID = pid;
            Log.d(TAG, "LogDumper: !!!!!!!!");
            if (SportParam.SportType == 0) {
                type = "situp";
            } else if (SportParam.SportType == 1) {
                type = "pullup";
            }else{
                type = "pushup";
            }
            try {
                out = new FileOutputStream(new File(dir, type + "-" + getDateEN() + ".log"));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d(TAG, "LogDumper: " + e.toString());
            }

            /**
             *
             * 日志等级：*:v , *:d , *:w , *:e , *:f , *:s
             *
             * 显示当前mPID程序的 E和W等级的日志.
             *
             * */
            cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
            // cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
            // cmds = "logcat -s way";//打印标签过滤信息
            // cmds = "logcat *:e *:i *:d *:v *:w | grep \"(" + mPID + ")\"";
            // cmds = "logcat *:e *:i | grep \"(" + mPID + ")\"";

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(
                        logcatProc.getInputStream()), 1024);
                String line = null;
                while (mRunning && (line = mReader.readLine()) != null) {
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(mPID)) {
                        out.write((line + "\n").getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }
            }
        }
    }

    public String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }

    //    public  String getFileName() {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        String date = format.format(new Date(System.currentTimeMillis()));
//        return date;// 2012年10月03日 23:41:31
//    }

}
