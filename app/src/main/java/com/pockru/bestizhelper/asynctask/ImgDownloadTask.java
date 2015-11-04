package com.pockru.bestizhelper.asynctask;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.webkit.WebView.HitTestResult;

import com.pockru.bestizhelper.data.Constants;

public class ImgDownloadTask extends AsyncTask<HitTestResult, Void, Void> {
//	private static final int ERR_SAVE_IMG_FILE = 10000;
//	ProgressDialog dlg;
	private Context mContext;

//	private Handler toastHandler = new Handler() {
//		public void handleMessage(android.os.Message msg) {
//			switch (msg.what) {
//			case ERR_SAVE_IMG_FILE:
//				if (mContext != null) {
//					Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.error_msg_save_picture), Toast.LENGTH_SHORT).show();
//				}
//				break;
//
//			default:
//				break;
//			}
//
//		};
//	};
	
	public ImgDownloadTask(Context context) {
		mContext = context;
	}

	@Override
	protected void onPreExecute() {
//		if (mContext instanceof Activity) {
//			dlg = ProgressDialog.show(mContext, null, "사진을 저장중입니다.");
//		}
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Void result) {
//		if (dlg != null && dlg.isShowing()) {
//			dlg.dismiss();
//		}
//		Toast.makeText(mContext, mContext.getString(R.string.msg_save_picture), Toast.LENGTH_SHORT).show();
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
//		if (dlg != null && dlg.isShowing()) {
//			dlg.dismiss();
//		}
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
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setTitle("사진 다운로드");
			request.setDescription(url);
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, createFileName(url));
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
