package com.hwyjr.app.wxapi;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;

import com.hwyjr.app.MainActivity;
import com.hwyjr.app.include.Const;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.constants .ConstantsAPI;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
/**
 * Created by zyl on 2016/8/27.
 */
public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private static final String TAG = "MicroMsg.SDKSample.WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Const.APP_ID, false);
        api.registerApp(Const.APP_ID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("wx_type", "pay");
        String CallbackParams = "{errCode:"+resp.errCode+"}";
        intent.putExtra("wx_back", CallbackParams);
        //startActivity(intent);
        System.out.println("微信支付回调 " + CallbackParams);
        startActivity(intent);
    }
}
