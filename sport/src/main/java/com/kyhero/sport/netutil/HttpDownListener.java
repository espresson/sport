package com.kyhero.sport.netutil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public interface HttpDownListener {
    void onFailure(Call call, IOException e);
    void onFinish(Call call, Response response, long mTotalLength, long mAlreadyDownLength);
    void onResponse(Call call, Response response, long mTotalLength, long mAlreadyDownLength);
}
