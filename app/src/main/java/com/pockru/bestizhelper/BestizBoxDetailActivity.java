package com.pockru.bestizhelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import com.pockru.bestizhelper.data.ArticleDB;
import com.pockru.bestizhelper.data.ArticleDetailData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.data.UserData;
import com.pockru.bestizhelper.database.helper.ArticleDatabaseHelper;
import com.pockru.bestizhelper.database.helper.MemberDatabaseHelper;
import com.pockru.bestizhelper.dialog.WriteDialog;
import com.pockru.network.BestizParamsUtil;
import com.pockru.network.BestizUrlUtil;
import com.pockru.utils.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class BestizBoxDetailActivity extends BaseActivity {

    private static final String TAG = "BestizBoxDetailActivity";

    private String ARTICLE_NUMBER = "";

    private TextView tvName, tvSubject, tvHomepage, tvHit;

    private WebView wvContents;
    private RelativeLayout wvContainer;
    private EditText etComment;

    private ArticleDetailData mArticleDetailData;

    private String atcUrl = "";
    private boolean isLogin = false;
    protected String loginId;
    protected String loginPwd;
    private boolean isPause = false;
    private boolean isModify = false;
    private boolean isDelete = false;
    private boolean isWriteArticle = false;

    private LinearLayout layoutPopup;

    private ValueCallback<Uri> mUploadMessage;

    // 파이어버드 관련
//    private Firebase mRef;
    private UserData mUserData;

    // 아티클 db 관련
    ArticleDB articleDB;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        atcUrl = intent.getStringExtra(Constants.INTENT_NAME_DETAIL_ARTICLE_URL);
        isWriteArticle = intent.getBooleanExtra(Constants.INTENT_NAME_WRITE_ARTICLE, false);

        Uri uri = Uri.parse(atcUrl);
        if (uri != null) {
            BASE_SERVER_URL = uri.getScheme() + "://"+uri.getHost() + "/zboard";
            BOARD_ID = uri.getQueryParameter("id");
            ARTICLE_NUMBER = uri.getQueryParameter("no");
        }

        if (ARTICLE_NUMBER != null && TextUtils.isDigitsOnly(ARTICLE_NUMBER)) {
            articleDB = ArticleDatabaseHelper.getData(getApplicationContext(), Integer.valueOf(ARTICLE_NUMBER));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tvName = (TextView) findViewById(R.id.txt_user_name);
        tvSubject = (TextView) findViewById(R.id.txt_atc_subject);
        tvHomepage = (TextView) findViewById(R.id.txt_atc_homepage);
        tvHit = (TextView) findViewById(R.id.txt_atc_hit);

        layoutPopup = (LinearLayout) findViewById(R.id.layout_popup);

        wvContents = (WebView) findViewById(R.id.wb_contents);
        wvContainer = (RelativeLayout) findViewById(R.id.sv_parent);
        initWebView(wvContents);

        removeZoomController(wvContents);

        etComment = (EditText) findViewById(R.id.et_comment);

        pb = (ProgressBar) findViewById(R.id.progressBar1);

//        mRef = new Firebase(UrlConstants.FIREBASE_URL).child(BOARD_ID).child(ARTICLE_NUMBER);
        mUserData = MemberDatabaseHelper.getData(getApplicationContext(), BASE_SERVER_URL);

        requestNetwork(FLAG_REQ_DETAIL_ARTICLE, atcUrl);
    }

    public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
        if (resCode != 200 || TextUtils.isEmpty(html)) {
            Toast.makeText(this, "통신 중 오류가 발생하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (flag) {
            case FLAG_REQ_WRITE: // 리로딩
                requestNetwork(FLAG_REQ_DETAIL_ARTICLE, atcUrl);
                break;
            case FLAG_REQ_DETAIL_ARTICLE:
                setDetailArticleData(html);
                break;
            case FLAG_REQ_COMMENT:
                requestNetwork(FLAG_REQ_DETAIL_ARTICLE, atcUrl);
                etComment.setText("");
                break;
            case FLAG_REQ_DELETE_COMMENT:
                showDeleteCommentDialog(html);
                break;
            case FLAG_REQ_DELETE_COMMENT_OK:
                requestNetwork(FLAG_REQ_DETAIL_ARTICLE, atcUrl);
                break;
            case FLAG_REQ_LOGIN:
                nextLoginStep(html);
                break;
            case FLAG_REQ_LOGOUT:
                isLogin = false;
                // 메뉴 리프레시
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        supportInvalidateOptionsMenu();
                    }
                });

                MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);

                Toast.makeText(this, "로그아웃을 성공하였습니다.", Toast.LENGTH_SHORT).show();
                requestNetwork(FLAG_REQ_DETAIL_ARTICLE, atcUrl);
                break;
            case FLAG_REQ_DELETE:
                Toast.makeText(this, "게시물을 삭제하였습니다.", Toast.LENGTH_SHORT).show();
                setResult(Constants.RESULT_REFRESH);
                finish();
                break;
//		case Constants.FLAG_REQ_MODIFY:
//			showModifyDialog(html);
//			break;
            default:
                break;
        }
    }

    private WriteDialog dialog;

    private void showModifyDialog(String title, String body) {
        if (dialog == null) {
            dialog = new WriteDialog(this, BASE_SERVER_URL, BOARD_ID, ARTICLE_NUMBER);
        }
        dialog.setWriteTitle(title);
        dialog.setWriteBody(body);
        dialog.show();
    }

    private void showDeleteCommentDialog(String arg2) {
        Document doc = Jsoup.parse(arg2);
        final String no = doc.getElementsByAttributeValue("name", "no").attr("value");
        final String c_no = doc.getElementsByAttributeValue("name", "c_no").attr("value");

        AlertDialog dlg = new AlertDialog.Builder(this).setTitle("Delete").setMessage("코멘트를 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestNetwork(FLAG_REQ_DELETE_COMMENT_OK,
                                BestizUrlUtil.createCommentDeleteUrl(BASE_SERVER_URL),
                                BestizParamsUtil.createDeleteCommentParams(BOARD_ID, no, c_no));
                    }
                }).setNegativeButton("취소", null).create();

        dlg.show();
    }

    private void setDetailArticleData(String html) {
        try {
            Document doc = Jsoup.parse(html);

            if (doc.getElementsByTag("a").text().contains("ㅁLogin")) {
                isLogin = false;
            } else {
                isLogin = true;
            }

            if (doc.select("img[src$=i_delete.gif]").size() > 0) {
                isDelete = true;
                findViewById(R.id.btn_delete).setVisibility(View.VISIBLE);
            } else {
                isDelete = false;
                findViewById(R.id.btn_delete).setVisibility(View.GONE);
            }

            if (doc.select("img[src$=i_modify.gif]").size() > 0) {
                isModify = true;
                findViewById(R.id.btn_modify).setVisibility(View.VISIBLE);
            } else {
                isModify = false;
                findViewById(R.id.btn_modify).setVisibility(View.GONE);
            }

            // 이미지 리사이징
            doc.select("img").attr("width", "100%").attr("height", "auto");

            // 동영상 리사이징
            doc.select("embed").attr("width", "100%").attr("height", "auto");
            doc.select("iframe").attr("width", "100%").attr("height", "auto");

            // 2번째 엘레멘트 = 이름 , 홈페이지 , 제목 등이 들어가있는 엘레멘트
            Elements elements = doc.getElementsByAttributeValueContaining("bgcolor", "white");
            Element element = elements.get(1);

            mArticleDetailData = new ArticleDetailData();

            if (!element.getElementsByAttributeValue("onfocus", "blur()").isEmpty()) {
                mArticleDetailData.setModifyUrl(element.getElementsByAttributeValue("onfocus", "blur()").get(0).attr("href"));
                mArticleDetailData.setDeleteUrl(element.getElementsByAttributeValue("onfocus", "blur()").get(1).attr("href"));
            }

            mArticleDetailData.setUserName(element.select("tbody > tr > td:nth-child(1) > span").text());
            mArticleDetailData.setUserHomepage(element.getElementsByAttributeValue("target", "_blank").attr("href"));
            mArticleDetailData.setAtcSubject(element.select("b").text());
            mArticleDetailData.setAtcHit(element.select("tbody > tr > td:nth-child(2) > font").get(0).text());

            // contents 셋팅
            elements = doc.getElementsByAttributeValueContaining("cellpadding", "10");
            element = elements.get(0);

            String val;
            for (Element e : elements.select("img")) { // img src attribute에 http 안붙는 예외 처리
                if (e != null && e.hasAttr("src")) {
                    val = e.attr("src");
                    if (!TextUtils.isEmpty(val) && (!val.startsWith("http:") && !val.startsWith("https:"))) {
                        e.attr("src", "http:" + val);
                    }
                }
            }

            String contents = element.getElementsByAttributeValue("style", "line-height:160%").html();

            // 리얼 콘텐츠만 따로 저장
            mArticleDetailData.setAtcRealContents(contents.substring(0, contents.indexOf("<!--\"<-->")));

            // comment 셋팅
            element = doc.getElementsByAttributeValueContaining("cellpadding", "3").get(0).attr("width", "100%");
            element.attr("bgcolor", "#f4f4f4");
            element.select("font").attr("size", "2");

            Elements tmpElements = element.select("a[onfocus=blur()]:not(:contains(*))");
            if (tmpElements != null && tmpElements.size() > 0) {
                tmpElements.remove();
            }

            Elements tr = element.select("tr");
            mArticleDetailData.setCommentCnt(tr.size());
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
                    String tmp = BASE_SERVER_URL + "/" + atags.get(i).attr("href");
                    atags.get(i).attr("href", tmp);
                }
            }

            contents += element.toString();
            mArticleDetailData.setAtcContents(contents);

            setCurrentLayout(mArticleDetailData);

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
            UserData data = new UserData(loginId, loginPwd, BASE_SERVER_URL);
            MemberDatabaseHelper.insertOrUpdate(getApplicationContext(), data);
            Toast.makeText(this, "로그인을 성공했습니다.", Toast.LENGTH_SHORT).show();
        } else {
            isLogin = false;
            Toast.makeText(this, "로그인을 실패했습니다.", Toast.LENGTH_SHORT).show();
        }
        // 메뉴 리프레시
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                supportInvalidateOptionsMenu();
            }
        });
        requestNetwork(FLAG_REQ_DETAIL_ARTICLE, atcUrl);
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
//        // 해당 글이 자신이 쓴 글이라면 디비를 업데이트한다.
        if (isWriteArticle) {
            data.setArticleType(ArticleDB.TYPE_WRITE);
            ArticleDatabaseHelper.insertOrUpdate(this, ArticleDB.createInstance(data, atcUrl));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(data.getAtcSubject());
        }

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
        wvContents.loadDataWithBaseURL(null, data.getAtcContents(), "text/html", "utf-8", null);
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
                showModifyDialog(mArticleDetailData.getAtcSubject(), mArticleDetailData.getAtcRealContents());
                break;
            case R.id.btn_delete:
                new AlertDialog.Builder(BestizBoxDetailActivity.this).
                        setTitle("Delete").
                        setMessage(R.string.msg_delete_article).
                        setPositiveButton("확인", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                requestNetwork(FLAG_REQ_DELETE,
                                        BestizUrlUtil.createArticleDeleteUrl(BASE_SERVER_URL),
                                        BestizParamsUtil.createDeleteParams(BOARD_ID, ARTICLE_NUMBER));
                            }
                        }).
                        setNegativeButton("취소", null).show();
                break;
            case R.id.btn_comment:
                if (isLogin) {
                    String comment = etComment.getText().toString();
                    if (mUserData != null) {
//                    mRef.push().setValue(new CommentData(mUserData.name, comment));
                    }

                    requestNetwork(FLAG_REQ_COMMENT,
                            BestizUrlUtil.createCommentWriteUrl(BASE_SERVER_URL),
                            BestizParamsUtil.createWriteCommentParams(BOARD_ID, ARTICLE_NUMBER, comment));
                } else {
                    Utils.showAlternateAlertDialog(BestizBoxDetailActivity.this,
                            "알림",
                            "로그인을 하셔야 코멘트를 쓰실 수 있습니다. 로그인을 하시겠습니까?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showLoginDialog();
                                }
                            });
                }
                break;
            default:
                break;
        }

    }

    private void showLoginDialog() {
        final View loginView = Utils.getView(this, R.layout.layout_login);
        
        Utils.showCompositeDialog(this, getString(R.string.menu_login), loginView, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                EditText id = (EditText) loginView.findViewById(R.id.editText_login_id);
                EditText pwd = (EditText) loginView.findViewById(R.id.editText_login_pwd);

                loginId = id.getText().toString();
                loginPwd = pwd.getText().toString();

                requestNetwork(FLAG_REQ_LOGIN,
                        BestizUrlUtil.createLoginUrl(BASE_SERVER_URL),
                        BestizParamsUtil.createLoginParams(BOARD_ID, id.getText().toString(), pwd.getText().toString()));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null) {
            return false;
        }

        if (isLogin) {// 로그인 상태일 경우
            menu.findItem(R.id.sub_menu_login).setVisible(false);
            menu.findItem(R.id.sub_menu_logout).setVisible(true);
        } else {// 비로그인 상태일 경우
            menu.findItem(R.id.sub_menu_login).setVisible(true);
            menu.findItem(R.id.sub_menu_logout).setVisible(false);
        }

        MenuItem favoriteMenu = menu.findItem(R.id.sub_menu_favorite);
        favoriteMenu.setCheckable(true);
        favoriteMenu.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int updateType;
                    if (item.isChecked()) {
                        item.setChecked(false);
                        item.setIcon(R.drawable.ic_favorite_border_black_24dp);
                        articleDB.articleType = articleDB.articleType & ~ArticleDB.TYPE_FAVORITE;
                        Toast.makeText(getApplicationContext(), "해당 게시물이 즐겨찾기 해제되었습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        item.setChecked(true);
                        item.setIcon(R.drawable.ic_favorite_black_24dp);
                        articleDB.articleType = articleDB.articleType | ArticleDB.TYPE_FAVORITE;
                        Toast.makeText(getApplicationContext(), "해당 게시물이 즐겨찾기 되었습니다.", Toast.LENGTH_LONG).show();
                    }

                    ArticleDatabaseHelper.insertOrUpdate(getApplicationContext(),articleDB);

                return false;
            }
        });

        if (articleDB != null) {
            favoriteMenu.setChecked((articleDB.articleType & ArticleDB.TYPE_FAVORITE) == ArticleDB.TYPE_FAVORITE);
            favoriteMenu.setIcon((articleDB.articleType & ArticleDB.TYPE_FAVORITE) == ArticleDB.TYPE_FAVORITE ?
                    R.drawable.ic_favorite_black_24dp :
                    R.drawable.ic_favorite_border_black_24dp);
        } else {
            favoriteMenu.setChecked(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
            case R.id.sub_menu_login:
                showLoginDialog();
                return true;
            case R.id.menu_logout:
            case R.id.sub_menu_logout:
                Utils.showAlternateAlertDialog(this, getString(R.string.menu_logout), getString(R.string.logout_msg_01), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestNetwork(FLAG_REQ_LOGOUT,
                                BestizUrlUtil.createLogoutUrl(BASE_SERVER_URL),
                                BestizParamsUtil.createLogoutParams(BOARD_ID));
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

        if (dialog != null) {
            dialog.onActivityResult(requestCode, resultCode, data);
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
            if (url.contains("about:blank")) {
                return;
            }

            if (url.contains("del_comment.php")) {
                requestNetwork(FLAG_REQ_DELETE_COMMENT, url);
                return;
            }

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url.contains("about:blank")) { // 예외상황 컨트롤
                return true;
            }

            if (url.contains("del_comment.php")) { // 예외상황 컨트롤
                requestNetwork(FLAG_REQ_DELETE_COMMENT, url);
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

    public static void startDetailActivity(Activity activity, String atcUrl, int reqCode, boolean isWriteArticle){
        Intent intent = new Intent(activity, BestizBoxDetailActivity.class);
        intent.putExtra(Constants.INTENT_NAME_DETAIL_ARTICLE_URL, atcUrl);
        intent.putExtra(Constants.INTENT_NAME_WRITE_ARTICLE, isWriteArticle);
        activity.startActivityForResult(intent, reqCode);
    }
}
