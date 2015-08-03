package com.pockru.bestizhelper.imgur;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.pockru.bestizhelper.R;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.preference.Preference;

public class ImgurUploadImage extends Activity {

	private static final String TAG = "ImgurUploadImage";

	public static final int AUTHORIZE_IMGUR 	= 0;
	public static final int UPLOAD_IMG 			= 1;
	public static final int CANCEL_IMG_UPLOAD = 2;
	
	public static final String CONSUMER_ID = "a2d09c8b7d1ceff";
	public static final String CONSUMER_SECRET = "4d942362651286c76e6f837cfde1abf9c7b9d6df";

	public static final String IMAGE_UPLOAD_URL = "https://api.imgur.com/3/image.json";

	private static final int MAX_IMAGE_WIDTH = 1048;

	//private CommonsHttpOAuthConsumer consumer;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AUTHORIZE_IMGUR:
				uploadImage(imgPath , imgUri);
				break;
			case UPLOAD_IMG:
				// Log.e(TAG, "---------img upload success!!-------------");
				if (msg.obj != null) {
					Intent intent = getIntent();
					intent.putExtra(Constants.ATTR_IMGUR_DATA, (String) msg.obj);
					successActivity(intent);
				}
				break;
			case CANCEL_IMG_UPLOAD:
				cancelActivity();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	String token, secret, imgPath;
	Uri imgUri;

	ProgressDialog d;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		d = new ProgressDialog(this);
		d.setMessage(getString(R.string.dialog_progress_msg_01));
		d.show();

		token = Preference.getImgurToken(this);
		secret = Preference.getImgurSecret(this);
		imgPath = getIntent().getStringExtra(Constants.ATTR_IMG_PATH);
		imgUri = getIntent().getData();

		if (imgPath != null || imgUri != null) {
			mHandler.sendEmptyMessage(AUTHORIZE_IMGUR);
		} else {
			cancelActivity();
		}

	}

	// Uploads an image to Imgur
	public void uploadImage(final String imagePath , final Uri imageUri) {

		new Thread() {
			@Override
			public void run() {
				
				InputStream is = null;
				
				BitmapFactory.Options bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				if (imagePath != null) {
					BitmapFactory.decodeFile(imagePath, bmOptions);	
				}else {
					
					try {
						is = getContentResolver().openInputStream(imageUri);
						BitmapFactory.decodeStream(is, new Rect(), bmOptions);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				

				int photoWidth = bmOptions.outWidth;

				int scaleFactor = (photoWidth > MAX_IMAGE_WIDTH) ? (photoWidth / MAX_IMAGE_WIDTH) : 1;

				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scaleFactor;
				bmOptions.inPurgeable = true;

				Bitmap scaledBitmap = null;
				if (imagePath != null) {
					scaledBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
				}else{
					scaledBitmap = BitmapFactory.decodeStream(is, new Rect(), bmOptions);
				}
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				scaledBitmap.compress(CompressFormat.JPEG, 100, baos);
				
				String data = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

				HttpPost hpost = new HttpPost(IMAGE_UPLOAD_URL);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("image", data));
				nameValuePairs.add(new BasicNameValuePair("type", "base64"));

				try {
					hpost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					cancelActivity();
				}

				hpost.setHeader("Authorization" , "Client-ID "+CONSUMER_ID);

				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

				HttpParams params = new BasicHttpParams();

				SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);

				DefaultHttpClient client = new DefaultHttpClient(mgr, params);
				
				HttpResponse resp = null;
				try {
					resp = client.execute(hpost);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(CANCEL_IMG_UPLOAD);
				} catch (IOException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(CANCEL_IMG_UPLOAD);
				}

				String result = null;
				try {
					result = EntityUtils.toString(resp.getEntity());
				} catch (ParseException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(CANCEL_IMG_UPLOAD);
				} catch (IOException e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(CANCEL_IMG_UPLOAD);
				}

				// debug += result;
				Log.e(TAG, "debug : " + result);

				try {
					JSONObject obj = new JSONObject(result);
					boolean isSuccess = obj.getBoolean("success");
					if (isSuccess) {
						Message msg = mHandler.obtainMessage(UPLOAD_IMG, result);
						mHandler.sendMessage(msg);
					} else {
						mHandler.sendEmptyMessage(CANCEL_IMG_UPLOAD);
					}

				} catch (JSONException e) {
					e.printStackTrace();
					cancelActivity();
				}

			}
		}.start();

	}

	private void successActivity(Intent data) {
		Toast.makeText(this, getText(R.string.toast_msg_imgur_02), Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK, data);
		finish();
	}

	private void cancelActivity() {
		Toast.makeText(this, getText(R.string.error_msg_cant_upload_image), Toast.LENGTH_SHORT).show();
		setResult(RESULT_CANCELED);
		finish();
	}


	public byte[] read(File file) throws IOException {

		// if ( file.length() > MAX_FILE_SIZE ) {
		// throw new FileTooBigException(file);
		// }
		ByteArrayOutputStream ous = null;
		InputStream ios = null;
		try {
			byte[] buffer = new byte[4096];
			ous = new ByteArrayOutputStream();
			ios = new FileInputStream(file);
			int read = 0;
			while ((read = ios.read(buffer)) != -1) {
				ous.write(buffer, 0, read);
			}
		} finally {
			if (ous != null)
				ous.close();

			if (ios != null)
				ios.close();

		}
		return ous.toByteArray();
	}

	@Override
	protected void onDestroy() {
		if (d.isShowing())
			d.dismiss();
		super.onDestroy();
	}
}
