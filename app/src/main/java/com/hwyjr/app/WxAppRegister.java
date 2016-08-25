package com.hwyjr.app;

import com.hwyjr.app.include.Const;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
/**
 * Created by zyl on 2016/8/25.
 */
public class WxAppRegister extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        final IWXAPI api = WXAPIFactory.createWXAPI(context, null);

        // 将该app注册到微信
        api.registerApp(Const.APP_ID);
    }
}
