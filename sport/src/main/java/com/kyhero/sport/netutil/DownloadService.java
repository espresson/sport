package com.kyhero.sport.netutil;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();

    public static int FLAG_DOWNLOAD = 1;//更新程序

    private Context mContext;
    private OkHttpDownUtil okHttpDownUtil;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        okHttpDownUtil = new OkHttpDownUtil(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //数字是随便写的“40”，
        nm.createNotificationChannel(new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_DEFAULT));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");
        //其中的2，是也随便写的，正式项目也是随便写
        startForeground(2, builder.build());

        downloadFile(okHttpDownUtil, "debug.json", "plugin/22/1/debug.json", HANDLER_DOWNLOAD_JSON_BEGIN);

        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 下载文件
     *
     * @param okHttpDownUtil
     * @param relativePathName    相对于根目录的文件名
     * @param relativeNetPathName 网站上相对于根目录的文件名
     * @param handlerNo
     */
    private void downloadFile(OkHttpDownUtil okHttpDownUtil, String relativePathName, String relativeNetPathName, final int handlerNo) {
        if (okHttpDownUtil.mSign != 0) {
            Log.d(TAG, "mSign==" + okHttpDownUtil.mSign);
            return;
        }
        final File downLoadFile = FileUtil.createFile(mContext, relativePathName);
        if (downLoadFile == null) {
            return;
        }
        downloadHandler.sendEmptyMessage(handlerNo);
        okHttpDownUtil.postRenewalDownRequest(relativeNetPathName, downLoadFile, new HttpDownListener() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "下载失败", e);
                Message msg = new Message();
                msg.what = handlerNo + 3;
                msg.obj = e;
                downloadHandler.sendMessage(msg);
            }


            @Override
            public void onFinish(Call call, Response response, long mTotalLength, long mAlreadyDownLength) {
                //下载完成
                if (mAlreadyDownLength == mTotalLength) {
                    //下载与实际大小一致，视为下载成功
                    downloadHandler.sendEmptyMessage(handlerNo + 2);

                } else {
                    //下载与实际大小不同，视为下载失败
                    downloadHandler.sendEmptyMessage(handlerNo + 3);
                }
            }

            @Override
            public void onResponse(Call call, Response response, long mTotalLength, long mAlreadyDownLength) {
                Message msg = new Message();
                msg.what = handlerNo + 1;
                msg.arg1 = (int) (((float) mAlreadyDownLength / mTotalLength) * 100);
                downloadHandler.sendMessage(msg);
            }
        });
    }

    public static final int HANDLER_DOWNLOAD_BEGIN = 1;  //开始下载
    public static final int HANDLER_DOWNLOAD_RUNNING = 2;//正在下载
    public static final int HANDLER_DOWNLOAD_SUCCESS = 3;//下载成功
    public static final int HANDLER_DOWNLOAD_FAIL = 4;   //下载失败


    public static final int HANDLER_DOWNLOAD_JSON_BEGIN = 5;    //开始下载
    public static final int HANDLER_DOWNLOAD_JSON_RUNNING = 6;  //正在下载
    public static final int HANDLER_DOWNLOAD_JSON_SUCCESS = 7;  //下载成功
    public static final int HANDLER_DOWNLOAD_JSON_FAIL = 8;     //下载失败

    //下载相关处理
    private Handler downloadHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_DOWNLOAD_BEGIN:
                    Log.d(TAG, " HANDLER_DOWNLOAD_BEGIN");
                    sendDownloadStatus("开始下载", 0);
                    break;
                case HANDLER_DOWNLOAD_RUNNING:
                    Log.d(TAG, " HANDLER_DOWNLOAD_RUNNING " + msg.arg1);
                    if (FLAG_DOWNLOAD != msg.arg1) {
                        FLAG_DOWNLOAD = msg.arg1;
                        sendDownloadStatus("下载进度：" + msg.arg1 + "%", 100);
                    }
                    break;
                case HANDLER_DOWNLOAD_SUCCESS:
                    Log.d(TAG, " HANDLER_DOWNLOAD_SUCCESS " + msg.arg1);
                    okHttpDownUtil.destroy();//关闭下载
                    sendDownloadStatus("下载成功", 100);
                    AppUtils.installApk(mContext, new File(FileUtil.apkFile));
                    break;
                case HANDLER_DOWNLOAD_FAIL:
                    Log.d(TAG, " HANDLER_DOWNLOAD_FAIL");
                    okHttpDownUtil.deleteCurrentFile();
                    okHttpDownUtil.destroy();//关闭下载
                    sendDownloadStatus("下载失败", 0);
                    break;
                case HANDLER_DOWNLOAD_JSON_BEGIN:
                    sendDownloadStatus("版本检测", 0);
                    break;
                case HANDLER_DOWNLOAD_JSON_RUNNING:
                    break;
                case HANDLER_DOWNLOAD_JSON_SUCCESS:
                    okHttpDownUtil.destroy();//关闭下载
                    sendDownloadStatus("版本检测", 100);
                    if (FileUtil.isExistFile(mContext, FileUtil.DEBUG_JSON)) {
                        DebugJson debugJson = FileUtil.getDebugJson(FileUtil.loadJSONFromFile(mContext, FileUtil.DEBUG_JSON));
                        if (debugJson != null && FileUtil.getVersion(mContext) != null) {
                            Log.d(TAG, "HANDLER_DOWNLOAD_JSON_SUCCESS: " + debugJson.getVersion() + " " + Double.parseDouble(FileUtil.getVersion(mContext)));
                            if (debugJson.getVersion() > Double.parseDouble(FileUtil.getVersion(mContext))) {
                                downloadFile(okHttpDownUtil, FileUtil.DEBUG_APK, "plugin/22/1/debug.apk", HANDLER_DOWNLOAD_BEGIN);
                            } else {
                                sendDownloadStatus("暂无更新", 0);
                            }
                        }
                    }
                    break;
                case HANDLER_DOWNLOAD_JSON_FAIL:
                    okHttpDownUtil.deleteCurrentFile();
                    okHttpDownUtil.destroy();//关闭下载
                    sendDownloadStatus("更新失败", 0);
                    break;
                default:
                    break;
            }
        }
    };

    public void sendDownloadStatus(CharSequence HANDLER_DOWNLOAD, int progress) {
        Intent intent = new Intent("action.download.display");
        intent.putExtra("download", HANDLER_DOWNLOAD);
        intent.putExtra("progress", progress);
        mContext.sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
