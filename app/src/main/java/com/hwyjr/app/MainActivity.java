package com.hwyjr.app;

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
import android.widget.FrameLayout;
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

public class MainActivity extends AppCompatActivity  {

    protected WebView webview;
    protected LinearLayout NaviBar;
    private ViewFlipper allFlipper;
    private String jsCallbacFunc = "" ;
    private String jsBackParams = "";
    private int inited = 0;
    private int resumed = 0;
    private JSONObject shareContent ;
    private IWXAPI api;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case 1:
                    //切换到主页面
                    allFlipper.setDisplayedChild(1);
                    break;
                case 2:
                    //关闭分享
                    findViewById(R.id.share_box).setVisibility(View.GONE);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle ct  = getIntent().getExtras();
        System.out.println("onCreate called");
        setContentView(R.layout.activity_main);

        if (webview == null) {
            webview = (WebView) findViewById(R.id.container);
            allFlipper = (ViewFlipper) findViewById(R.id.allFlipper);
            this.initWebview();
            this.initNaviBarEvent();
            this.initShareEvent();
            webview.loadUrl(Const.WEB_PORTAL);
            api = WXAPIFactory.createWXAPI(this, Const.APP_ID, false);
            api.registerApp(Const.APP_ID);
        } else {
            System.out.println("webview has exist");
        }


        if (ct != null) {
            //第二次进入，不需要显示启动页
            handler.sendEmptyMessage(1);

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    handler.sendEmptyMessage(1); //给UI主线程发送消息
                }
            }, 3000); //启动等待3秒钟
        }


        //handleIntent(getIntent());
    }


    public void onResume() {
        super.onResume();
        Bundle by = this.getIntent().getExtras();
        System.out.println("恢复了 " );
        Intent ct = getIntent();
        if ("login".equals(ct.getStringExtra("wx_type"))) {
            handelWxLoginBack(ct.getStringExtra("wx_back"));
        }
        //
        if ("pay".equals(ct.getStringExtra("wx_type"))) {
            handelWxPayBack(ct.getStringExtra("wx_back"));
        }
        ct.removeExtra("wx_type");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void initWebview() {
        this.inited = 1;

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAppCacheEnabled(true);
       // WebView.setWebContentsDebuggingEnabled(true);
        //设置ua
        String DefaultUa = webview.getSettings().getUserAgentString();
        String NewUa = DefaultUa + " hwy/" + Utils.getVersionName(this) ;
        webview.getSettings().setUserAgentString(NewUa);
        NaviBar = (LinearLayout)findViewById(R.id.navi_bar);
        final MainActivity self = this;

        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                if (url.startsWith("tel:") ){
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);

                }
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

            @Override
            public void onPageFinished(WebView view, String url)
            {

                //开始
                super.onPageFinished(view, url);
                //回调js
                //只有main.js 加载完成才回调
                self.webviewCallback();


            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl)  {
                super.onReceivedError( view, errorCode,
                        description, failingUrl);
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

    /**
     *  初始化分享事件
     */
    public void initShareEvent() {
        TextView cancel = (TextView)findViewById(R.id.share_cancel_bnt);
        final FrameLayout share_nt = (FrameLayout)findViewById(R.id.share_box);
        ImageButton wxShFriend = (ImageButton)findViewById(R.id.share_wx_friend);
        ImageButton wxShTimeline = (ImageButton)findViewById(R.id.share_wx_timeline);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            share_nt.setVisibility(View.GONE);
        }});
        wxShFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("分享到微信朋友");
                if (!api.isWXAppInstalled()) {
                    //提醒用户没有按照微信
                    Toast.makeText(share_nt.getContext(), "请先安装微信!", Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
    }

    public void hideShare() {
        FrameLayout share_nt = (FrameLayout)findViewById(R.id.share_box);
        share_nt.setVisibility(View.INVISIBLE);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()){
            webview.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出整个应用程序
    }



    /**
     * 回调h5的js代码
     * @param
     */
    public void webviewCallback() {
        if (jsCallbacFunc != null && jsCallbacFunc != ""  && jsBackParams != "") {
            webview.loadUrl("javascript:"+jsCallbacFunc+" && "+jsCallbacFunc+"(" +jsBackParams+")");
            //webview.loadUrl("javascript:"+JsContent);
            jsCallbacFunc = jsBackParams = "";
        }
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
                    findViewById(R.id.share_box).setVisibility(View.VISIBLE);
                    shareContent = jsonParams;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(2); //给UI主线程发送消息
                        }
                    }, 10000); //10秒后关闭分享
                    break;
                case "pay":
                    System.out.println("支付");
                    act = uri.getQueryParameter("act");
                    if (act.equals("weixin")) {

                        if (!api.isWXAppInstalled()) {
                            //提醒用户没有按照微信
                            Toast.makeText(this, "请先安装微信!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {

                            this.saveData("order", jsonParams.getString("order_sn"));
                            PayReq request = new PayReq();
                            request.appId = jsonParams.getString("appid");
                            request.partnerId = jsonParams.getString("partnerid");
                            request.prepayId = jsonParams.getString("prepayid");
                            request.packageValue = jsonParams.getString("package");
                            request.nonceStr = jsonParams.getString("noncestr");
                            request.timeStamp = jsonParams.getString("timestamp");
                            request.sign = jsonParams.getString("sign");
                            api.sendReq(request);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    return;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }
    }

    public void handelWxPayBack(String wxBack) {
        jsCallbacFunc = "AppCall.wxPayBack";
        jsBackParams = wxBack;
        webviewCallback();
    }

    public void handelWxLoginBack(String wxBack) {
        jsCallbacFunc = "AppCall.wxCallback";
        jsBackParams = wxBack;
        webviewCallback();
    }
    public void saveData(String key, String value) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key,value);
        editor.commit();
    }

    public void removeData(String key) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.commit();
    }

    public String getData(String key, String Default) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return  sharedPref.getString(key, Default);
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {


        //

        switch ( resultCode ) {
            case 0 :
                if ("login".equals(data.getStringExtra("wx_type"))) {
                    handelWxLoginBack(data.getStringExtra("wx_back"));
                }
                break;
            case 1:
                if ("pay".equals(data.getStringExtra("wx_type"))) {
                    handelWxPayBack(data.getStringExtra("wx_back"));
                }
                break;
            default :
                break;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("on new intent");
        setIntent(intent);
    }


}
