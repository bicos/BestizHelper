package com.pockru.bestizhelper.asynctask;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebView.HitTestResult;

import com.pockru.bestizhelper.data.Constants;

public class ImgDownloadTask extends AsyncTask<HitTestResult, Void, Void> {
	private Context mContext;

	public ImgDownloadTask(Context context) {
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected Void doInBackground(HitTestResult... params) {
		saveImageFile(params[0]);
		return null;
	}

	private void saveImageFile(HitTestResult result) {
		try {
			DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			String url = result.getExtra();
			String fileName = createFileName(url);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setTitle("사진 다운로드");
			request.setDescription(url);
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			}
			manager.enqueue(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String createFileName(String url){
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String ext = MimeTypeMap.getFileExtensionFromUrl(url);
		ext = TextUtils.isEmpty(ext) ? "png" : ext;
		String fileName = Constants.IMG_PREFIX_NAME + "_" + timeStamp +"."+ ext;
		return fileName;
	}
}
