package com.hwyjr.app.scan;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.hwyjr.app.R;
import com.hwyjr.app.Util;
import com.hwyjr.app.http.UserAPI;
import com.hwyjr.app.util.MyApplication;


public class ResultActivity extends Activity {
	private int QR_WIDTH = 300;
	private int QR_HEIGHT = 300;
	private BufferedOutputStream outStream;
	private long time = Calendar.getInstance().getTimeInMillis();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_result);
		initdata();
		 findViewById(R.id.button_back).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	private void initdata() {
		/*生成二维码
		 * 
		 * */
				View view = LayoutInflater.from(ResultActivity.this).inflate(R.layout.bitmapdialog, null);
				ImageView img = (ImageView) view.findViewById(R.id.dialog_bitmap);
				final String url = "http://www.baidu.com";
				final Bitmap scanbitmap = Util.createQRImage(url,QR_WIDTH,QR_HEIGHT);
				img.setImageBitmap(scanbitmap);
				
					new AlertDialog.Builder(ResultActivity.this)
						.setTitle("二维码")
						.setView(view)
						.setPositiveButton("存至本地",
								new DialogInterface.OnClickListener() {

									

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
											/*
											 * 将图片转换成一个byte[]；
											 * */
											byte [] bitmaps = getbitmaptobytes(scanbitmap);
											
											/*
											 * 将Byte[]转换成long类型
											 * */
											long longbitmaps = bytes2long(bitmaps);
											/*
											 * 判断SD卡是否有足够的空间供下载使用
											 * */
//											boolean iscapacity = isEnaleforDownload(longbitmaps);
											if(true){
											
											try {
												File sdCardDir = Environment.getExternalStorageDirectory();
												//防止出现重复名字
												String fileName =time+".png";
												File dir;
												dir = new File(sdCardDir.getCanonicalPath()+"/myscan/");
												if (!dir.exists()) {
													dir.mkdirs();
												}
												
												File cacheFile = new File(dir, fileName);
												FileOutputStream fstream = new FileOutputStream(cacheFile);
									            outStream = new BufferedOutputStream(fstream);
									            outStream.write(bitmaps);
									           
									            
									            Toast.makeText(ResultActivity.this, "图片成功存至myscan目录下", 0).show();
									            
											} catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
												Log.d("ydh","保存本地图片异常！！！");
											}finally{
												
												if(outStream!=null){
													try {
														outStream.close();
													} catch (IOException e) {
														// TODO Auto-generated catch block
														e.printStackTrace();
													}
												}
												//TODO 
												dialog.dismiss();
												dialog=null;
											}
										
										}  }else{
											Toast.makeText(ResultActivity.this, "SDCard存储空间不足", 0).show();
										}

									}

									private boolean isEnaleforDownload(long longbitmaps) {
										/*
										 * Statfs : 获取系统文件的类
										 * @.getAbsolutePath()给一个抽象路径名的绝对路径字符串
										 * 
										 * */
										StatFs statfs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
										
										//获得你的手机共有几个存储，即获得块的总量
										int blockCount = statfs.getBlockCount();
										
										//该手机里可用的块的数量，即可用的存储。也可以说是剩余内存容量
										int availableBlocks = statfs.getAvailableBlocks();
										
										/*
										 * 获得每一个块的大小， 返回值用long接受，int可能达到上限
										 * */
										long blockSize = statfs.getBlockSize();
										//获得可用的存储空间
										
										long asavespace = availableBlocks * blockSize;
										
										if(asavespace>longbitmaps){
											return true;
										}
										return false;
									}

									/*
									 * 将Byte[]转换成long类型
									 * */
									private long bytes2long(byte[] bitmaps) {
										int num = 0;
										for (int ix = 0; ix < 8; ++ix) {  
											        num <<= 8;  
											        num |= (bitmaps[ix] & 0xff);  
											  }  
										return num;
									}

									/*
									 * 将图片转换成Byte[]
									 * */
									private byte[] getbitmaptobytes(Bitmap bitmap) {
										ByteArrayOutputStream out = new ByteArrayOutputStream();
										bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
										return out.toByteArray();
									}
								}).show();

		
	}


	

}