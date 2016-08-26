package com.hwyjr.app.include;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by zyl on 2016/8/26.
 */

//处理自定义协议的类
public class SScheme {

    protected Context context;
    protected String url;
    protected String host;
    protected String query;
    // IWXAPI 是第三方app和微信通信的openapi接口
    private IWXAPI api;

    public  SScheme(Context context) {
        this.context = context;
        api = WXAPIFactory.createWXAPI(context, Const.APP_ID, false);
        api.registerApp(Const.APP_ID);
    }

    public void parseUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            host = uri.getHost();
            query = uri.getQuery();

            switch (host) {
                case "login":
                    String act = uri.getQueryParameter("act");
                    if (act.equals("weixin")) {

                        if (!api.isWXAppInstalled()) {
                            //提醒用户没有按照微信
                            Toast.makeText(context, "请先安装微信!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //微信登陆
                        final SendAuth.Req req = new SendAuth.Req();
                        req.scope = "snsapi_userinfo";
                        req.state = "wechat_sdk_hwy";
                        api.sendReq(req);
                        ((Activity)context).finish();
                    }
                    System.out.println("登录");
                    break;
                case "scan":
                    System.out.println("扫码");
                    break;
                case "share":
                    System.out.println("分享");
                    break;
                default:
                    return;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
    }
}
