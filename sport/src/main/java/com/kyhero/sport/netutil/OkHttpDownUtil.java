package com.kyhero.sport.netutil;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @content: okhttp的下载功能工具类 (分别包含：1.无断点续传的get下载 2.有断点续传的get下载 3.无断点续传的post下载 4.有断点续传的post下载)
 * @time:2018-12-12
 * @build:z
 */

public class OkHttpDownUtil {
    private static final String TAG = "OkHttpDownUtil";
    private Call mCall;
    private long mAlreadyDownLength = 0;//已经下载长度
    private long mTotalLength = 0;//整体文件大小
    public int mSign = 0; //标记当前运行的是那个方法
    private String mDownUrl;//下载网络地址
    private File mPath;//文件保存路径
    private FormBody mformBody;
    private JSONObject mJson;
    private HttpDownListener mHttpDownListener;//下载进度接口回调
    Context context;
    HttpDownListener listener;

    private String url = "http://canbus.xygala.com/manage/data/Group/public/home/CanBus/";

    public OkHttpDownUtil(Context context/*, HttpDownListener listener*/) {
        this.context = context;
        //this.listener = listener;
    }

    /**
     * 没有断点续传功能的get请求下载
     *
     * @param downUrl             下载网络地址
     * @param saveFilePathAndName 保存路径
     */
    public void getDownRequest(final String downUrl, final File saveFilePathAndName) {
        mSign = 1;
        mDownUrl = downUrl;
        mPath = saveFilePathAndName;
        mHttpDownListener = listener;
        mAlreadyDownLength = 0;
        Request request = new Request.Builder()
                .url(mDownUrl)
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        mCall = okHttpClient.newCall(request);//构建了一个完整的http请求
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mHttpDownListener != null) {
                    mHttpDownListener.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                mTotalLength = responseBody.contentLength();//下载文件的总长度
                InputStream inp = responseBody.byteStream();
                FileOutputStream fileOutputStream = new FileOutputStream(mPath);
                try {
                    byte[] bytes = new byte[2048];
                    int len = 0;
                    while ((len = inp.read(bytes)) != -1) {
                        mAlreadyDownLength = mAlreadyDownLength + len;
                        fileOutputStream.write(bytes, 0, len);
                        if (mHttpDownListener != null) {
                            mHttpDownListener.onResponse(call, response, mTotalLength, mAlreadyDownLength);
                        }

                    }
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: "+e.toString());
                } finally {
                    fileOutputStream.close();
                    inp.close();
                }
            }
        });
    }

    /**
     * 有断点续传功能的get下载
     *
     * @param downUrl             下载地址
     * @param saveFilePathAndName 保存路径
     * @param listener            进度监听
     */
    public void getRenewalDownRequest(final String downUrl, final File saveFilePathAndName, final HttpDownListener listener) {
        mSign = 2;
        mDownUrl = downUrl;
        mPath = saveFilePathAndName;
        mHttpDownListener = listener;
        Request request = new Request.Builder()
                .url(mDownUrl)
                .header("RANGE", "bytes=" + mAlreadyDownLength + "-")
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        mCall = okHttpClient.newCall(request);//构建了一个完整的http请求
        mCall.enqueue(new Callback() { //发送请求
            @Override
            public void onFailure(Call call, IOException e) {
                if (mHttpDownListener != null) {
                    mHttpDownListener.onFailure(call, e);
                }
                Log.e(TAG, "onFailure: 异常报错=" + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                InputStream inputStream = responseBody.byteStream();//得到输入流
                RandomAccessFile randomAccessFile = new RandomAccessFile(mPath, "rw");//得到任意保存文件处理类实例
                if (mTotalLength == 0) {
                    mTotalLength = responseBody.contentLength();//得到文件的总字节大小
                    randomAccessFile.setLength(mTotalLength);//预设创建一个总字节的占位文件
                }
                if (mAlreadyDownLength != 0) {
                    randomAccessFile.seek(mAlreadyDownLength);
                }
                byte[] bytes = new byte[2048];
                int len = 0;
                try {
                    while ((len = inputStream.read(bytes)) != -1) {
                        randomAccessFile.write(bytes, 0, len);
                        mAlreadyDownLength = mAlreadyDownLength + len;
                        if (mHttpDownListener != null) {
                            mHttpDownListener.onResponse(call, response, mTotalLength, mAlreadyDownLength);
                        }
                    }

                } catch (Exception e) {
                    Log.d(TAG, "onResponse: "+e.toString());
                } finally {
                    mAlreadyDownLength = randomAccessFile.getFilePointer();//记录当前保存文件的位置
                    randomAccessFile.close();
                    inputStream.close();
                    Log.e(TAG, "流关闭 下载的位置=" + mAlreadyDownLength);
                    mAlreadyDownLength = 0;
                }

            }
        });
    }

    /**
     * 没有断点续传的post下载
     *
     * @param downUrl
     * @param saveFilePathAndName
     * @param json
     * @param listener
     */
    public void postDownRequest(final String downUrl, final File saveFilePathAndName, final JSONObject json, final HttpDownListener listener) {
        mSign = 3;
        mDownUrl = downUrl;
        mPath = saveFilePathAndName;
        mJson = json;
        mHttpDownListener = listener;
        mAlreadyDownLength = 0;
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(mDownUrl)
                .post(changeJSON(json))
                .build();
        mCall = okHttpClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mHttpDownListener != null) {
                    mHttpDownListener.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                ResponseBody responseBody = response.body();
                mTotalLength = responseBody.contentLength();
                InputStream inputStream = responseBody.byteStream();
                FileOutputStream fileOutputStream = new FileOutputStream(mPath);
                byte[] bytes = new byte[2048];
                int len = 0;
                try {
                    while ((len = inputStream.read(bytes)) != -1) {
                        fileOutputStream.write(bytes, 0, len);
                        mAlreadyDownLength = mAlreadyDownLength + len;
                        if (mHttpDownListener != null) {
                            mHttpDownListener.onResponse(call, response, mTotalLength, mAlreadyDownLength);
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: "+e.toString());
                    mHttpDownListener.onFailure(call, (IOException) e);
                } finally {
                    fileOutputStream.close();
                    inputStream.close();
                    mHttpDownListener.onFinish(call, response, mTotalLength, mAlreadyDownLength);
                    Log.d(TAG, "流关闭");
                }
            }

        });

    }

    /**
     * 支持断点续传的post下载
     *
     * @param downUrl             下载网址
     * @param saveFilePathAndName 文件保存路径
     * @param formBody            参数
     * @param listener            接口回调
     */
    public void postRenewalDownRequest(final String downUrl, final File saveFilePathAndName, final FormBody formBody, final HttpDownListener listener) {
        mSign = 4;
        mDownUrl = downUrl;
        mPath = saveFilePathAndName;
        mformBody = formBody;
        mHttpDownListener = listener;
        Request request = new Request.Builder()
                .url(mDownUrl)
                .header("RANGE", "bytes=" + mAlreadyDownLength + "-")
                .post(mformBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        mCall = okHttpClient.newCall(request);//构建了一个完整的http请求
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mHttpDownListener != null) {
                    mHttpDownListener.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody responseBody = response.body();
                InputStream inputStream = responseBody.byteStream();
                RandomAccessFile randomAccessFile = new RandomAccessFile(mPath, "rw");
                if (mTotalLength == 0) {
                    mTotalLength = responseBody.contentLength();
                    randomAccessFile.setLength(mTotalLength);
                }
                if (mAlreadyDownLength != 0) {
                    randomAccessFile.seek(mAlreadyDownLength);
                }
                byte[] bytes = new byte[2048];
                int len = 0;
                try {
                    while ((len = inputStream.read(bytes)) != -1) {
                        randomAccessFile.write(bytes, 0, len);
                        mAlreadyDownLength = mAlreadyDownLength + len;
                        if (mHttpDownListener != null) {
                            mHttpDownListener.onResponse(call, response, mTotalLength, mAlreadyDownLength);
                        }

                    }
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: "+e.toString());
                } finally {
                    mAlreadyDownLength = randomAccessFile.getFilePointer();
                    randomAccessFile.close();
                    inputStream.close();
                    Log.e(TAG, "流关闭 下载的位置=" + mAlreadyDownLength);
                }

            }
        });
    }


    /**
     * 支持断点续传的post下载
     *
     * @param downUrl             下载网址
     * @param saveFilePathAndName 文件保存路径
     *                            //* @param json                参数
     * @param listener            接口回调
     */
    public void postRenewalDownRequest(final String downUrl, final File saveFilePathAndName, final HttpDownListener listener) {
        mSign = 4;
        mDownUrl = url + downUrl;
        mPath = saveFilePathAndName;
        mHttpDownListener = listener;

        Request request = new Request.Builder()
                .url(mDownUrl)
                .header("RANGE", "bytes=" + mAlreadyDownLength + "-")
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        mCall = okHttpClient.newCall(request);//构建了一个完整的http请求
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mHttpDownListener != null) {
                    mHttpDownListener.onFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //请求错误检测
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: "+e.toString());
                    mHttpDownListener.onFailure(call, (IOException) e);
                    return;
                }
                ResponseBody responseBody = null;
                InputStream inputStream = null;
                RandomAccessFile randomAccessFile = null;
                try {
                    responseBody = response.body();
                    inputStream = responseBody.byteStream();
                    randomAccessFile = new RandomAccessFile(mPath, "rw");
                    if (mTotalLength == 0) {
                        mTotalLength = responseBody.contentLength();
                        randomAccessFile.setLength(mTotalLength);
                    }
                    if (mAlreadyDownLength != 0) {
                        randomAccessFile.seek(mAlreadyDownLength);
                    }
                    byte[] bytes = new byte[4096];
                    int len = 0;
                    while ((len = inputStream.read(bytes)) != -1) {
                        randomAccessFile.write(bytes, 0, len);
                        mAlreadyDownLength = mAlreadyDownLength + len;
                        if (mHttpDownListener != null) {
                            mHttpDownListener.onResponse(call, response, mTotalLength, mAlreadyDownLength);
                        }
                        Log.e(TAG, mTotalLength+" dowm =" + mAlreadyDownLength);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: "+e.toString());
                } finally {
                    mAlreadyDownLength = randomAccessFile.getFilePointer();
                    randomAccessFile.close();
                    inputStream.close();
                    if (mHttpDownListener != null) {
                        mHttpDownListener.onFinish(call, response, mTotalLength, mAlreadyDownLength);
                    }
                    Log.e(TAG, "finish = " + mAlreadyDownLength);
                }
            }
        });
    }

    /**
     * 恢复下载
     */
    public void resume() {
        if (mSign == 0) {
            return;
        }
        switch (mSign) {
            case 1:
                getDownRequest(mDownUrl, mPath);
                break;
            case 2:
                getRenewalDownRequest(mDownUrl, mPath, mHttpDownListener);
                break;
            case 3:
                postDownRequest(mDownUrl, mPath, mJson, mHttpDownListener);
                break;
            case 4:
                postRenewalDownRequest(mDownUrl, mPath, mHttpDownListener);
                break;
            default:
                break;
        }

    }

    /**
     * 暂停下载
     */
    public void stop() {
        if (mCall != null) {
            mCall.cancel();
        }

    }

    /**
     * 删除下载文件
     */
    public void deleteCurrentFile() {
        if (mPath == null) {
            Log.e(TAG, "deleteCurrentFile error : 没有路径");
            return;
        }
        if (!mPath.exists()) {
            Log.e(TAG, "deleteCurrentFile error: 文件不存在");
            return;
        }
        mPath.delete();
        mAlreadyDownLength = 0;
        mTotalLength = 0;
        mSign = 0;
    }

    /**
     * 销毁
     */
    public void destroy() {
        if (mCall != null) {
            mCall.cancel();
            mCall = null;
        }
        mDownUrl = null;
        mPath = null;
        mHttpDownListener = null;
        mSign = 0;
        mAlreadyDownLength = 0;
        mTotalLength = 0;
    }

    /**
     * 转换Json参数为RequestBody
     *
     * @param jsonParam json对象
     * @return RequestBody
     */
    private RequestBody changeJSON(JSONObject jsonParam) {
        RequestBody requestBody = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                , String.valueOf(jsonParam));
        return requestBody;
    }


}
