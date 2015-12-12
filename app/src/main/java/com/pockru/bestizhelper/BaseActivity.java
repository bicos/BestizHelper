package com.pockru.bestizhelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.pockru.bestizhelper.application.BestizBoxApplication;
import com.pockru.bestizhelper.application.BestizBoxApplication.TrackerName;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.dialog.WriteDialog;
import com.pockru.bestizhelper.tumblr.TumblrOAuthActivity;
import com.pockru.network.BestizNetworkConn;
import com.pockru.network.RequestInfo;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseActivity extends AppCompatActivity {

	private static final String TAG = "BaseActivity";

	protected static final int NETWORK_3G = 1;
	protected static final int NETWORK_WIFI = 2;
	protected static final int NETWORK_NOT_AVAILABLE = 3;

	public static final int FLAG_REQ_MAIN_ARTICLE = 1000;
	public static final int FLAG_REQ_NEXT_ARTICLE = 1001;
	public static final int FLAG_REQ_LOGIN = 1003;
	public static final int FLAG_REQ_WRITE = 1004;
	public static final int FLAG_REQ_LOGOUT = 1005;
	public static final int FLAG_REQ_SEARCH = 1006;
	public static final int FLAG_REQ_MEM_INFO = 1007;
	public static final int FLAG_REQ_DETAIL_ARTICLE = 1008;
	public static final int FLAG_REQ_COMMENT = 1009;
	public static final int FLAG_REQ_DELETE_COMMENT = 1010;
	public static final int FLAG_REQ_DELETE_COMMENT_OK = 1011;
	public static final int FLAG_REQ_DELETE = 1012;

	protected String privUrl = "";

	protected ProgressBar pb;
	protected boolean isRequestNetwork = false;

	// protected PersistentCookieStore cookieStore;

	protected Header header;

	protected Tracker appTracker;

	protected BoardData mBoardData;
	protected String BASE_SERVER_URL;
	protected String BOARD_ID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		appTracker = ((BestizBoxApplication) getApplication()).getTracker(TrackerName.APP_TRACKER);
		appTracker.enableAdvertisingIdCollection(true);
		appTracker.enableAutoActivityTracking(true);
		appTracker.enableExceptionReporting(true);

		sendGaScreen();
	}

	@Override
	protected void onDestroy() {
		BestizNetworkConn.getInstance(getApplicationContext()).cancel();
		urlList.clear();

		super.onDestroy();
	}

	protected void setActionBarTitle(String title) {
		getSupportActionBar().setTitle(title);
	}

	protected void setActionBarTitle(int titleId) {
		getSupportActionBar().setTitle(titleId);
	}

	protected int checkNetwork() {
		final ConnectivityManager connMgr = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (wifi.isConnected()) {
			return NETWORK_WIFI;
		} else if (mobile.isConnected()) {
			return NETWORK_3G;
		} else {
			return NETWORK_NOT_AVAILABLE;
		}
	}

	private String createParamStr(ArrayList<NameValuePair> params) {
		String result = "";
		if (params == null) {
			return result;
		}

		int max = params.size();
		for (int i = 0; i < max; i++) {
			NameValuePair pair = params.get(i);
			result += (pair.getName() + "=" + pair.getValue());
			if (i != max - 1) {
				result += "&";
			}
		}
		return result;
	}

	ArrayList<String> urlList = new ArrayList<>();

	public void requestNetwork(final int flag, String url, final ArrayList<NameValuePair> params) {
		if (params != null) {
			Log.i(TAG, "request url : " + url + "?" + createParamStr(params));
		} else {
			Log.i(TAG, "request url : " + url);
		}

		// url 셋팅
		url = url.replace(" ", "%20"); // 오류 수정

		final Map<String, String> requestProperty = new HashMap<String, String>();

		// referer setting
		if (!privUrl.equals("")) {
			// BestizBoxApplication.getClientInstance().addHeader("Referer",
			// privUrl);
			requestProperty.put("Referer", privUrl);
		}

		// cookie setting
		// List<Cookie> cookieList = cookieStore.getCookies();
		// if (cookieList != null) {
		// StringBuilder cookieBuilder = new StringBuilder();
		// for (Cookie cookie : cookieList) {
		// cookieBuilder.append(cookie.getName());
		// cookieBuilder.append("=");
		// cookieBuilder.append(cookie.getValue());
		// cookieBuilder.append("&");
		// }
		// if (cookieBuilder.length() > 0) {
		// String cookieStr = cookieBuilder.substring(0, cookieBuilder.length()
		// - 1);
		// BestizBoxApplication.getClientInstance().addHeader("Set-Cookie",
		// cookieStr);
		// }
		// }

		privUrl = url;

		final String finalUrl = url;

		if (urlList.contains(finalUrl)) {
			return;
		} else {
			urlList.add(finalUrl);
		}

		RequestInfo info = new RequestInfo();
		info.setUrl(finalUrl);
		info.setParams(createParamStr(params));
		info.setRequestProperty(requestProperty);
		info.setEncoding("euc-kr");
		if (params != null) {
			try {
				info.setEntity(new UrlEncodedFormEntity(params, "euc-kr"));
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		final BestizNetworkConn conn = BestizNetworkConn.getInstance(getApplicationContext());

		new AsyncTask<RequestInfo, Void, String>() {

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgress();
			}

			@Override
			protected String doInBackground(RequestInfo... params) {
				String response = null;
				try {
					response = conn.requestPost(params[0]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return response;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				dismissProgress();

				urlList.remove(finalUrl);

				if (result != null && result.contains("history.back()")) {
					Document html = Jsoup.parse(result);
					Elements elements = html.select("p[align=center]");
					if (elements != null && elements.size() > 0) {
						showAlertDialog(elements.get(0).text());
					}
				}

//				if (result != null) {
//					if (result.length() > 4000) {
//						Log.v(TAG, "sb.length = " + result.length());
//						int chunkCount = result.length() / 4000;     // integer division
//						for (int i = 0; i <= chunkCount; i++) {
//							int max = 4000 * (i + 1);
//							if (max >= result.length()) {
//								Log.v(TAG, result.substring(4000 * i));
//							} else {
//								Log.v(TAG, result.substring(4000 * i, max));
//							}
//						}
//					} else {
//						Log.v(TAG, result);
//					}
//				}

				onResponse(conn.getResCode(), conn.getHeaderFields(), result, flag);
			}

		}.execute(info);

	}

	private void showAlertDialog(String text) {
		if (isFinishing()) {
			return;
		}

		AlertDialog dialog = new AlertDialog.Builder(this)
										.setTitle("알림")
										.setMessage(text)
										.setPositiveButton("확인", null)
										.create();
		dialog.show();
	}

	public void requestNetwork(final int flag, String url) {
		requestNetwork(flag, url, null);
	}

	private void showProgress() {
		if (!pb.isShown()) {
			pb.setVisibility(View.VISIBLE);
		}
		isRequestNetwork = true;
	}

	private void dismissProgress() {
		if (pb.isShown()) {
			pb.setVisibility(View.GONE);
		}
		isRequestNetwork = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
			openOptionsMenu();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public void sendGaEvent(final int categoryId, final int actionId, final int labelId) {
		if (appTracker != null) {
			appTracker.send(new HitBuilders.EventBuilder().setCategory(getString(categoryId)).setAction(getString(actionId)).setLabel(getString(labelId))
					.build());
		}
	}

	public void sendGaEvent(final String category, final String action, final String label) {
		if (appTracker != null) {
			// if(action.equals("화면접속")){
			// sendGaScreen(category);
			// }
			appTracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
		}
	}

	public void sendGaEvent(final int categoryId, final String action, final String label) {
		if (appTracker != null) {
			// if(action.equals("화면접속")){
			// sendGaScreen(getString(categoryId));
			// }
			appTracker.send(new HitBuilders.EventBuilder().setCategory(getString(categoryId)).setAction(action).setLabel(label).build());
		}
	}

	public void sendGaScreen() {
		sendGaScreen(getClass().getName());
	}

	public void sendGaScreen(final int nameId) {
		appTracker.setScreenName(getString(nameId));
		appTracker.send(new HitBuilders.AppViewBuilder().build());
	}

	public void sendGaScreen(final String name) {
		appTracker.setScreenName(name);
		appTracker.send(new HitBuilders.AppViewBuilder().build());
	}

	public void sendCommerce(String transactionId, String dealName, String sku, String category) {
		sendDataToTwoTrackers(new HitBuilders.ItemBuilder().setTransactionId(transactionId) // (String)
																							// Transaction
																							// ID
				.setName(dealName) // (String) Product name
				.setSku(sku) // (String) Product SKU
				.setCategory(category) // (String) Product category
				.setPrice(18.0d) // (Double) Product price
				.setQuantity(1L) // (Long) Product quantity
				.setCurrencyCode("KRW") // (String) Currency code
				.build());
	}

	private void sendDataToTwoTrackers(Map<String, String> params) {
		appTracker.send(params);
		// Tracker ecommerceTracker =
		// ((GlobalApplication)getApplication()).getTracker(TrackerName.ECOMMERCE_TRACKER);
		// ecommerceTracker.send(params);
	}

	public abstract void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag);

	public static class TumblrImgUpload extends AsyncTask<String, Void, PhotoPost> {
		Context mContext;
		WriteDialog mDialog;
		TumblrImgUpload mImgUpload;
		ProgressDialog mProgress;

		public boolean startImgUpload;

		public TumblrImgUpload(Context context, WriteDialog dialog) {
			super();
			mContext = context;
			mDialog = dialog;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			startImgUpload = true;
			if (mProgress == null) {
				mProgress = new ProgressDialog(mContext);
				mProgress.setMessage("이미지 업로딩중입니다...");
			}
			mProgress.show();

			if (mImgUpload != null) {
				boolean canCancel = mImgUpload.cancel(false);
				if (!canCancel) {
					this.cancel(true);
				}
			}
			mImgUpload = this;
		}

		@Override
		protected PhotoPost doInBackground(String... params) {
			JumblrClient client = new JumblrClient(TumblrOAuthActivity.CONSUMER_ID, TumblrOAuthActivity.CONSUMER_SECRET, params[0], params[1]);
			if (client.user().getBlogs() != null && client.user().getBlogs().size() > 0) {
				try {
					PhotoPost post = client.newPost(client.user().getBlogs().get(0).getName(), PhotoPost.class);
					post.setPhoto(new Photo(new File(params[2])));
					post.save();
					return (PhotoPost) client.blogPost(post.getBlogName(), post.getId());
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(PhotoPost result) {
			super.onPostExecute(result);
			startImgUpload = false;

			if (mProgress != null) {
				mProgress.dismiss();
			}

			if (mDialog != null && mDialog.isShowing()) {
				if (result != null && result.getPhotos() != null && result.getPhotos().size() > 0) {
					mDialog.addImageToContainer(result.getPhotos().get(0).getOriginalSize().getUrl());
					Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.error_msg_success_upload_image), Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.error_msg_failed_to_save_link), Toast.LENGTH_SHORT).show();
				}
			}

			mImgUpload = null;
		}
	}

}
