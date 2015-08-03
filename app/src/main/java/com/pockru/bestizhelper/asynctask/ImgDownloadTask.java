package com.pockru.bestizhelper.asynctask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebView.HitTestResult;
import android.widget.Toast;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.Constants;

public class ImgDownloadTask extends AsyncTask<HitTestResult, Void, Void> {
	private static final int ERR_SAVE_IMG_FILE = 10000;
	
	ProgressDialog dlg;
	Context mContext;

	private Handler toastHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case ERR_SAVE_IMG_FILE:
				if (mContext != null) {
					Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.error_msg_save_picture), Toast.LENGTH_SHORT).show();					
				}
				break;

			default:
				break;
			}

		};
	};
	
	public ImgDownloadTask(Context context) {
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
		if (mContext instanceof Activity) {
			dlg = ProgressDialog.show(mContext, null, "사진을 저장중입니다.");
		}
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Void result) {
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
		}
		Toast.makeText(mContext, mContext.getString(R.string.msg_save_picture), Toast.LENGTH_SHORT).show();
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		if (dlg != null && dlg.isShowing()) {
			dlg.dismiss();
		}
		super.onCancelled();
	}

	@Override
	protected Void doInBackground(HitTestResult... params) {
		saveImageFile(params[0]);
		return null;
	}

	private void saveImageFile(HitTestResult result) {
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(result.getExtra());
			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				URL url = new URL(result.getExtra());
				String timeStamp = String.valueOf(System.currentTimeMillis());
				String ext = MimeTypeMap.getFileExtensionFromUrl(url.getFile());
				ext = TextUtils.isEmpty(ext) ? "png" : ext;
				String fileName = Constants.IMG_PREFIX_NAME + "_" + timeStamp +"."+ ext;
//				int index = fileName.lastIndexOf("/");
//				if (index >= 0) {
//					fileName = fileName.substring(index + 1);
//				}
//
//				if (fileName.lastIndexOf(".") < 0) {
//					fileName = fileName.concat(".PNG");
//				}
//
//				if (fileName.contains(":large")) {
//					fileName = fileName.replace(":large", "");
//				}
////				String fileName = url.getPath() +"." + MimeTypeMap.getFileExtensionFromUrl(url.getFile());
//				String fileName = url.getPath();
//				Log.i("ravy", "fileName : "+fileName);
				
				File imgDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
				File saveImgFile = new File(imgDir, fileName);
				if (!saveImgFile.exists())
					saveImgFile.createNewFile();

				InputStream is = entity.getContent();
				BufferedInputStream bis = new BufferedInputStream(is);
				ByteArrayBuffer buffer = new ByteArrayBuffer(8);
				int current = 0;
				while ((current = bis.read()) != -1) {
					buffer.append((byte) current);
				}

				FileOutputStream fos = new FileOutputStream(saveImgFile);
				fos.write(buffer.toByteArray());
				fos.close();
				is.close();

				// Tell the media scanner about the new file so that it is
				// immediately available to the user.
				MediaScannerConnection.scanFile(mContext, new String[] { saveImgFile.toString() }, null, new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
					}
				});

			}
		} catch (Exception e) {
			e.printStackTrace();
			toastHandler.sendEmptyMessage(ERR_SAVE_IMG_FILE);
		}
	}
}
