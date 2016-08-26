package com.hwyjr.app.wxapi;

import com.hwyjr.app.R;
import com.hwyjr.app.include.Utils;

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
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    protected WebView webview;
    protected LinearLayout NaviBar;
    private ViewFlipper allFlipper;
    private IWXAPI api;
    private String jsCallbacFunc ;
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
        jsCallbacFunc = "";
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            jsCallbacFunc = savedInstanceState.getString("jsCallbacFunc");
            System.out.println("onCreate: temp = " + jsCallbacFunc);
        }
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
        this.initNaviBarEvent();
        api = WXAPIFactory.createWXAPI(this, Const.APP_ID, false);
        api.registerApp(Const.APP_ID);
        api.handleIntent(getIntent(), this);
        //handleIntent(getIntent());
    }


    public void onResume() {
        super.onResume();
        //jsCallbacFunc = (String)this.getIntent().getExtras().get("jsCallbacFunc");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("jsCallbacFunc", jsCallbacFunc);
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
        webview.getSettings().setUserAgentString(NewUa);
        NaviBar = (LinearLayout)findViewById(R.id.navi_bar);
        final WXEntryActivity self = this;
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
                        //官网
                        NaviBar.setVisibility(View.GONE);
                    } else {
                        NaviBar.setVisibility(View.VISIBLE);
                    }
                }

                if (url != null && url.startsWith("hwy://")) {
                    System.out.println(url);
                    //自定义协议

                    (self).parseUrl(url);
                }

                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {

                if (url.contains(Const.WEB_HOST)) {
                    //官网
                    NaviBar.setVisibility(View.GONE);
                } else {
                    NaviBar.setVisibility(View.VISIBLE);
                }
                //结束
                super.onPageStarted(view, url, favicon);

            }
        });


    }

    public void WebViewBack() {
        if (webview.canGoBack()) {
            webview.goBack();
        }
    }
    public void initNaviBarEvent() {
        //初始化naviBar事件
        ImageButton ibnt = (ImageButton)findViewById(R.id.wb_back);
        ibnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webview.canGoBack()) {
                    webview.goBack();
                }
            }
        });

        TextView home = (TextView)findViewById(R.id.wb_home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webview.loadUrl(Const.WEB_PORTAL);
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
        String op ="";
        String CallbackParams = "";
        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            String code = "";
            SendAuth.Resp authResp = (SendAuth.Resp) resp;
            code = authResp.code;
            CallbackParams = "{errCode:"+result+",code:'"+code+"'}";
        }
        this.webviewCallback(CallbackParams);
    }

    /**
     * 回调h5的js代码
     * @param params
     */
    public void webviewCallback(String params) {
        if (jsCallbacFunc != null && jsCallbacFunc != "" ) {
            webview.loadUrl("javascript:"+jsCallbacFunc+"('" +params+"')");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    public void parseUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            jsCallbacFunc = uri.getQueryParameter("callback");
            String params = uri.getQueryParameter("params");
            //解析json格式的参数
            JSONObject jsonParams = null;
            try {
                if (params != null) {
                    jsonParams = new JSONObject(params);
                }

            } catch (JSONException e) {
                System.out.println("json jiexi shibai");
            }

            switch (host) {
                case "login":
                    String act = uri.getQueryParameter("act");
                    if (act.equals("weixin")) {

                        if (!api.isWXAppInstalled()) {
                            //提醒用户没有按照微信
                            Toast.makeText(this, "请先安装微信!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //微信登陆
                        final SendAuth.Req req = new SendAuth.Req();
                        req.scope = "snsapi_userinfo";
                        req.state = "wechat_sdk_hwy";
                        api.sendReq(req);
                        //finish();
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
