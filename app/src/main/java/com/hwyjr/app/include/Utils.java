package com.hwyjr.app.include;

import android.annotation.TargetApi;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zyl on 2016/8/26.
 */
public class Utils {


    //版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;

        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }


    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }


    public static Bitmap getRemotePic(String pic) {
        Bitmap bitmap = null;

        try {
            URL picUrl = new URL(pic);
            HttpURLConnection conn = (HttpURLConnection) picUrl
                    .openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            //conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (Exception e) {
            Log.d("hwy", "加载图片失败");
            e.printStackTrace();
            return bitmap;
        } finally {
        }

        return bitmap;
    }


    @TargetApi(12)
    public static byte[] bmpToByteArray(Bitmap thumb, boolean compress) {
        Bitmap forThumb;
        int height = thumb.getHeight();
        int newWidth = 100;
        int newheight = (int)((thumb.getHeight() * 1.0 / thumb.getWidth()) * 100);
        newheight = 100;
        forThumb = Bitmap.createScaledBitmap(thumb, newWidth, newheight,true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int size = 0;
        if (Build.VERSION.SDK_INT >= 11) {
            size = forThumb.getByteCount();
        } else {
            size = forThumb.getRowBytes() * forThumb.getHeight();
        }

        if (compress) {
            if (size > 32 * 1024 ) {
                forThumb.compress(Bitmap.CompressFormat.PNG, 30, baos);
            }

        }

        return baos.toByteArray();
    }


    /**
     * Get image from newwork
     * @param path The path of image
     * @return InputStream
     * @throws Exception
     */
    public InputStream getImageStream(String path) throws Exception{
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
            return conn.getInputStream();
        }
        return null;
    }
    /**
     * Get data from stream
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while( (len=inStream.read(buffer)) != -1){
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }


    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
