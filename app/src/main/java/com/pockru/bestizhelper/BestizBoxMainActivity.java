package com.pockru.bestizhelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.support.v7.view.SupportMenuInflater;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.pockru.bestizhelper.asynctask.ImgDownloadTask;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.data.UserData;
import com.pockru.bestizhelper.database.helper.MemberDatabaseHelper;
import com.pockru.bestizhelper.dialog.WriteDialog;
import com.pockru.network.BestizParamsUtil;
import com.pockru.network.BestizUrlUtil;
import com.pockru.utils.Utils;

import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BestizBoxMainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    WebView mWebView;

    ProgressBar b;

    private String privUrl = "";

    private String no = "";

    int pageNum = 1;

    private String sn = "", ss = "", sc = "";

    private String keyword;

    List<String> postNumList;

    boolean saveUrl = false;

    boolean isLogin = false;

    boolean isWriteComment = false;

    boolean isDeletable = false;

    private LinearLayout layoutPopup;

    private RelativeLayout webViewContainer;

    private ValueCallback<Uri> mUploadMessage;
    protected boolean tryLogin;
    protected String loginId;
    protected String loginPwd;

    private WriteDialog mWriteDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBoardData = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);
        if (mBoardData != null) {
            BASE_SERVER_URL = mBoardData.baseUrl;
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

        mWebView.postUrl(BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID),
                URLEncodedUtils.format(BestizParamsUtil.createMovePageParams(BOARD_ID,
                        page,
                        keyword,
                        sn,
                        ss,
                        sc),
                        "euc-kr").getBytes());
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
        mWebView.postUrl(BestizUrlUtil.createArticleDeleteUrl(BASE_SERVER_URL),
                URLEncodedUtils.format(BestizParamsUtil.createDeleteParams(BOARD_ID,no), "euc-kr")
                        .getBytes());
    }

    private void comment(String comment, String no) {
        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(privUrl);

        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", privUrl);
        extraHeaders.put("Set-Cookie", cookie);

        mWebView.loadUrl(BestizUrlUtil.createCommentWriteUrl(BASE_SERVER_URL,
                BestizParamsUtil.createWriteCommentParams(BOARD_ID, no, comment)),
                extraHeaders);
    }

    private void setAutoLogin(String id, String pwd, String baseUrl) {
        UserData data = new UserData(id, pwd, baseUrl);
        MemberDatabaseHelper.insertOrUpdate(getApplicationContext(), data);
    }

    private void login(String id, String pwd) {

        requestNetwork(FLAG_REQ_LOGIN,
                BestizUrlUtil.createLoginUrl(BASE_SERVER_URL),
                BestizParamsUtil.createLoginParams(BOARD_ID, id, pwd));
    }

    private void logout() {
        mWebView.postUrl(BestizUrlUtil.createLogoutUrl(BASE_SERVER_URL),
                URLEncodedUtils.format(BestizParamsUtil.createLogoutParams(BOARD_ID),
                        "euc-kr").getBytes());
    }

    private void search(boolean sn, boolean ss, boolean sc, String keyword) {
        mWebView.postUrl(BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID),
                URLEncodedUtils.format(BestizParamsUtil.createMovePageParams(
                        BOARD_ID,
                        "1",
                        keyword,
                        sn ? "on" : "off",
                        ss ? "on" : "off",
                        sc ? "on" : "off"), "euc-kr").getBytes());
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

                mWriteDialog = new WriteDialog(BestizBoxMainActivity.this, BASE_SERVER_URL, BOARD_ID, no);
                mWriteDialog.show();

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

        switch (requestCode) {
            case REQ_FILECHOOSER:
                Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
                break;
            default:
                break;
        }

        if (mWriteDialog != null) {
            mWriteDialog.onActivityResult(requestCode, resultCode, data);
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

            if (TextUtils.isEmpty(url) == false) {
                Uri uri = Uri.parse(url);

                if (uri != null) {
                    no = uri.getQueryParameter("no");
                    keyword = uri.getQueryParameter("keyword");
                    sn = uri.getQueryParameter("sn");
                    ss = uri.getQueryParameter("ss");
                    sc = uri.getQueryParameter("sc");
                } else {
                    no = "";
                    keyword = "";
                    sn="";
                    ss= "";
                    sc = "";
                }
            }

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

            } else if (url.contains("/write_ok.php")) {
                saveUrl = true;
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

    @Override
    public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
        switch (flag) {
            case FLAG_REQ_WRITE:
                mWebView.reload();
                break;
            case FLAG_REQ_LOGIN:
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

}
