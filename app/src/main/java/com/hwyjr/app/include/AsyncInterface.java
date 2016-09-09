package com.hwyjr.app.include;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * Created by zyl on 2016/9/1.
 * 异步下载图片回调，用户分享朋友圈
 */
public interface AsyncInterface {

    public void imgdownload(Bitmap bitmap, JSONObject params, int type);
}
