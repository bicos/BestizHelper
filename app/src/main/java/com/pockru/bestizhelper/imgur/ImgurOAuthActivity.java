package com.pockru.bestizhelper.imgur;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.pockru.bestizhelper.R;
import com.pockru.preference.Preference;
import com.pockru.utils.Utils;

/**
 * @author bicos
 *
 */
public class ImgurOAuthActivity extends Activity {

	private static final String TAG = "ImgurOAuthActivity";

	public static final String CONSUMER_ID = "a2d09c8b7d1ceff";
	public static final String CONSUMER_SECRET = "4d942362651286c76e6f837cfde1abf9c7b9d6df";

	// public static final String REQUEST_URL =
	// "http://api.imgur.com/oauth/request_token";
	// public static final String ACCESS_URL =
	// "http://api.imgur.com/oauth/access_token";
	// public static final String AUTHORIZE_URL =
	// "http://api.imgur.com/oauth/authorize";

	public static final String REQUEST_URL = "https://api.imgur.com/oauth2/addclient";
	public static final String ACCESS_URL = "https://api.imgur.com/oauth2/token";
	public static final String AUTHORIZE_URL = "https://api.imgur.com/oauth2/authorize";

	public static final String OAUTH_CALLBACK_SCHEME = "bestizbox-imgur";
	public static final String OAUTH_CALLBACK_HOST = "callback";
	public static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

	private CommonsHttpOAuthConsumer consumer;
	private CommonsHttpOAuthProvider provider;

	private String token, secret, authURL, verifier;

	private WebView mWebView;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mWebView.loadUrl((String) msg.obj);
				break;
			case 1:
				if (consumer.getToken() != null || consumer.getTokenSecret() != null) {
					Preference.setImgurToken(ImgurOAuthActivity.this, consumer.getToken());
					Preference.setImgurSecret(ImgurOAuthActivity.this, consumer.getTokenSecret());

					Toast.makeText(ImgurOAuthActivity.this, getString(R.string.toast_msg_imgur_01), Toast.LENGTH_SHORT).show();

				} else {
					Toast.makeText(ImgurOAuthActivity.this, getString(R.string.error_msg_cant_interlock), Toast.LENGTH_SHORT).show();
				}
				finish();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};;

	@Override
	protected void onResume() {
		mWebView.resumeTimers();
		super.onResume();
	}

	@Override
	protected void onPause() {
		mWebView.pauseTimers();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		mWebView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.imgur_activity);

		consumer = new CommonsHttpOAuthConsumer(CONSUMER_ID, CONSUMER_SECRET);
		provider = new CommonsHttpOAuthProvider(REQUEST_URL, ACCESS_URL, AUTHORIZE_URL);

		token = Preference.getImgurToken(this);
		secret = Preference.getImgurSecret(this);

		mWebView = (WebView) findViewById(R.id.webView);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.setWebChromeClient(new WebChromeClient());
		mWebView.setWebViewClient(new CallBack());

		if (token.equals("") || secret.equals("")) {
			setAuthURL();
			Log.e(TAG, "token null || secret null");
		} else {
			Toast.makeText(this, getString(R.string.error_msg_already_get_token), Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private void setAuthURL() {
		new Thread() {
			@Override
			public void run() {
				try {
					authURL = provider.retrieveRequestToken(consumer, OAUTH_CALLBACK_URL);
				} catch (OAuthMessageSignerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OAuthNotAuthorizedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message msg = mHandler.obtainMessage(0, authURL);
				mHandler.sendMessage(msg);

				super.run();
			}
		}.start();
	}

	private class CallBack extends WebViewClient {

		ProgressDialog dialog = new ProgressDialog(ImgurOAuthActivity.this);

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// Log.e(TAG, "url : "+url);

			if (url.startsWith(OAUTH_CALLBACK_URL)) {
				Log.e(TAG, "-------------OAUTH_CALLBACK_URL----------------");

				String params = url.substring(url.indexOf("?") + 1);
				verifier = Utils.getURLParam(params, "oauth_token");

				new Thread() {
					public void run() {
						try {
							provider.retrieveAccessToken(consumer, verifier);

						} catch (OAuthMessageSignerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (OAuthNotAuthorizedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (OAuthExpectationFailedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (OAuthCommunicationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						mHandler.sendEmptyMessage(1);
					};
				}.start();
			} else {
				return super.shouldOverrideUrlLoading(view, url);
				// Log.e("DAY", "shouldOverrideUrlLoading!");
			}
			return true;
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);

			// Log.e("DAY", "onLoadResource!");
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			// Log.e("DAY", "onReceivedError!");

		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			dialog.setMessage("Loading page . . .");
			if (!dialog.isShowing())
				dialog.show();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);

			// Log.e("DAY", "onPageFinished!");
			dialog.dismiss();

		}
	}

}
