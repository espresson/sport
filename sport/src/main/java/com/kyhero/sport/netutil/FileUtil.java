package com.kyhero.sport.netutil;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.jraska.falcon.Falcon;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {


    public static final String TAG = "FileUtil";
    public static String DEBUG_JSON = "debug.json";
    public static String DEBUG_APK = "debug.apk";

    public static String PATH_LOGCAT;
    public static String apkFile;

    /**
     * 初始化目录
     */
    public static File createFile(Context context, String filestr) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// 优先保存到SD卡中
            PATH_LOGCAT = context.getExternalCacheDir() + File.separator + "APPUpdate/";
        } else { // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = context.getFilesDir().getAbsolutePath() + File.separator + "llvisionLog";
        }
        Log.d(TAG, "init  " + PATH_LOGCAT);
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            boolean ok = file.mkdirs();
            Log.d(TAG, "init  mkdirs " + ok);
        }

        apkFile = PATH_LOGCAT + filestr;
        Log.d(TAG, "init  mkdirs " + apkFile);
        File file1 = new File(PATH_LOGCAT, filestr);

        if (!file1.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return null;
            }
        }
        return file1;
    }

    public static File getRootPath(Context context) {
        File path = new File(context.getExternalCacheDir(), "APPUpdate");
        if (!path.exists()) {
            path.mkdirs();
        }
        return path;
    }

    public static File getFile(Context context, String relativePathFileName) {
        return new File(getRootPath(context), relativePathFileName);
    }

    public static boolean isExistFile(Context context, String relativePathFileName) {
        File file = getFile(context, relativePathFileName);
        return file.exists();
    }

    public static String loadJSONFromFile(Context context, String source) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            in = new FileInputStream(new File(getRootPath(context), source));//文件名
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            Log.d(TAG, "loadJSONFromFile   " + e.toString());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d(TAG, "loadJSONFromFile   " + e.toString());
                }
            }
        }
        return content.toString();
    }


    public static DebugJson getDebugJson(String json) {
        DebugJson debugJson = new DebugJson();
        try {
            JSONObject jObject = new JSONObject(json);
            Double version = jObject.getDouble("version");
            debugJson.setVersion(version);
        } catch (JSONException e) {
            Log.d(TAG, "getDebugJson   " + e.toString());
            return null;
        }
        return debugJson;
    }


    /**
     * 返回系统变量中的主程序版本号，默认返回1.00
     */
    public static String getVersion(Context context) {
        String versionName = "1.00";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
        } catch (Exception e) {

        }
        return versionName;
    }

    public static void capture(Activity activity) {
        File f = createFile1();
        Falcon.takeScreenshot(activity, f);
    }

    /**
     * 初始化目录
     */
    public static File createFile1() {
        String PATH_LOGCAT = "sdcard/APPLog/";
        Log.d(TAG, "init  " + PATH_LOGCAT);
        File file = new File(PATH_LOGCAT);
        if (!file.exists()) {
            boolean ok = file.mkdirs();
            Log.d(TAG, "init  mkdirs " + ok);
        }

        File file1 = new File(PATH_LOGCAT, getDateEN() + ".jpg");

        if (!file1.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.d(TAG, "init  mkdirs " + e.toString());
                return null;
            }
        }
        return file1;
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;// 2012-10-03 23:41:31
    }

}
