package com.hwyjr.app;

import com.hwyjr.app.include.Utils;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import com.hwyjr.app.include.Const;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.constants .ConstantsAPI;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;

import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements IWXAPIEventHandler {

    protected WebView webview;
    protected LinearLayout NaviBar;
    private ViewFlipper allFlipper;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:
                    //切换到主页面
                    allFlipper.setDisplayedChild(1);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webview = (WebView) findViewById(R.id.container);
        allFlipper = (ViewFlipper) findViewById(R.id.allFlipper);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(1); //给UI主线程发送消息
            }
        }, 3000); //启动等待3秒钟
        this.initWebview();
    }


    public void initWebview() {
        webview.loadUrl(Const.WEB_PORTAL);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAppCacheEnabled(true);
        //WebView.setWebContentsDebuggingEnabled(true);
        //设置ua
        String DefaultUa = webview.getSettings().getUserAgentString();
        String NewUa = DefaultUa + " hwy/" + Utils.getVersionName(this) ;
        System.out.println("new ua is " + NewUa);
        webview.getSettings().setUserAgentString(NewUa);
        NaviBar = (LinearLayout)findViewById(R.id.navi_bar);
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                if (url != null && (url.startsWith("http://") || url.startsWith("https://") )) {

                    //如果url host 不是官网，则需要添加返回按钮
                    view.loadUrl(url);
                    if (url.contains(Const.WEB_HOST)) {
                        //非官网
                        NaviBar.setVisibility(View.GONE);
                    } else {
                        NaviBar.setVisibility(View.VISIBLE);
                    }
                }

                if (url != null && url.startsWith("hwy://")) {
                    //自定义协议

                }

                return true;
            }
        });


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()){
            webview.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出整个应用程序
    }


    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
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
        int result = 0;

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:

                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:

                break;
            default:

                break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }




    //处理自定义协议

    public void handelScheme(String url) {
        if (url == null) {
            return ;
        }
        try {
            URL  urlObj = new URL(url);
            String host = urlObj.getHost();
            String Query = urlObj.getQuery();

            switch (host) {
                case "login":
                    break;
                case "scan":
                    break;
                case "share":
                    break;
                default:
                    return;
            }


        } catch (MalformedURLException e) {
            return ;
        }

    }
}
