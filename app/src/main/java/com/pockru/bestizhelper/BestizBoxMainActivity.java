package com.pockru.bestizhelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.view.SupportMenuInflater;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebView.WebViewTransport;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pockru.bestizhelper.adapter.ArticleListAdapter;
import com.pockru.bestizhelper.asynctask.ImgDownloadTask;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.data.ImageData;
import com.pockru.bestizhelper.data.UserData;
import com.pockru.bestizhelper.database.helper.MemberDatabaseHelper;
import com.pockru.bestizhelper.tumblr.TumblrOAuthActivity;
import com.pockru.network.BestizUrlUtil;
import com.pockru.preference.Preference;
import com.pockru.utils.Utils;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BestizBoxMainActivity extends BaseActivity {

	private static final String TAG = "MainActivity";

	private static final int REQ_CODE_GET_PHOTO = 100;
//	private static final int REQ_CODE_IMG_UPLOAD = 101;
	private static final int REQ_FILECHOOSER = 102;
	private static final int REQ_WRITE_POST = 103;
	private static final int REQ_LOG_IN = 104;
	
	private static final int REQ_CODE_TUMBLR_AUTH = 105;

	WebView mWebView;
	ProgressDialog d;
	ProgressBar b;
	Handler handler;

	private String privUrl = "";

	private String no = "";

	private String imgUrl = "", imgId = "", deleteHash = "";

	private ArrayList<ImageData> imgList = new ArrayList<ImageData>();

	int pageNum = 1;

	private String sn = "", ss = "", sc = "";

	private String keyword;

	private View writeView;

	List<String> postNumList;

	boolean saveUrl = false;

	boolean isLogin = false;

	boolean isWriteComment = false;

	boolean isDeletable = false;

	private LinearLayout layoutPopup;

	private RelativeLayout webViewContainer;

	private HorizontalScrollView hsvImage;

	private LinearLayout containerImg;
	
	private boolean startImgUpload;

	private ValueCallback<Uri> mUploadMessage;
	protected boolean tryLogin;
	protected String loginId;
	protected String loginPwd;

	private ListView lvMain;
	private ArticleListAdapter mAdapter;

	private BroadcastReceiver completeReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "다운로드가 완료되었습니다.",Toast.LENGTH_SHORT).show();
		}

	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBoardData = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);
		if (mBoardData != null) {
			BASE_SERVER_URL = mBoardData.baseUrl;
//			DETAIL_URL = mBoardData.id;
			BOARD_ID = mBoardData.id;
		}
		
		getSupportActionBar().setTitle(mBoardData == null ? "" : mBoardData.name);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		layoutPopup = (LinearLayout) findViewById(R.id.layout_popup);
		webViewContainer = (RelativeLayout) findViewById(R.id.layout_webview_mode);
		
		postNumList = new ArrayList<String>();

		b = (ProgressBar) findViewById(R.id.pbAddress);
		
		pb = (ProgressBar) findViewById(R.id.progressBar1);

		mWebView = (WebView) findViewById(R.id.webView1);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
			CookieManager.setAcceptFileSchemeCookies(true);
		}
		
		initWebView(mWebView);

		UserData data = MemberDatabaseHelper.getData(getApplicationContext(), BASE_SERVER_URL);

		if (data != null) {
			Log.i(TAG, "auto login true");
			login(data.id, data.pwd);
		} else {
			Log.i(TAG, "auto login false");
			mWebView.loadUrl(BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
		}
	}

	private void initWebView(final WebView mWebView) {
		mWebView.setWebViewClient(new BestizBoxWebViewClient());
		mWebView.setWebChromeClient(new BestizWebChromeClient());

		// image click
		mWebView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				vibrator.vibrate(1);

				final HitTestResult result = mWebView.getHitTestResult();
//				Log.i(TAG, "result : " + result.getType() + " , " + result.getExtra());

				int type = result.getType();

				if (type == HitTestResult.IMAGE_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
					Utils.showCustomListDialog(BestizBoxMainActivity.this, result.getExtra(), getResources().getStringArray(R.array.photo_act_list),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										new ImgDownloadTask(BestizBoxMainActivity.this).execute(result);
										break;
									case 1:
										saveUrl(result.getExtra());
										break;
									default:
										break;
									}
								}

							});
				} else if (type == HitTestResult.SRC_ANCHOR_TYPE) {
					Utils.showCustomListDialog(BestizBoxMainActivity.this, result.getExtra(), getResources().getStringArray(R.array.link_list),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										saveUrl(result.getExtra());
									default:
										break;
									}
								}

							});
				}

				return false;
			}
		});

		mWebView.setLongClickable(true);

		mWebView.getSettings().setSupportZoom(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setPluginState(PluginState.ON);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		// mWebView.getSettings().setSupportMultipleWindows(true);
		removeZoomController(mWebView);
		// mWebView.getSettings().setLoadWithOverviewMode(true);
		mWebView.getSettings().setUseWideViewPort(true);
		mWebView.getSettings().setDefaultTextEncodingName("euc-kr");
		if (18 < Build.VERSION.SDK_INT) {
			// 캐시 사용안함
			mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		}

		// Setting Local Storage
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setDomStorageEnabled(true);
		String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		// Log.i(TAG, "databasePath : "+databasePath);
		mWebView.getSettings().setDatabasePath(databasePath);

		mWebView.addJavascriptInterface(new JIFace(), "droid");

		mWebView.setDownloadListener(new CustomDownloadListener());
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void saveUrl(String extra) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			ClipboardManager mgr = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData date = ClipData.newPlainText(extra, extra);
			mgr.addPrimaryClipChangedListener(new OnPrimaryClipChangedListener() {

				@Override
				public void onPrimaryClipChanged() {
					Toast.makeText(getApplicationContext(), getString(R.string.msg_save_link_url), Toast.LENGTH_SHORT).show();
				}
			});
			mgr.setPrimaryClip(date);
		} else {
			android.text.ClipboardManager mgr = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			mgr.setText(extra);
			Toast.makeText(getApplicationContext(), getString(R.string.msg_save_link_url), Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressLint("NewApi")
	@TargetApi(11)
	private void removeZoomController(WebView view) {
		if (android.os.Build.VERSION.SDK_INT > 11) {
			view.getSettings().setDisplayZoomControls(false);
		}
	}

	@Override
	public void onBackPressed() {

		if (layoutPopup.getChildCount() > 0) {

			layoutPopup.removeAllViews();
			layoutPopup.setVisibility(View.GONE);

		} else {

			if (mWebView.canGoBack())
				mWebView.goBack();
			else
				super.onBackPressed();
		}
	}

	public void movePage(View v) {
		switch (v.getId()) {
		case R.id.btn_left:
			if (!(pageNum == 1))
				pageNum--;

			movePage(String.valueOf(pageNum), keyword, sn, ss, sc);
			break;
		case R.id.btn_right:
			pageNum++;
			movePage(String.valueOf(pageNum), keyword, sn, ss, sc);
			break;

		default:
			break;
		}
	}

	private void movePage(String page, String keyword, String sn, String ss, String sc) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", page));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("category", ""));
		params.add(new BasicNameValuePair("sn", sn));
		params.add(new BasicNameValuePair("ss", ss));
		params.add(new BasicNameValuePair("sc", sc));
		params.add(new BasicNameValuePair("divpage", "15"));
		if (keyword != null)
			params.add(new BasicNameValuePair("keyword", keyword));

		mWebView.postUrl(BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID),
				URLEncodedUtils.format(params, "euc-kr").getBytes());
	}

	private void write(String title, String contents) {

		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("no", ""));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("page_num", ""));
		params.add(new BasicNameValuePair("keyword", ""));
		params.add(new BasicNameValuePair("category", ""));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("mode", "write"));
		params.add(new BasicNameValuePair("category", "1"));
		params.add(new BasicNameValuePair("use_html", "1"));
		params.add(new BasicNameValuePair("subject", title));
		params.add(new BasicNameValuePair("memo", contents));
		
		privUrl = BASE_SERVER_URL + "/write.php";
		requestNetwork(REQ_WRITE_POST, BestizUrlUtil.createArticleWriteUrl(BASE_SERVER_URL), params);
		
//		mWebView.postUrl(BASE_URL + "/write_ok.php", EncodingUtils.getBytes(URLEncodedUtils.format(params, "euc-kr"), "BASE64"));
	}

	// private void memo() {
	// ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
	// params.add(new BasicNameValuePair("id", inputToId.getText().toString()));
	// params.add(new BasicNameValuePair("member_no", inputFromId.getText()
	// .toString()));
	// params.add(new BasicNameValuePair("kind", "6"));
	// params.add(new BasicNameValuePair("subject", inputSubject.getText()
	// .toString()));
	// params.add(new BasicNameValuePair("html", "0"));
	// params.add(new BasicNameValuePair("memo", inputMemo.getText()
	// .toString()));
	// byte[] result = HttpPostSender.getInstance().sendPost(
	// "http://bestjd.bestiz.net/zboard/send_message.php", params);
	// try {
	// response.setText(new String(result, "euc_kr"));
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	private void delete(String no) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("page_num", "25"));
		params.add(new BasicNameValuePair("keyword", ""));
		params.add(new BasicNameValuePair("category", ""));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("mode", ""));

		mWebView.postUrl(BestizUrlUtil.createArticleDeleteUrl(BASE_SERVER_URL), URLEncodedUtils.format(params, "euc-kr").getBytes());
	}

	private void comment(String comment, String no) {
		CookieSyncManager.createInstance(this);
		CookieManager cookieManager = CookieManager.getInstance();
		String cookie = cookieManager.getCookie(privUrl);

		Map<String, String> extraHeaders = new HashMap<String, String>();
		extraHeaders.put("Referer", privUrl);
		extraHeaders.put("Set-Cookie", cookie);

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("page_num", "25"));
		params.add(new BasicNameValuePair("keyword", ""));
		params.add(new BasicNameValuePair("category", ""));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("mode", ""));
		params.add(new BasicNameValuePair("memo", comment));

		mWebView.loadUrl(BestizUrlUtil.createCommentWriteUrl(BASE_SERVER_URL, params), extraHeaders);
	}

	private void setAutoLogin(String id, String pwd, String baseUrl) {
		UserData data = new UserData(id, pwd, baseUrl);
		MemberDatabaseHelper.insertOrUpdate(getApplicationContext(), data);
	}

	private void login(String id, String pwd) {

		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("auto_login", "0"));
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("s_url", "/zboard/zboard.php?id=" + BOARD_ID));
		params.add(new BasicNameValuePair("user_id", id));
		params.add(new BasicNameValuePair("password", pwd));

		requestNetwork(REQ_LOG_IN, BestizUrlUtil.createLoginUrl(BASE_SERVER_URL), params);
	}

	private void logout() {
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("s_url", "/zboard/zboard.php?id=" + BOARD_ID));
		mWebView.postUrl(BestizUrlUtil.createLogoutUrl(BASE_SERVER_URL), URLEncodedUtils.format(params, "euc-kr").getBytes());
	}

	private void search(boolean sn, boolean ss, boolean sc, String keyword) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("sn", sn ? "on" : "off"));
		params.add(new BasicNameValuePair("ss", ss ? "on" : "off"));
		params.add(new BasicNameValuePair("sc", sc ? "on" : "off"));
		params.add(new BasicNameValuePair("keyword", keyword));
		mWebView.postUrl(BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID), URLEncodedUtils.format(params, "euc-kr").getBytes());
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_login:
		case R.id.sub_menu_login:
			final View loginView = Utils.getView(this, R.layout.layout_login);

			Utils.showCompositeDialog(this, getString(R.string.menu_login), loginView, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

					EditText id = (EditText) loginView.findViewById(R.id.editText_login_id);
					EditText pwd = (EditText) loginView.findViewById(R.id.editText_login_pwd);
					tryLogin = true;
					loginId = id.getText().toString();
					loginPwd = pwd.getText().toString();

					login(id.getText().toString(), pwd.getText().toString());
				}
			});
			return true;
		case R.id.menu_write:
		case R.id.sub_menu_write:

			if (imgList.size() > 0) {
				imgList.clear();
			}

			writeView = Utils.getView(this, R.layout.layout_write);

			hsvImage = (HorizontalScrollView) writeView.findViewById(R.id.hsvImage);
			containerImg = (LinearLayout) writeView.findViewById(R.id.containerImg);

			Button btnImgAdd = (Button) writeView.findViewById(R.id.button_img_add);
			btnImgAdd.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (TextUtils.isEmpty(Preference.getTumblrToken(BestizBoxMainActivity.this)) || TextUtils.isEmpty(Preference.getTumblrSecret(BestizBoxMainActivity.this))) {
						startActivityForResult(new Intent(BestizBoxMainActivity.this, TumblrOAuthActivity.class), REQ_CODE_TUMBLR_AUTH);
					} else {
						if (Utils.isOverCurrentAndroidVersion(VERSION_CODES.KITKAT) >= 0) {
							Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(intent, REQ_CODE_GET_PHOTO);
						} else {
							Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							startActivityForResult(intent, REQ_CODE_GET_PHOTO);
						}
					}
				}
			});

			Utils.showCompositeDialog(this, getString(R.string.menu_write), writeView, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					EditText contents = (EditText) writeView.findViewById(R.id.editText_contents);
					final EditText subject = (EditText) writeView.findViewById(R.id.editText_subject);

					String tmp = "";

					for (int i = 0; i < imgList.size(); i++) {
						if (imgList.get(i).is1024over) {
							tmp += "<img src=\"" + imgList.get(i).imgUrl + "\"" + " width=\"1024\"><br><br>";
						} else {
							tmp += "<img src=\"" + imgList.get(i).imgUrl + "\"><br><br>";
						}
					}

					final String totalContents = (!tmp.equals("")) ? tmp + contents.getText().toString() : contents.getText().toString();
					if (startImgUpload) {
						Utils.showAlternateAlertDialog(BestizBoxMainActivity.this, getString(R.string.menu_write),
								getString(R.string.alert_msg_still_img_upload), new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										if (which == DialogInterface.BUTTON_POSITIVE) {
											write(subject.getText().toString(), totalContents);
										}
									}
								});

					} else {
						write(subject.getText().toString(), totalContents);
					}

					if (imgList.size() > 0) {
						imgList.clear();
					}

				}
			});
			return true;
		case R.id.menu_comment:
		case R.id.sub_menu_comment:
			if (!mWebView.getUrl().contains("/view.php")) {
				Utils.showBasicAlertDialog(this, getString(R.string.dialog_default_title), getString(R.string.error_msg_cant_write_comment));
				return true;
			}

			final View commentView = Utils.getView(this, R.layout.layout_comment);
			if (no != "") {
				Utils.showCompositeDialog(this, getString(R.string.menu_comment), commentView, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						EditText comment = (EditText) commentView.findViewById(R.id.editText_comment);

						comment(comment.getText().toString(), no);
					}
				});
			}
			return true;
		case R.id.menu_delete:
		case R.id.sub_menu_delete:
			if (!isDeletable) {
				Utils.showBasicAlertDialog(this, getString(R.string.dialog_default_title), getString(R.string.error_msg_cant_delete_post));
				return true;
			} else {
				Utils.showAlternateAlertDialog(this, getString(R.string.menu_delete), getString(R.string.delete_msg_01), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						delete(no);
					}
				});
			}
			return true;

		case R.id.menu_refresh:
		case R.id.sub_menu_refresh:
			// mWebView.loadUrl("http://bestjd.bestiz.net/zboard/zboard.php?id=jd1211&page=1");
			// mWebView.loadUrl(BASE_SERVER_URL + DETAIL_URL);
			mWebView.reload();
			return true;
		case R.id.menu_logout:
		case R.id.sub_menu_logout:
			Utils.showAlternateAlertDialog(this, getString(R.string.menu_logout), getString(R.string.logout_msg_01), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					logout();
				}
			});

			return true;
		case R.id.menu_home:
		case R.id.sub_menu_home:
			Intent intent = new Intent(BestizBoxMainActivity.this, BoardSelectActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			finish();
			return true;

		case R.id.menu_search:
		case R.id.sub_menu_search:
			final View searchView = getLayoutInflater().inflate(R.layout.layout_search, null);

			Utils.showCompositeDialog(this, getString(R.string.menu_search), searchView, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					CheckBox cbName = (CheckBox) searchView.findViewById(R.id.cbName);
					CheckBox cbSubject = (CheckBox) searchView.findViewById(R.id.cbSubject);
					CheckBox cbContents = (CheckBox) searchView.findViewById(R.id.cbContents);
					EditText etSearch = (EditText) searchView.findViewById(R.id.etSearch);

					search(cbName.isChecked(), cbSubject.isChecked(), cbContents.isChecked(), etSearch.getText().toString());
				}
			});

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	boolean isPause = false;

	@Override
	protected void onResume() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mWebView != null && isPause) {
				mWebView.onResume();
				isPause = false;
			}
		}

		IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(completeReceiver, completeFilter);

		super.onResume();
	}

	@Override
	protected void onPause() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (mWebView != null) {
				mWebView.onPause();
				isPause = true;
			}
		}
		stopMediaPlayer();

		unregisterReceiver(completeReceiver);

		super.onPause();
	}

	private void stopMediaPlayer() {
		MediaPlayer player = new MediaPlayer();

		if (player.isPlaying()) {
			player.stop();
		}
	}

	@Override
	protected void onDestroy() {
		if (mWebView != null) {
			webViewContainer.removeView(mWebView);
			mWebView.removeAllViews();
			mWebView.destroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;

		switch (requestCode) {
		case REQ_CODE_GET_PHOTO:
			if (writeView != null && data != null) {
				startImgUpload = true;
				mUplaodUri = data.getData();
				uploadPictures(Preference.getTumblrToken(BestizBoxMainActivity.this), Preference.getTumblrSecret(BestizBoxMainActivity.this));
			}
			break;
		case REQ_CODE_TUMBLR_AUTH:
			if (Utils.isOverCurrentAndroidVersion(VERSION_CODES.KITKAT) >= 0) {
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, REQ_CODE_GET_PHOTO);
			} else {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(intent, REQ_CODE_GET_PHOTO);
			}
			break;
		case REQ_FILECHOOSER:
			Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SupportMenuInflater inflater = new SupportMenuInflater(this);
		inflater.inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu == null) {
			return false;
		}

		/**
		 * 1 : login 2 : logout 3 : write 4 : comment 5 : delete 6 : refresh 7 :
		 * search
		 */
//		if (isLogin) {
//			menu.findItem(R.id.sub_menu_login).setVisible(false);
//			menu.findItem(R.id.sub_menu_logout).setVisible(true);
//			menu.findItem(R.id.sub_menu_write).setVisible(true);
//			menu.findItem(R.id.sub_menu_delete).setVisible(true);
//			menu.findItem(R.id.sub_menu_comment).setVisible(true);
//		} else {
//			menu.findItem(R.id.sub_menu_login).setVisible(true);
//			menu.findItem(R.id.sub_menu_logout).setVisible(false);
//			menu.findItem(R.id.sub_menu_write).setVisible(false);
//			menu.findItem(R.id.sub_menu_delete).setVisible(false);
//			menu.findItem(R.id.sub_menu_comment).setVisible(false);
//		}

		 if (isLogin) {
			 menu.findItem(R.id.sub_menu_login).setVisible(false);
			 menu.findItem(R.id.sub_menu_logout).setVisible(true);
			 menu.findItem(R.id.sub_menu_write).setVisible(true);

			 if (isWriteComment)
				 menu.findItem(R.id.sub_menu_comment).setVisible(true);
			 else
				 menu.findItem(R.id.sub_menu_comment).setVisible(false);

			 if (isDeletable)
				 menu.findItem(R.id.sub_menu_delete).setVisible(true);
			 else
				 menu.findItem(R.id.sub_menu_delete).setVisible(false);

		 } else {
			 menu.findItem(R.id.sub_menu_login).setVisible(true);
			 menu.findItem(R.id.sub_menu_logout).setVisible(false);
			 menu.findItem(R.id.sub_menu_write).setVisible(false);
			 menu.findItem(R.id.sub_menu_delete).setVisible(false);
			 menu.findItem(R.id.sub_menu_comment).setVisible(false);
		 }

		return true;
	}

	private class BestizBoxWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
			if (url.startsWith("http:") || url.startsWith("https:") || url.startsWith("javascript:")) {
				return super.shouldOverrideUrlLoading(view, url);
			} else {
				Intent intent = null;
				try {
					intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
					startActivity(intent);
				} catch (URISyntaxException e) {
					handleURISyntaxException(e);
					e.printStackTrace();
				} catch (ActivityNotFoundException e) {
					handleActivityNotFoundException(e, intent);
					e.printStackTrace();
				}
				return true;
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			b.setProgress(0);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			privUrl = url;

//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//				CookieManager.getInstance().flush();				
//			} else {
//				CookieSyncManager.getInstance().sync();
//			}

			// logCookie(url);

			String ht = "javascript:window.droid.print(document.getElementsByTagName('html')[0].innerHTML);";
			view.loadUrl(ht);

			if (url.contains("/logout.php")) {
				isLogin = false;
				MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);
			} else if (url.contains("/view.php")) {
				String params = url.substring(url.indexOf("?") + 1);
				no = Utils.getURLParam(params, "no");

				isWriteComment = true;

				if (saveUrl) {
					postNumList.add(no);
					saveUrl = false;
				}

				for (String number : postNumList) {
					if (url.contains(number)) {
						isDeletable = true;
					}
				}
			}
			// Search 관련
			else if (url.contains("/zboard.php")) {
				String params = url.substring(url.indexOf("?") + 1, url.length());
				
				keyword = Utils.getURLParam(params, "keyword");
				
				sn = Utils.getURLParam(params, "sn");
				ss = Utils.getURLParam(params, "ss");
				sc = Utils.getURLParam(params, "sc");
			} else if (url.contains("/write_ok.php")) {
				saveUrl = true;
				imgUrl = "";
			} else if (url.contains("/delete_ok.php")) {
				postNumList.remove(no);
				isDeletable = false;
			} else {
				if (isWriteComment)
					isWriteComment = false;
			}
			super.onPageFinished(view, url);
		}
	}

	private class BestizWebChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (newProgress == 100) {
				b.setProgress(0);
			} else {
				b.setProgress(newProgress);
			}
		}

		public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
			openFileChooser(uploadFile);
		}

		public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
			openFileChooser(uploadMsg, "");
		}

		public void openFileChooser(ValueCallback<Uri> uploadMsg) {

			mUploadMessage = uploadMsg;
			Intent i = new Intent(Intent.ACTION_GET_CONTENT);
			i.addCategory(Intent.CATEGORY_OPENABLE);
			i.setType("*/*");
			startActivityForResult(Intent.createChooser(i, "파일 선택"), REQ_FILECHOOSER);

		}

		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			WebView childeView = new WebView(BestizBoxMainActivity.this);
			initWebView(childeView);
			childeView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
			layoutPopup.setVisibility(View.VISIBLE);
			layoutPopup.addView(childeView);
			WebView.WebViewTransport transport = (WebViewTransport) resultMsg.obj;
			transport.setWebView(childeView);
			resultMsg.sendToTarget();
			return true;
		}

		@Override
		public void onCloseWindow(WebView window) {
			layoutPopup.setVisibility(View.GONE);
			window.destroy();
		}
	}

	class CustomDownloadListener implements DownloadListener {
		public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimetype, final long contentLength) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
	}

	class JIFace {
		@JavascriptInterface
		public void print(String data) {
			data = "<html>" + data + "</html>";

			if (data.contains("ㅁLogout")) {

				if (tryLogin) {
					setAutoLogin(loginId, loginPwd, BASE_SERVER_URL);
					tryLogin = false;
					loginId = "";
					loginPwd = "";
				}
				isLogin = true;
			} else {
				isLogin = false;
			}
			
			Document doc = Jsoup.parse(data);
			pageNum = getSelectedPageNum(doc);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					supportInvalidateOptionsMenu();
				}
			});
		}
	}


	private String getHost(String url) {
		if (url == null)
			return "";

		int start = url.indexOf('/');
		start = url.indexOf('/', start + 1) + 1;
		int end = url.indexOf('/', start);

		if (start < 0 || end < 0)
			return url;

		String domain = url.substring(start, end);
		return domain;
	}

	@Override
	public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
		switch (flag) {
		case REQ_WRITE_POST:
			mWebView.reload();
			break;
		case REQ_LOG_IN:
			Toast.makeText(BestizBoxMainActivity.this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show();
			mWebView.loadUrl(BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
			break;

		default:
			break;
		}
	}

	private void handleURISyntaxException(URISyntaxException e) {
		Toast.makeText(this, this.getString(R.string.msg_illegal_uri_syntax), Toast.LENGTH_LONG).show();
	}

	private void handleActivityNotFoundException(ActivityNotFoundException e, Intent intent) {
		String packageName = intent.getPackage();
		if (packageName != null && !packageName.equals("")) {
			Toast.makeText(this, this.getString(R.string.msg_not_supported_url), Toast.LENGTH_LONG).show();
			Intent goMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(this.getString(R.string.url_go_market) + packageName));
			this.startActivity(goMarket);
		} else {
			Toast.makeText(this, this.getString(R.string.msg_not_supported_url), Toast.LENGTH_LONG).show();
		}
	}
	
	private int getSelectedPageNum(Document doc) {
		Elements elements = doc.getElementsByAttributeValue("colspan", "2").select("a[href],b");

		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).hasAttr("href") == false) {
				try {
					int pageNum = Integer.parseInt(elements.get(i).text());
					return pageNum;
				} catch (Exception e) {
					e.printStackTrace();
					return 1;
				}
			}
		}

		return 1;
	}
	
	Uri mUplaodUri;
	
	private void uploadPictures(String token, String secret) {
		new TumblrImgUpload().execute(token, secret, Utils.getRealPathFromURI(mUplaodUri, BestizBoxMainActivity.this));
	}
	
	class TumblrImgUpload extends AsyncTask<String, Void, PhotoPost> {
		TumblrImgUpload mImgUpload;
		ProgressDialog mProgress;
		
		public TumblrImgUpload() {
			super();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (mProgress == null) {
				mProgress = new ProgressDialog(BestizBoxMainActivity.this);
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
			JumblrClient client = new JumblrClient(
					TumblrOAuthActivity.CONSUMER_ID, 
					TumblrOAuthActivity.CONSUMER_SECRET, 
					params[0], 
					params[1]);
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
			if (mProgress != null) {
				mProgress.dismiss();
			}
			mImgUpload = null;
			
			if (result != null && result.getPhotos() != null && result.getPhotos().size() > 0) {
				addImageToContainer(result.getPhotos().get(0).getOriginalSize().getUrl());
				Toast.makeText(BestizBoxMainActivity.this, getString(R.string.error_msg_success_upload_image), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(BestizBoxMainActivity.this, getString(R.string.error_msg_failed_to_save_link), Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	private void addImageToContainer(final String imgUrl) {
		if (writeView != null) {
			hsvImage.setVisibility(View.VISIBLE);
			startImgUpload = false;
			
			final ImageView iv = new ImageView(BestizBoxMainActivity.this);
			iv.setAdjustViewBounds(true);

			LayoutParams params = new LayoutParams((int) getResources().getDimension(R.dimen.img_default_size), (int) getResources().getDimension(
					R.dimen.img_default_size));
			iv.setLayoutParams(params);
			iv.setPadding(5, 0, 5, 0);
			Glide.with(BestizBoxMainActivity.this).load(imgUrl).into(new SimpleTarget<GlideDrawable>(params.width, params.height) {

				@Override
				public void onResourceReady(GlideDrawable arg0, GlideAnimation<? super GlideDrawable> arg1) {
					if (arg0 != null) {
						ImageData data = new ImageData(imgUrl, false);
						imgList.add(data);
						iv.setImageDrawable(arg0);
					}
				}
			});
			containerImg.addView(iv);
		}
	}
}
