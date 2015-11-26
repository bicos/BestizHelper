package com.pockru.bestizhelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.DownloadManager;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebView.WebViewTransport;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pockru.bestizhelper.asynctask.ImgDownloadTask;
import com.pockru.bestizhelper.data.ArticleData;
import com.pockru.bestizhelper.data.ArticleDetailData;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.preference.Preference;
import com.pockru.utils.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BestizBoxDetailActivity extends BaseActivity {

	private static final String TAG = "BestizBoxDetailActivity";

	// private String BASE_SERVER_URL = "";
	// private String BASE_URL = "";
	// private String BOARD_ID = "";
	private String ARTICLE_NUMBER = "";

	private TextView tvName, tvSubject, tvHomepage, tvHit;

	private WebView wvContents;
	private RelativeLayout wvContainer;
	private LinearLayout llCommentInput;
	private EditText etComment;

	private ArticleData mArticleData;
	private String atcUrl = "";
	private boolean isLogin = false;
	protected String loginId;
	protected String loginPwd;
	private boolean isPause = false;
	private boolean isModify = false;
	private boolean isDelete = false;

	private LinearLayout layoutPopup;

	private ValueCallback<Uri> mUploadMessage;

	private BroadcastReceiver completeReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "다운로드가 완료되었습니다.",Toast.LENGTH_SHORT).show();
		}

	};

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		Intent intent = getIntent();
		if (intent == null) {
			finish();
		}
		
		mBoardData = (BoardData) intent.getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);
		if (mBoardData != null) {
			BASE_SERVER_URL = mBoardData.baseUrl;
			DETAIL_URL = mBoardData.id;
			BASE_URL = BASE_SERVER_URL.replace("/zboard.php", "");
			BOARD_ID = DETAIL_URL.replace("?id=", "");			
		}
		mArticleData = (ArticleData) intent.getSerializableExtra(Constants.INTENT_NAME_ARTICLE_DATA);
		
		getSupportActionBar().setTitle(mBoardData == null ? "" : mBoardData.name);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);

		atcUrl = intent.getStringExtra(Constants.INTENT_NAME_DETAIL_ARTICLE_URL);

		String params = atcUrl.substring(atcUrl.indexOf("?") + 1);
		ARTICLE_NUMBER = Utils.getURLParam(params, "no");

		isLogin = intent.getBooleanExtra(Constants.INTENT_NAME_IS_LOGIN, false);

		tvName = (TextView) findViewById(R.id.txt_user_name);
		tvSubject = (TextView) findViewById(R.id.txt_atc_subject);
		tvHomepage = (TextView) findViewById(R.id.txt_atc_homepage);
		tvHit = (TextView) findViewById(R.id.txt_atc_hit);

		layoutPopup = (LinearLayout) findViewById(R.id.layout_popup);

		wvContents = (WebView) findViewById(R.id.wb_contents);
		wvContainer = (RelativeLayout) findViewById(R.id.sv_parent);
		initWebView(wvContents);

		removeZoomController(wvContents);

		llCommentInput = (LinearLayout) findViewById(R.id.ll_comment_input);
		View containerArticleModify = findViewById(R.id.containerModify);

		if (isLogin) {
			llCommentInput.setVisibility(View.VISIBLE);
			containerArticleModify.setVisibility(View.VISIBLE);
		} else {
			llCommentInput.setVisibility(View.GONE);
			containerArticleModify.setVisibility(View.GONE);
		}

		etComment = (EditText) findViewById(R.id.et_comment);

		pb = (ProgressBar) findViewById(R.id.progressBar1);

		requestNetwork(Constants.FLAG_REQ_DETAIL_ARTICLE, atcUrl);
	}

	public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
		if (resCode != 200) {
			Toast.makeText(this, "통신 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (TextUtils.isEmpty(html)) {
			Toast.makeText(this, "통신 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

		switch (flag) {
		case Constants.FLAG_REQ_DETAIL_ARTICLE:
			setDetailArticleData(html);
			break;
		case Constants.FLAG_REQ_COMMENT:
			requestNetwork(Constants.FLAG_REQ_DETAIL_ARTICLE, atcUrl);
			etComment.setText("");
			break;
		case Constants.FLAG_REQ_DELETE_COMMENT:
			ShowdeleteCommentDialog(html);
			break;
		case Constants.FLAG_REQ_DELETE_COMMENT_OK:
			requestNetwork(Constants.FLAG_REQ_DETAIL_ARTICLE, atcUrl);
			break;
		case Constants.FLAG_REQ_LOGIN:
			nextLoginStep(html);
			break;
		case Constants.FLAG_REQ_LOGOUT:
			isLogin = false;
			// 메뉴 리프레시
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					supportInvalidateOptionsMenu();
				}
			});

			setAutoLogin(null, null, BASE_SERVER_URL);
			Toast.makeText(this, "로그아웃을 성공하였습니다.", Toast.LENGTH_SHORT).show();
			requestNetwork(Constants.FLAG_REQ_DETAIL_ARTICLE, atcUrl);
			break;
		case Constants.FLAG_REQ_DELETE:
			Toast.makeText(this, "게시물을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
			setResult(Constants.RESULT_REFRESH);
			finish();
			break;
		case Constants.FLAG_REQ_MODIFY:
			// try {
			// // createModifyDialog(new String(arg2, "EUC-KR"));
			// createModifyDialog(arg2);
			// } catch (UnsupportedEncodingException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			createModifyDialog(html);
			break;
		default:
			break;
		}
	}

	// todo : 만들어야함...
	private void createModifyDialog(String html) {
		findViewById(R.id.scroll_debug).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.tv_debug)).setText(html);
		Document doc = Jsoup.parse(html);
		String title = doc.getElementsByClass("input").attr("value");
		String body = doc.getElementsByClass("textarea").html();

		Log.i("test", "title : " + title + " , body : " + body);
	}

	private void ShowdeleteCommentDialog(String arg2) {
		// try {
		// Document doc = Jsoup.parse(new String(arg2, "EUC-KR"));
		Document doc = Jsoup.parse(arg2);
		final String no = doc.getElementsByAttributeValue("name", "no").attr("value");
		final String c_no = doc.getElementsByAttributeValue("name", "c_no").attr("value");

//		Log.i(TAG, "delete comment no : " + no + " , c_no : " + c_no);

		AlertDialog dlg = new AlertDialog.Builder(this).setTitle("Delete").setMessage("코멘트를 삭제하시겠습니까?")
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestNetwork(Constants.FLAG_REQ_DELETE_COMMENT_OK, BASE_URL + "/del_comment_ok.php", deleteComment(no, c_no));
					}
				}).setNegativeButton("취소", null).create();

		dlg.show();
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	private void setDetailArticleData(String html) {
		try {
			Document doc = Jsoup.parse(html);

			if (doc.select("img[src$=i_delete.gif]").size() > 0) {
				isDelete = true;
				findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
			} else {
				isDelete = false;
				findViewById(R.id.btn_delete).setVisibility(View.GONE);
			}

			// 이미지 리사이징
			doc.select("img").attr("width", "100%").attr("height", "auto");

			// 동영상 리사이징
			doc.select("embed").attr("width", "100%").attr("height", "auto");
			doc.select("iframe").attr("width", "100%").attr("height", "auto");

			// 2번째 엘레멘트 = 이름 , 홈페이지 , 제목 등이 들어가있는 엘레멘트
			Elements elements = doc.getElementsByAttributeValueContaining("bgcolor", "white");
			Element element = elements.get(1);

			ArticleDetailData data = new ArticleDetailData();
			data.setUserName(mArticleData.getAtcUser());

			if (!element.getElementsByAttributeValue("onfocus", "blur()").isEmpty()) {
				data.setModifyUrl(element.getElementsByAttributeValue("onfocus", "blur()").get(0).attr("href"));
				data.setDeleteUrl(element.getElementsByAttributeValue("onfocus", "blur()").get(1).attr("href"));
			}

			data.setUserHomepage(element.getElementsByAttributeValue("target", "_blank").attr("href"));

			data.setAtcSubject(element.select("b").text());
			data.setAtcHit("(hit : " + mArticleData.getAtcHit() + ")");

			// contents 셋팅
			elements = doc.getElementsByAttributeValueContaining("cellpadding", "10");
			element = elements.get(0);

			String val = "";
			for (Element e : elements.select("img")) { // img src attribute에 http 안붙는 예외 처리
				if (e != null && e.hasAttr("src")) {
					val = e.attr("src");
					if (!TextUtils.isEmpty(val) && (!val.startsWith("http:") && !val.startsWith("https:"))) {
						e.attr("src", "http:" + val);
					}
				}
			}

			String contents = element.getElementsByAttributeValue("style", "line-height:160%").toString();

			// comment 셋팅
			element = doc.getElementsByAttributeValueContaining("cellpadding", "3").get(0).attr("width", "100%");
			element.attr("bgcolor", "#f4f4f4");
			element.select("font").attr("size", "2");

			Elements tmpElements = element.select("a[onfocus=blur()]:not(:contains(*))");
			if (tmpElements != null && tmpElements.size() > 0) {
				tmpElements.remove();
			}

			Elements tr = element.select("tr");
			for (int i = 0; i < tr.size(); i++) {
				Elements td = tr.get(i).select("td");
				for (int j = 0; j < td.size(); j++) {
					switch (j) {
					case 0:
						// td.get(j).attr("width", "15%");
						break;
					case 1:
						td.get(j).attr("width", "80%");
						break;
					case 2:
						td.get(j).attr("width", "4%");
						break;
					case 3:
						td.get(j).attr("width", "1%");
						if (td.get(j).select("a") == null) {
							td.get(j).remove();
						}
					default:
						break;
					}
				}
			}

			// 삭제 링크 변경
			Elements atags = element.select("a");
			for (int i = 0; i < atags.size(); i++) {
				if (atags.get(i).attr("href").startsWith("del_comment.php")) {
					String tmp = BASE_URL + "/" + atags.get(i).attr("href");
					atags.get(i).attr("href", tmp);
				}
			}

			contents += element.toString();
			data.setAtcContents(contents);

			setCurrentLayout(data);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "게시물을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
			setResult(Constants.RESULT_REFRESH);
			finish();
			e.printStackTrace();
		}
	}

	private void nextLoginStep(String arg2) {
		// try {
		// Document doc = Jsoup.parse(new String(arg2, "EUC-KR"));
		Document doc = Jsoup.parse(arg2);
		String httpEquiv = doc.getElementsByAttribute("http-equiv").attr("http-equiv");
		String content = doc.getElementsByAttribute("content").attr("content");

		if ((httpEquiv != null && content != null) && (httpEquiv.equalsIgnoreCase("refresh"))) {
			isLogin = true;
			setAutoLogin(loginId, loginPwd, BASE_SERVER_URL);
			// BestizBoxApplication.getClientInstance().setCookieStore(cookieStore);
			Toast.makeText(this, "로그인을 성공했습니다.", Toast.LENGTH_SHORT).show();
		} else {
			isLogin = false;
			setAutoLogin(null, null, BASE_SERVER_URL);
			Toast.makeText(this, "로그인을 실패했습니다.", Toast.LENGTH_SHORT).show();
		}
		// 메뉴 리프레시
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				supportInvalidateOptionsMenu();
			}
		});
		requestNetwork(Constants.FLAG_REQ_DETAIL_ARTICLE, atcUrl);
		
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onResume() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (wvContents != null) {
				wvContents.onResume();
			}
		}

		IntentFilter completeFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		registerReceiver(completeReceiver, completeFilter);

		super.onResume();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onPause() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (wvContents != null) {
				wvContents.onPause();
			}
		}
		stopMediaPlayer();

		unregisterReceiver(completeReceiver);

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (wvContents != null) {
			wvContainer.removeView(wvContents);
			wvContents.removeAllViews();
			wvContents.destroy();
		}
		super.onDestroy();
	}

	private void stopMediaPlayer() {
		MediaPlayer player = new MediaPlayer();

		if (player.isPlaying()) {
			player.stop();
		}
	}

	@Override
	public void onBackPressed() {

		if (layoutPopup.getChildCount() > 0) {
			layoutPopup.removeAllViews();
			layoutPopup.setVisibility(View.GONE);
		} else if (wvContents.canGoBack()) {
			wvContents.goBack();
		} else {
			Intent intent = new Intent();
			intent.putExtra(Constants.INTENT_NAME_IS_LOGIN, isLogin);
			setResult(RESULT_OK, intent);
			super.onBackPressed();
		}
		
	}

	private void setCurrentLayout(ArticleDetailData data) {
		// 레이아웃이 바뀔때마다 디비를 업데이트한다.
//		ArticleDatabaseHelper.insertOrUpdate(this, ArticleDB.createInstance(mArticleData, data));
		
		tvSubject.setText(data.getAtcSubject());
		if (data.getUserHomepage() == null || data.getUserHomepage().equals("")) {
			findViewById(R.id.ll_hompage).setVisibility(View.GONE);
		} else {
			findViewById(R.id.ll_hompage).setVisibility(View.VISIBLE);
			tvHomepage.setText(data.getUserHomepage());
		}
		tvName.setText(data.getUserName());
		tvHit.setText(data.getAtcHit());
		wvContents.getSettings().setDefaultFontSize(14);
		wvContents.loadDataWithBaseURL(Uri.parse(atcUrl).getHost(), data.getAtcContents(), "text/html", "utf-8", atcUrl);
	}

	private ArrayList<NameValuePair> comment(String comment, String no) {
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
		return params;
	}

	private ArrayList<NameValuePair> deleteComment(String no, String c_no) {
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
		params.add(new BasicNameValuePair("c_no", c_no));
		return params;
	}

	private ArrayList<NameValuePair> delete(String no) {
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
		return params;
	}

	private ArrayList<NameValuePair> modify(String no) {
		// write.php?id=jd1404&page=1&sn1=&divpage=25&sn=off&ss=on&sc=off&select_arrange=headnum&desc=asc&no=191347&mode=modify
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("sn1", ""));
		params.add(new BasicNameValuePair("divpage", "25"));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("mode", "modify"));
		return params;
	}

	private ArrayList<NameValuePair> login(String id, String pwd) {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("auto_login", "0"));
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("s_url", "/zboard/zboard.php?id=" + BOARD_ID));
		try {
			params.add(new BasicNameValuePair("user_id", URLEncoder.encode(id, "utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			params.add(new BasicNameValuePair("password", URLEncoder.encode(pwd, "utf-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return params;

	}

	private ArrayList<NameValuePair> logout() {
		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("select_arrange", "headnum"));
		params.add(new BasicNameValuePair("desc", "asc"));
		params.add(new BasicNameValuePair("sn", "off"));
		params.add(new BasicNameValuePair("ss", "on"));
		params.add(new BasicNameValuePair("sc", "off"));
		params.add(new BasicNameValuePair("s_url", "/zboard/zboard.php?id=" + BOARD_ID));
		return params;
	}

	@SuppressLint("NewApi")
	@TargetApi(11)
	private void removeZoomController(WebView view) {
		if (android.os.Build.VERSION.SDK_INT > 11) {
			view.getSettings().setDisplayZoomControls(false);
		}
	}

	public void btnClick(View v) {
		switch (v.getId()) {
		case R.id.btn_modify:
			requestNetwork(Constants.FLAG_REQ_MODIFY, BASE_URL + "/write.php", modify(ARTICLE_NUMBER));
			break;
		case R.id.btn_delete:
			new AlertDialog.Builder(BestizBoxDetailActivity.this).setTitle("Delete").setMessage(R.string.msg_delete_article)
					.setPositiveButton("확인", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							requestNetwork(Constants.FLAG_REQ_DELETE, BASE_URL + "/delete_ok.php", delete(ARTICLE_NUMBER));
						}
					}).setNegativeButton("취소", null).show();
			break;
		case R.id.btn_comment:
			requestNetwork(Constants.FLAG_REQ_COMMENT, BASE_URL + "/comment_ok.php", comment(etComment.getText().toString(), ARTICLE_NUMBER));
			break;
		default:
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_detail, menu);
		// getMenuInflater().inflate(R.menu.menu_login, menu);
		// getMenuInflater().inflate(R.menu.menu_logout, menu);
		// getMenuInflater().inflate(R.menu.menu_home, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (menu == null) {
			return false;
		}

		/**
		 * 0 : login, 1 : logout, 2 : home
		 */
		if (isLogin) {// 로그인 상태일 경우
			menu.findItem(R.id.sub_menu_login).setVisible(false);
			// menu.findItem(R.id.menu_login).setVisible(false);
			menu.findItem(R.id.sub_menu_logout).setVisible(true);
			// menu.findItem(R.id.menu_logout).setVisible(true);
		} else {// 비로그인 상태일 경우
			menu.findItem(R.id.sub_menu_login).setVisible(true);
			// menu.findItem(R.id.menu_login).setVisible(true);
			menu.findItem(R.id.sub_menu_logout).setVisible(false);
			// menu.findItem(R.id.menu_logout).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_login:
		case R.id.sub_menu_login:
			final View loginView = Utils.getView(this, R.layout.layout_login);

			Utils.showCompositeDialog(this, getString(R.string.menu_login), loginView, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {

					EditText id = (EditText) loginView.findViewById(R.id.editText_login_id);
					EditText pwd = (EditText) loginView.findViewById(R.id.editText_login_pwd);

					loginId = id.getText().toString();
					loginPwd = pwd.getText().toString();

					requestNetwork(Constants.FLAG_REQ_LOGIN, BASE_URL + "/login_check.php", login(id.getText().toString(), pwd.getText().toString()));
				}
			});
			return true;

		case R.id.menu_logout:
		case R.id.sub_menu_logout:
			if (Preference.getAutoLogin(getApplicationContext())) {
				Preference.setAutoLogin(getApplicationContext(), false);
			}

			Utils.showAlternateAlertDialog(this, getString(R.string.menu_logout), getString(R.string.logout_msg_01), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					requestNetwork(Constants.FLAG_REQ_LOGOUT, BASE_URL + "/logout.php", logout());
				}
			});

			return true;
		case R.id.menu_home:
		case R.id.sub_menu_home:
			Intent intent = new Intent(BestizBoxDetailActivity.this, BoardSelectActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case Constants.REQ_FILECHOOSER:
			Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
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
				Log.i(TAG, "result : " + result.getType() + " , " + result.getExtra());

				int type = result.getType();

				if (type == HitTestResult.IMAGE_TYPE || type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
					Utils.showCustomListDialog(BestizBoxDetailActivity.this, result.getExtra(), getResources().getStringArray(R.array.photo_act_list),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case 0:
										// new ImgDownloadTask().execute(result);
										new ImgDownloadTask(BestizBoxDetailActivity.this).execute(result);
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
					Utils.showCustomListDialog(BestizBoxDetailActivity.this, result.getExtra(), getResources().getStringArray(R.array.link_list),
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

		// Setting Local Storage
		mWebView.getSettings().setDatabaseEnabled(true);
		mWebView.getSettings().setDomStorageEnabled(true);
		String databasePath = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
		// Log.i(TAG, "databasePath : "+databasePath);
		mWebView.getSettings().setDatabasePath(databasePath);

		mWebView.setDownloadListener(new CustomDownloadListener());
	}

	private class BestizBoxWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.i("ravy", "onPageStarted url : "+url);
			if (url.contains("about:blank")) {
				return;
			}

			if (url.contains("del_comment.php")) {
				requestNetwork(Constants.FLAG_REQ_DELETE_COMMENT, url);
				return;
			}

			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.i("ravy", "onPageFinished url : "+url);
			super.onPageFinished(view, url);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.i("ravy", "shouldOverrideUrlLoading url : "+url);
			
			if (url.contains("about:blank")) { // 예외상황 컨트롤
				return true;
			}

			if (url.contains("del_comment.php")) { // 예외상황 컨트롤
				requestNetwork(Constants.FLAG_REQ_DELETE_COMMENT, url);
				return true;
			}

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
	}

	private class BestizWebChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
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
			startActivityForResult(Intent.createChooser(i, "파일 선택"), Constants.REQ_FILECHOOSER);

		}

		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			WebView childeView = new WebView(BestizBoxDetailActivity.this);
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

	private void setAutoLogin(String id, String pwd, String baseUrl) {
		Log.i(TAG, "setAutoLogin id : " + id + " , pwd : " + pwd + " , basUrl : " + baseUrl);
		Preference.setAutoLogin(getApplicationContext(), true);
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

}
