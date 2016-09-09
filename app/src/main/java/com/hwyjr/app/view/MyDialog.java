package com.hwyjr.app.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.hwyjr.app.R;

/**
 * Created by zyl on 2016/9/9.
 */
public abstract  class MyDialog extends Dialog {
    private Context context;
    /**
     * 构造器
     * @param context 上下文
     * @param layoutId 资源文件id
     */
    public MyDialog (Context context, int layoutId) {
        super(context,layoutId);
        this.context = context;
        createDialog(layoutId);
    }
    /**
     * 设置dialog
     * @param layoutId
     */
    public  void createDialog(int layoutId){
        setContentView(layoutId);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        window.setAttributes(params);
        initViews();
        if(!(context instanceof Activity)){
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
    }

    public void closeDialog(){
        dismiss();
    }
    /**
     * 用于初始化相应的控件
     */
    public abstract void initViews();

}
