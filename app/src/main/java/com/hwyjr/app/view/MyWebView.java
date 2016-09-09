package com.hwyjr.app.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.AbsoluteLayout;
import android.widget.ProgressBar;

import com.hwyjr.app.R;

/**
 * Created by zyl on 2016/9/9.
 */
@SuppressWarnings("deprecation")
public class MyWebView extends WebView implements View.OnLongClickListener{

    private Context context;
    private LongClickCallBack mCallBack;
    private ProgressBar progressBar;
    public MyWebView(Context context) {
        super(context);
        this.context = context;
        this.init(context);
    }


    public ProgressBar getProgressBar() {
        return this.progressBar;
    }

    public void init(Context context) {
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setLayoutParams(new AbsoluteLayout.LayoutParams(AbsoluteLayout.LayoutParams.MATCH_PARENT, 6, 0, 0));
        progressBar.setProgressDrawable(getResources().getDrawable(R.drawable.webview_progressbar));
        addView(progressBar);
        setOnLongClickListener(this);
    }


    public void setLongClickCallBack(LongClickCallBack back) {
        this.mCallBack = back;
    }


    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    public MyWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    @TargetApi(21)
    public MyWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context);
    }


    @Override
    public boolean onLongClick(View v) {
        // 长按事件监听（注意：需要实现LongClickCallBack接口并传入对象）
        final HitTestResult htr = getHitTestResult();//获取所点击的内容
        System.out.println("webview 长按 " + htr.getType() + " == " + WebView.HitTestResult.IMAGE_TYPE);
        if (htr.getType() == WebView.HitTestResult.IMAGE_TYPE) {//判断被点击的类型为图片
            if (mCallBack != null) {
                mCallBack.onLongClickCallBack(htr.getExtra());
            }

        }
        return false;
    }

    /**
     * 长按事件回调接口，传递图片地址
     * @author LinZhang
     */
    public interface LongClickCallBack{
        /**用于传递图片地址*/
        void onLongClickCallBack(String imgUrl);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        LayoutParams lp = (LayoutParams) progressBar.getLayoutParams();
        lp.x = l;
        lp.y = t;
        progressBar.setLayoutParams(lp);
        super.onScrollChanged(l, t, oldl, oldt);
    }
}
