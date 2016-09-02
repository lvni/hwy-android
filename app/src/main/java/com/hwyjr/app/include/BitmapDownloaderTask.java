package com.hwyjr.app.include;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by zyl on 2016/9/1.
 */
public class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
    private String url;
    AsyncInterface main ;
    private  JSONObject code;

    public void setCallBack(AsyncInterface i, JSONObject params) {
        main = i;
        code = params;
    }
    @Override
    // Actual download method, run in the task thread
    protected Bitmap doInBackground(String... params) {
        // params comes from the execute() call: params[0] is the url.
        return downloadBitmap(params[0]);
    }

    @Override
    // Once the image is downloaded, associates it to the imageView
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        main.imgdownload(bitmap, code);

    }

    static Bitmap downloadBitmap(String url) {
        Bitmap bitmap = null;
        try {

            if (url == null || url.equals("")) {
                return bitmap;
            }
            URL picUrl = new URL(url);
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
}
