package com.hwyjr.app.wxapi;

import com.hwyjr.app.MainActivity;
import com.hwyjr.app.R;
import com.hwyjr.app.include.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import com.hwyjr.app.include.Const;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.constants .ConstantsAPI;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, Const.APP_ID, false);
        api.registerApp(Const.APP_ID);
        api.handleIntent(getIntent(), this);

        //handleIntent(getIntent());
    }


    public void onResume() {
        super.onResume();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {

        System.out.println("wx back " + req.getType());
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:

                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:

                break;
            default:
                break;
        }
    }

    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        int result = resp.errCode;
        String CallbackParams = "";
        System.out.println("wx back " + resp.errCode);
        Intent intent = new Intent(this, MainActivity.class);
        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            String code = "";
            SendAuth.Resp authResp = (SendAuth.Resp) resp;
            code = authResp.code;
            CallbackParams = "{errCode:"+result+",code:\""+code+"\"}";
            intent.putExtra("wx_type", "login");
            intent.putExtra("wx_back", CallbackParams);
        }

        if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
            CallbackParams = "{errCode:"+result+"}";
            intent.putExtra("wx_type", "share");
            intent.putExtra("wx_back", CallbackParams);
        }


        System.out.println("微信（1）回调 " + CallbackParams);
        ;
        //startActivityForResult(intent, 0);
        startActivity(intent);


        //this.webviewCallback(CallbackParams);
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

}
