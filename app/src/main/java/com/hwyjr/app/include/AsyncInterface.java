package com.hwyjr.app.include;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * Created by zyl on 2016/9/1.
 */
public interface AsyncInterface {

    public void imgdownload(Bitmap bitmap, JSONObject params);
}
