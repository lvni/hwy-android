package com.hwyjr.app;

import com.hwyjr.app.R;
import com.hwyjr.app.include.AsyncInterface;
import com.hwyjr.app.include.FileUtils;
import com.hwyjr.app.include.Utils;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.os.Handler;
import android.os.Message;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;
import com.hwyjr.app.include.Const;

import com.hwyjr.app.scan.MipcaActivityCapture;
import com.hwyjr.app.view.MyDialog;
import com.hwyjr.app.view.MyWebView;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import com.hwyjr.app.include.BitmapDownloaderTask;

public class MainActivity extends AppCompatActivity  implements AsyncInterface, MyWebView.LongClickCallBack {

    protected MyWebView webview;
    protected LinearLayout NaviBar;
    private ViewFlipper allFlipper;
    private String jsCallbacFunc = "" ;
    private String jsDelayCall = "";
    private String DevPusToken = "";

    private MyDialog mCustomDialog;
    private ArrayAdapter<String> adapter;
    private int isDeviceRegister = 0;// 设备是否向H5注册了



    private JSONObject shareContent ;
    private IWXAPI api;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> mUploadMessage;
    private ValueCallback<Uri> mUploadMessage1;
    ProgressBar progressbar;
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
                    hideShare();
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


        if (!Utils.isNetworkConnected(this)) {
            Toast.makeText(this, "网络无法连接", Toast.LENGTH_SHORT).show();
        }
        if (webview == null) {
            webview = (MyWebView) findViewById(R.id.container);
            allFlipper = (ViewFlipper) findViewById(R.id.allFlipper);
            this.initWebview();
            this.initNaviBarEvent();
            this.initShareEvent();
            webview.loadUrl(Const.WEB_PORTAL);
            api = WXAPIFactory.createWXAPI(this, Const.APP_ID, false);
            api.registerApp(Const.APP_ID);

            registerPush();
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
        Intent ct = getIntent();
        if ("login".equals(ct.getStringExtra("wx_type"))) {
            webviewCallback(ct.getStringExtra("wx_back"));
        }
        //
        if ("pay".equals(ct.getStringExtra("wx_type"))) {
            webviewCallback(ct.getStringExtra("wx_back"));
        }

        if ("share".equals(ct.getStringExtra("wx_type"))) {
            webviewCallback(ct.getStringExtra("wx_back"));
        }
        if ("scan".equals(ct.getStringExtra("wx_type"))) {
            webviewCallback(ct.getStringExtra("wx_back"));
        }
        ct.removeExtra("wx_type");
        XGPushClickedResult click = XGPushManager.onActivityStarted(this);
        if (click != null) { // 判断是否来自信鸽的打开方式

            String customContent = click.getCustomContent();
            String jsBack = "{title:'"+click.getTitle()+"'"
                          + ",content:'" + click.getContent() + "'"
                          + ",customContent:"+customContent+"}";

            jsCallbacFunc = "AppCall.pushBack";
            webviewCallback(jsBack);
            jsDelayCall = "AppCall.pushBack && AppCall.pushBack("+jsBack+")";
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @TargetApi(22)
    public void initWebview() {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.setLongClickCallBack(this);  //注册长按

        webview.setFocusable(true);
        webview.setFocusableInTouchMode(true);

        //提高网页加载速度，暂时阻塞图片加载，然后网页加载好了，在进行加载图片
        //webview.getSettings().setBlockNetworkImage(true);

        WebView.setWebContentsDebuggingEnabled(true);
        //设置ua
        int chanelId = 0;
        try {
            ApplicationInfo appInfo = this.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            chanelId = appInfo.metaData.getInt("CHANNEL");
        } catch (Exception e) {
            //获取不到元数据就算了
        }
        String DefaultUa = webview.getSettings().getUserAgentString();
        String NewUa = DefaultUa + " hwy/" + Utils.getVersionName(this) + " (" + Utils.getVersionCode(this) + ") channel("+chanelId+")" ;
        webview.getSettings().setUserAgentString(NewUa);
        NaviBar = (LinearLayout)findViewById(R.id.navi_bar);
        progressbar = webview.getProgressBar(); //获取进度条
        webview.setWebChromeClient(new WebChromeClient(){
            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                if (mUploadMessage1 != null) {
                    mUploadMessage1.onReceiveValue(null);
                }
                mUploadMessage1 = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                if (mUploadMessage1 != null) {
                    mUploadMessage1.onReceiveValue(null);
                }
                mUploadMessage1 = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                String type = TextUtils.isEmpty(acceptType) ? "*/*" : acceptType;
                i.setType(type);
                startActivityForResult(Intent.createChooser(i, "File Chooser"),
                        FILECHOOSER_RESULTCODE);
            }

            // For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                if (mUploadMessage1 != null) {
                    mUploadMessage1.onReceiveValue(null);
                }
                mUploadMessage1 = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                String type = TextUtils.isEmpty(acceptType) ? "*/*" : acceptType;
                i.setType(type);
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }


            //Android 5.0+
            @Override
            @SuppressLint("NewApi")
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                mUploadMessage = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                if (fileChooserParams != null && fileChooserParams.getAcceptTypes() != null
                        && fileChooserParams.getAcceptTypes().length > 0) {
                    i.setType(fileChooserParams.getAcceptTypes()[0]);
                } else {
                    i.setType("*/*");
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    progressbar.setVisibility(View.GONE);
                } else {
                    if (progressbar.getVisibility() == View.GONE)
                        progressbar.setVisibility(View.VISIBLE);
                    progressbar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }


        });

        //使用默认浏览器下载资源
        webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent =  new  Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
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

                    parseUrl(url);
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
                view.getSettings().setBlockNetworkImage(true);

            }

            @Override
            public void onPageFinished(WebView view, String url)
            {

                //开始
                super.onPageFinished(view, url);
                view.getSettings().setBlockNetworkImage(false);
                if (isDeviceRegister == 0 && DevPusToken != "") {
                    //想服务器注册当前设备
                    registerToH(DevPusToken);
                    isDeviceRegister = 1;
                }

                webViewDelayCallback();



            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl)  {
                super.onReceivedError( view, errorCode, description, failingUrl);
                loadEorrrPage(failingUrl);
            }




        });


    }

    /**
     * 加载显示错误页，当网络不能用是出现
     * @param failingUrl
     */
    public void loadEorrrPage(String failingUrl) {

        if (failingUrl == null || failingUrl == "") {
            failingUrl = Const.WEB_PORTAL;
        }
        String url = Const.ERROR_PAGE + "?url=" + failingUrl;
        try {
            webview.loadUrl(url);
        } catch (Exception e) {
            System.out.println("失败页异常");
        }
    }

    /**
     *  文件选择回调
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (result == null) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
            String path =  FileUtils.getPath(this, result);
            if (TextUtils.isEmpty(path)) {
                mUploadMessage.onReceiveValue(null);
                mUploadMessage = null;
                return;
            }
            Uri uri = Uri.fromFile(new File(path));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mUploadMessage.onReceiveValue(new Uri[]{uri});
            } else {
                mUploadMessage1.onReceiveValue(uri);
            }

            mUploadMessage = null;
        }
    }


    /**
     * 初始化导航按钮事件
     */
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
        final MainActivity self = this;
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             hideShare();
        }});
        wxShFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startShareWx(shareContent, SendMessageToWX.Req.WXSceneSession);
            }
        });
        wxShTimeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startShareWx(shareContent, SendMessageToWX.Req.WXSceneTimeline);
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

            String url = webview.getOriginalUrl();
            if (url == Const.WEB_PORTAL
                    || url.contains(Const.WEB_PORTAL + "index.html")
                    || webview.getOriginalUrl().contains(Const.ERROR_PAGE)) {
                finish();
            }
            webview.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出整个应用程序
    }



    /**
     * 回调h5的js代码
     * @param
     */
    public void webviewCallback(String params) {
        if (jsCallbacFunc != null && jsCallbacFunc != ""  && params != "") {
            webview.loadUrl("javascript:"+jsCallbacFunc+" && "+jsCallbacFunc+"(" +params+")");
            jsCallbacFunc  = "";
        }
    }

    public void webViewDelayCallback() {
        if (jsDelayCall != null && jsDelayCall != "" && !webview.canGoBack()) {
            //只有在不能返回，也就是第一次进入才能执行
            webview.loadUrl("javascript:" + jsDelayCall);
        }
        jsDelayCall = "";
    }



    public void startShareWx(JSONObject params, int scence) {
        BitmapDownloaderTask a = new BitmapDownloaderTask();
        String img = null;
        try {

            params.put("scene", scence);
            img = params.getString("img");

        } catch (Exception e) {
             //不可能进来
        }
        a.setCallBack(this, params,Const.DL_TYPE_WEIXIN);
        a.execute(img);

    }

    /**
     *  分享到微信
     * @param params
     * @param thumb
     */
     public void shareWx(JSONObject params, Bitmap thumb) {
        try {
            int sence = params.getInt("scene");
            if (!api.isWXAppInstalled()) {
                //提醒用户没有按照微信
                Toast.makeText(this, "请先安装微信!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (params.has("link")) {
                //有链接
                WXWebpageObject webpage = new WXWebpageObject();
                webpage.webpageUrl = params.getString("link");
                WXMediaMessage msg = new WXMediaMessage(webpage);
                msg.title = params.getString("title");
                msg.description = params.getString("desc");
                if (thumb != null) {
                    msg.thumbData = Utils.bmpToByteArray(thumb, true);
                }
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.transaction = Utils.buildTransaction("webpage");
                req.message = msg;
                req.scene = sence;
                api.sendReq(req);
                hideShare();
            } else if (params.has("img")) {
                //纯图片
                if (thumb != null) {
                    WXImageObject imgObj = new WXImageObject(thumb);
                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;
                    msg.thumbData = Utils.bmpToByteArray(thumb, true);
                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = Utils.buildTransaction("img");
                    req.message = msg;
                    req.scene = sence;
                    api.sendReq(req);
                }

            }
        } catch (Exception e) {
            //分享出现异常
            Toast.makeText(this, "分享出现问题", Toast.LENGTH_SHORT).show();
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
                        return ;
                    }
                    System.out.println("登录");
                    break;
                case "scan":
                    System.out.println("扫码");
                    Intent iten = new Intent(MainActivity.this, MipcaActivityCapture.class);
                    startActivity(iten);
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
                        return ;
                    } else {
                        Toast.makeText(this, "目前不支持，请更新最新版本app", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "setPushTag":
                    String tag  = uri.getQueryParameter("tag");
                    if (tag != null && tag != "") {
                        //有tag，调用接口设置
                        setPushTag(tag);
                    }
                    break;
                case "removePushTag":
                    tag  = uri.getQueryParameter("tag");
                    if (tag != null && tag != "") {
                        //有tag，调用接口设置
                        removePushTag(tag);
                    }
                    break;
                case  "close":
                    finish();
                    break;
                case "update":
                    //下载更新app
                    String link = jsonParams.getString("url");
                    String title = jsonParams.getString("title");
                    String desc = jsonParams.getString("desc");
                    Utils.startDownload(this,link,title,desc);
                    break;
                default:
                    Toast.makeText(this, "目前不支持，请更新最新版本app", Toast.LENGTH_SHORT).show();
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ;
        }


    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.out.println("on new intent");
        setIntent(intent);
    }

    /**
     * 图片异步下载完成后的回调，type 觉得类型
     * @param c
     * @param params
     * @param type
     */
    public void imgdownload(Bitmap c, JSONObject params, int type) {
        if (type == Const.DL_TYPE_WEIXIN) {
            //微信分享
            shareWx(params, c);
        }

        if (type == Const.DL_TYPE_STORE) {
            //图片保存
            saveImg(c);
        }

    }

    /**
     * webview长按图片回调
     * @param imgUrl
     */
    @Override
    public void onLongClickCallBack(String imgUrl) {

        showDialog(imgUrl);
        System.out.println("长按图片了 " + imgUrl);

    }

    /**
     * 开始保存图片
     * @param img
     */
    public void startSaveImg(String img) {
        BitmapDownloaderTask a = new BitmapDownloaderTask();
        a.setCallBack(this, null,Const.DL_TYPE_STORE);
        a.execute(img);
    }

    /**
     * 保存图片
     * @param img
     */
    public void saveImg(Bitmap img) {

        int seconds = (int)(System.currentTimeMillis() / 1000);
        File appDir = new File(Environment.getExternalStorageDirectory(), "hwy");
        String filename =  seconds + ".jpg";
        File f = new File(appDir, filename);
        try {
            if (!appDir.exists()) {
                appDir.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(f);
            img.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(MainActivity.this, "图片保存在" + appDir + "目录下" , Toast.LENGTH_LONG).show();
            // 其次把文件插入到系统图库

             MediaStore.Images.Media.insertImage(this.getContentResolver(),
                        f.getAbsolutePath(), filename, null);

            // 最后通知图库更新
            this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + f.getAbsolutePath())));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            Toast.makeText(MainActivity.this, "保存图片失败", Toast.LENGTH_LONG).show();

        }
    }
    /**
     * 显示长按图片的Dialog
     * @param
     */
    private void  showDialog(final String imgUrl) {

        adapter = new ArrayAdapter<String>(this,R.layout.item_dialog);
        adapter.add("发送给朋友");
        adapter.add("保存到手机");

        mCustomDialog = new MyDialog(this, R.layout.custom_dialog) {

            @Override
            public void initViews() {
                // 初始CustomDialog化控件
                ListView mListView = (ListView) findViewById(R.id.listView);
                mListView.setAdapter(adapter);
                mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // 点击事件
                        switch (position) {
                            case 0:
                                //Toast.makeText(MainActivity.this, "已发送给朋友", Toast.LENGTH_LONG).show();
                                JSONObject param = new JSONObject();
                                try {
                                    param.put("img", imgUrl);
                                } catch (JSONException e) {
                                    //不应该
                                }
                                startShareWx(param, SendMessageToWX.Req.WXSceneSession);
                                closeDialog();
                                break;
                            case 1:
                                startSaveImg(imgUrl);
                                //Toast.makeText(MainActivity.this, "已保存到手机", Toast.LENGTH_LONG).show();
                                closeDialog();
                                break;
                            default:
                                closeDialog();
                                break;
                        }

                    }
                });
            }
        };
        mCustomDialog.show();
    }


    private void registerToH(String token) {
        jsCallbacFunc = "AppCall.deviceRegister";  //注册当前设备接口
        String jsContent = "{token:'"+token+"'}";
        webviewCallback(jsContent);
    }
    /**
     * 注册推送
     */
    private void registerPush() {
        XGPushConfig.enableDebug(this, true);
        XGPushManager.registerPush(getApplicationContext(), new XGIOperateCallback() {
            @Override
            public void onSuccess(Object o, int i) {
                System.out.println("注册成功，设备token为：" + o);
                DevPusToken = o.toString();

            }

            @Override
            public void onFail(Object o, int i, String s) {
                System.out.println( "注册失败，错误码：" + i + ",错误信息：" + s);
            }
        });
    }



    private void setPushTag( String Tag) {
        XGPushManager.setTag(this, Tag);
    }

    private void removePushTag(String tag) {
        XGPushManager.deleteTag(this, tag);
    }

}
