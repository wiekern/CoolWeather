package com.wiekern.coolweather.util;

/**
 * Created by yafei on 9/8/16.
 */

public interface HttpCallbackListener {
    void onFinish(String response);
    void onError(Exception e);
}
