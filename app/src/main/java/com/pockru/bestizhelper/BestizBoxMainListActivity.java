package com.pockru.bestizhelper;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.internal.view.SupportMenuInflater;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.model.people.Person.Gender;
import com.pockru.bestizhelper.adapter.ArticleListAdapter;
import com.pockru.bestizhelper.data.ArticleData;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.data.ImageData;
import com.pockru.bestizhelper.data.UserData;
import com.pockru.bestizhelper.database.helper.MemberDatabaseHelper;
import com.pockru.bestizhelper.tumblr.TumblrOAuthActivity;
import com.pockru.preference.Preference;
import com.pockru.utils.UiUtils;
import com.pockru.utils.Utils;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BestizBoxMainListActivity extends BaseActivity {

    private BoardData mBoardData;
    private String BASE_SERVER_URL;
    private String BASE_URL;
    private String DETAIL_URL;
    private String BOARD_ID;
    private static final String TAG = "MainActivity";

    private static final int REQ_CODE_GET_PHOTO = 100;
    public static final int REQ_CODE_DETAIL_ARTICLE = 103;
    private static final int REQ_CODE_TUMBLR_AUTH = 104;

    protected static final int FLAG_REQ_MAIN_ARTICLE = 1000;
    protected static final int FLAG_REQ_NEXT_ARTICLE = 1001;
    protected static final int FLAG_REQ_LOGIN = 1003;
    protected static final int FLAG_REQ_WRITE = 1004;
    protected static final int FLAG_REQ_LOGOUT = 1005;
    protected static final int FLAG_REQ_SEARCH = 1006;

    Handler handler;

    private String no = "";

    private ArrayList<ImageData> imgList = new ArrayList<ImageData>();

    int pageNum = 1;

    private String sn = "", ss = "", sc = "";

    private String keyword;

    private View writeView;

    List<String> postNumList;

    boolean saveUrl = false;

    boolean isLogin = false;

    int CurrentIndex = 0;

    private HorizontalScrollView hsvImage;

    private LinearLayout containerImg;
    private boolean startImgUpload;

    protected boolean tryLogin;
    protected String loginId;
    protected String loginPwd;

    private SwipeRefreshLayout mSwipeMain;
    private ListView mListMain;
    private ArticleListAdapter mAdapter;

    private AdView adView;

    private Button btnWrite;
    private int maxTansY, transY;

    //  로그인 후 로직 제어
    private boolean isShowWriteDialog = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mBoardData = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);
        if (mBoardData != null) {
            BASE_SERVER_URL = mBoardData.baseUrl;
            DETAIL_URL = mBoardData.id;
            BASE_URL = BASE_SERVER_URL.replace("/zboard.php", "");
            BOARD_ID = DETAIL_URL.replace("?id=", "");
        }

        getSupportActionBar().setTitle(mBoardData == null ? "" : mBoardData.name);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // ArrayList<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        // params.add(new BasicNameValuePair("id", BOARD_ID));

        // cookieStore.clear();

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.ad_unit_id));
        adView.setAdSize(AdSize.BANNER);

        LinearLayout layout = (LinearLayout) findViewById(R.id.adViewContainer);
        layout.addView(adView);

        AdRequest request = new AdRequest.Builder().setGender(Gender.FEMALE).build();
        adView.loadAd(request);

        mSwipeMain = (SwipeRefreshLayout) findViewById(R.id.swipe_main);
        mListMain = (ListView) findViewById(R.id.lv_main);
        mAdapter = new ArticleListAdapter(this);
        mListMain.setAdapter(mAdapter);
        mListMain.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                Intent intent = new Intent(BestizBoxMainListActivity.this, BestizBoxDetailActivity.class);
                intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, mBoardData);
                intent.putExtra(Constants.INTENT_NAME_DETAIL_ARTICLE_URL, BASE_URL + "/" + ((String) arg1.findViewById(R.id.txt_main_atc_title).getTag()));
                intent.putExtra(Constants.INTENT_NAME_BASE_URL, BASE_URL);
                intent.putExtra(Constants.INTENT_NAME_BOARD_ID, BOARD_ID);
                intent.putExtra(Constants.INTENT_NAME_IS_LOGIN, isLogin);
                intent.putExtra(Constants.INTENT_NAME_BASE_SERVER_URL, BASE_SERVER_URL);
                intent.putExtra(Constants.INTENT_NAME_ARTICLE_DATA, (ArticleData) mAdapter.getItem(arg2));
                startActivityForResult(intent, REQ_CODE_DETAIL_ARTICLE);
            }
        });

        // 스크롤 셋팅
        mListMain.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount > 0 && firstVisibleItem + visibleItemCount >= (totalItemCount - 5) && isRequestNetwork == false) {
                    pageNum++;
                    requestNetwork(FLAG_REQ_NEXT_ARTICLE, BASE_SERVER_URL, movePage(String.valueOf(pageNum), keyword, sn, ss, sc));
                }

//				computeScrollPosition(view);
            }
        });
        mListMain.setOnTouchListener(new OnTouchListener() {
            private int prevSrollY;
            private boolean isScrollUp = true;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    prevSrollY = (int) event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    int moveScroll = (int) (prevSrollY - event.getY());
                    prevSrollY = (int) event.getY();

                    computeWriteBtnPosition(moveScroll);
                    if (moveScroll > 0) {
                        isScrollUp = true;
                    } else {
                        isScrollUp = false;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    animWriteBtnPosition(isScrollUp);
                }

                return false;
            }
        });


        // 리프레쉬 셋팅
        // mSwipeMain.setColorSchemeColors(android.R.color.holo_blue_bright,
        // android.R.color.holo_green_light,
        // android.R.color.holo_orange_light,
        // android.R.color.holo_red_light);
        mSwipeMain.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                pageNum = 1;
                requestNetwork(FLAG_REQ_MAIN_ARTICLE, BASE_SERVER_URL + DETAIL_URL);
            }
        });

        // 버튼 셋팅
        btnWrite = (Button) findViewById(R.id.btn_write);
        btnWrite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLogin) {
                    showWriteDialog();
                } else {
                    Utils.showAlternateAlertDialog(BestizBoxMainListActivity.this,
                            "알림",
                            "로그인을 하셔야 글을 쓰실 수 있습니다. 로그인을 하시겠습니까?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isShowWriteDialog = true;
                                    showLoginDialog();
                                }
                            });
                }
            }
        });

        btnWrite.post(new Runnable() {

            @Override
            public void run() {
                int[] locations = new int[2];
                btnWrite.getLocationOnScreen(locations);
                maxTansY = UiUtils.getDisplayHeight(BestizBoxMainListActivity.this) - locations[1];
            }
        });

        postNumList = new ArrayList<String>();

        pb = (ProgressBar) findViewById(R.id.progressBar1);

        Log.e(TAG, "BASE_SERVER_URL + DETAIL_URL : " + BASE_SERVER_URL + DETAIL_URL);

        UserData data = MemberDatabaseHelper.getData(getApplicationContext(), BASE_SERVER_URL);

        if (data != null) {
            loginId = data.id;
            loginPwd = data.pwd;
            Log.i(TAG, "auto login data = " + data);
            requestNetwork(FLAG_REQ_LOGIN, BASE_URL + Constants.URL_LOGIN, login(data.id, data.pwd));
        } else {
            Log.i(TAG, "auto login false");
            requestNetwork(FLAG_REQ_MAIN_ARTICLE, BASE_SERVER_URL + DETAIL_URL);
        }

    }

    private void computeWriteBtnPosition(int moveScroll) {
        transY += moveScroll;

        if (transY < 0) {
            transY = 0;
        } else if (transY > maxTansY) {
            transY = maxTansY;
        }

        if (btnWrite != null) {
            ViewCompat.setTranslationY(btnWrite, transY);
        }
    }

    private void animWriteBtnPosition(boolean isScrollUp) {
        if (isScrollUp) {
            ViewCompat.animate(btnWrite).translationY(maxTansY).setDuration(200).start();
            transY = maxTansY;
        } else {
            ViewCompat.animate(btnWrite).translationY(0).setDuration(200).start();
            transY = 0;
        }
    }

    @Override
    protected void onPause() {
        adView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        adView.resume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.removeAllViews();
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
        mSwipeMain.setRefreshing(false);

        if (resCode != 200) {
            Toast.makeText(getApplicationContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (html == null) {
            Toast.makeText(getApplicationContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (flag) {
            case FLAG_REQ_MAIN_ARTICLE:
                setMainArticleList(html);
                break;
            case FLAG_REQ_NEXT_ARTICLE:
                setNextArticleList(html);
                break;
            case FLAG_REQ_LOGIN:
                nextLoginStep(html);
                break;
            case FLAG_REQ_WRITE:
                requestNetwork(FLAG_REQ_MAIN_ARTICLE, BASE_SERVER_URL + DETAIL_URL);
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
//                setAutoLogin(null, null, BASE_SERVER_URL);

                // db에서 해당 데이터 제거
                MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);

                Toast.makeText(this, getString(R.string.msg_logout_success), Toast.LENGTH_SHORT).show();
                requestNetwork(FLAG_REQ_MAIN_ARTICLE, BASE_SERVER_URL + DETAIL_URL);
                break;
            case FLAG_REQ_SEARCH:
                setMainArticleList(html);
                break;
            default:
                break;
        }
    }

    private void nextLoginStep(String arg2) {
        Document doc = Jsoup.parse(arg2);
        String httpEquiv = doc.getElementsByAttribute("http-equiv").attr("http-equiv");
        String content = doc.getElementsByAttribute("content").attr("content");

        Log.i(TAG, "httpEquiv : " + httpEquiv + " , content : " + content);

        if ((httpEquiv != null && content != null) && (httpEquiv.equalsIgnoreCase("refresh"))) { // 로그인 성공
            isLogin = true;
//            setAutoLogin(loginId, loginPwd, BASE_SERVER_URL);
            // BestizBoxApplication.getClientInstance().setCookieStore(cookieStore);

            // login값 셋팅
            UserData data = new UserData(loginId, loginPwd, BASE_SERVER_URL);
            MemberDatabaseHelper.insertOrUpdate(getApplicationContext(), data);

			Toast.makeText(this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show();

            if (isShowWriteDialog) { //  로그인을 성공한 경우에만 다음 작업 진행
                showWriteDialog();
            }
        } else {
            isLogin = false;
//            setAutoLogin(null, null, BASE_SERVER_URL);
            MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);

//            Toast.makeText(this, getString(R.string.msg_login_failed), Toast.LENGTH_SHORT).show();
        }

        // 메뉴 리프레시
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                supportInvalidateOptionsMenu();
            }
        });
        requestNetwork(FLAG_REQ_MAIN_ARTICLE, BASE_SERVER_URL + DETAIL_URL);

        if (isShowWriteDialog) {
            isShowWriteDialog = false;
        }
    }

    private void setNextArticleList(String arg) {
        setMainArticleList(arg, false);
    }

    private void setMainArticleList(String arg) {
        setMainArticleList(arg, true);
    }

    private void setMainArticleList(String arg, boolean isFirst) {

        String params = privUrl.substring(privUrl.indexOf("?") + 1, privUrl.length());

        if (!Utils.getURLParam(params, "page").equals(""))
            pageNum = Integer.parseInt(Utils.getURLParam(params, "page"));

        keyword = Utils.getURLParam(params, "keyword");
        Log.i(TAG, "keyword : " + keyword);
        sn = Utils.getURLParam(params, "sn");
        ss = Utils.getURLParam(params, "ss");
        sc = Utils.getURLParam(params, "sc");

        ArticleData data;
        ArrayList<ArticleData> dataList = new ArrayList<ArticleData>();
        Document doc = Jsoup.parse(arg);
        Elements elements = doc.getElementsByAttributeValueContaining("onMouseOver", "this.style.backgroundColor='#F9F9F9'");
        Element element;

        int size = elements.size();

        for (int i = 0; i < size; i++) {
            data = new ArticleData();
            element = elements.get(i);
            // data.setAtcNum(element.getElementsByAttributeValueContaining("class",
            // "listnum").get(0).text());
            data.setAtcDate(element.getElementsByAttributeValueContaining("class", "listnum").get(1).text());
            data.setAtcHit(element.getElementsByAttributeValueContaining("class", "listnum").get(2).text());
            data.setAtcVote("+" + element.getElementsByAttributeValueContaining("class", "listnum").get(3).text());
            data.setAtcLink(element.getElementsByAttributeValueContaining("style", "word-break:break-all;").get(0).getElementsByAttribute("href").attr("href"));
            data.setAtcTitle(element.getElementsByAttributeValueContaining("style", "word-break:break-all;").text());
            data.setAtcUser(element.getElementsByAttribute("nowrap").text());

            dataList.add(data);
        }

		/* 사이즈가 0일때 하단 로직 실행 안함 */
        if (dataList.size() == 0) {
            return;
        }

        if (isFirst) {
            mAdapter.setDataList(dataList);
        } else {

			/* 중복 제거 */
            ArrayList<ArticleData> prevDataList = (ArrayList<ArticleData>) mAdapter.getDataList();
            ArrayList<ArticleData> totalDataList = new ArrayList<ArticleData>(prevDataList);

            boolean isAdded = false;
            int max = dataList.size();
            for (int i = 0; i < max; i++) {
                if (totalDataList.contains(dataList.get(i)) == false) {
                    totalDataList.add(dataList.get(i));
                    isAdded = true;
                }
            }

            if (isAdded == false) {
                pageNum++;
                requestNetwork(FLAG_REQ_NEXT_ARTICLE, BASE_SERVER_URL, movePage(String.valueOf(pageNum), keyword, sn, ss, sc));
            } else {
                mAdapter.setDataList(totalDataList);
            }
            // mAdapter.addAllData(dataList);
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

    private ArrayList<NameValuePair> movePage(String page, String keyword, String sn, String ss, String sc) {
        // RequestParams params = new RequestParams();
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("page", page));
        params.add(new BasicNameValuePair("id", BOARD_ID));
        params.add(new BasicNameValuePair("no", no));
        params.add(new BasicNameValuePair("select_arrange", "headnum"));
        params.add(new BasicNameValuePair("desc", "asc"));
        params.add(new BasicNameValuePair("category", ""));
        params.add(new BasicNameValuePair("sn", sn));
        params.add(new BasicNameValuePair("ss", ss));
        params.add(new BasicNameValuePair("sn", sn));
        params.add(new BasicNameValuePair("sc", sc));
        params.add(new BasicNameValuePair("divpage", "15"));
        if (keyword != null)
            params.add(new BasicNameValuePair("keyword", keyword));

        return params;
    }

    private ArrayList<NameValuePair> write(String title, String contents) {
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
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

        return params;
    }

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
    }

//    private void setAutoLogin(String id, String pwd, String baseUrl) {
//        Log.i(TAG, "setAutoLogin id : " + id + " , pwd : " + pwd + " , basUrl : " + baseUrl);
//
//        if (baseUrl.contains(Constants.SERVER_01_URL)) {
//            Preference.setServer1Id(getApplicationContext(), id);
//            Preference.setServer1Pwd(getApplicationContext(), pwd);
//        } else if (baseUrl.contains(Constants.SERVER_02_URL)) {
//            Preference.setServer2Id(getApplicationContext(), id);
//            Preference.setServer2Pwd(getApplicationContext(), pwd);
//        } else if (baseUrl.contains(Constants.SERVER_03_URL)) {
//            Preference.setServer3Id(getApplicationContext(), id);
//            Preference.setServer3Pwd(getApplicationContext(), pwd);
//        } else if (baseUrl.contains(Constants.SERVER_04_URL)) {
//            Preference.setServer4Id(getApplicationContext(), id);
//            Preference.setServer4Pwd(getApplicationContext(), pwd);
//        } else if (baseUrl.contains(Constants.SERVER_05_URL)) {
//            Preference.setServer5Id(getApplicationContext(), id);
//            Preference.setServer5Pwd(getApplicationContext(), pwd);
//        }
//
//        Preference.setAutoLogin(getApplicationContext(), true);
//    }

    private ArrayList<NameValuePair> login(String id, String pwd) {

        // RequestParams params = new RequestParams();
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
        params.add(new BasicNameValuePair("user_id", id));
        params.add(new BasicNameValuePair("password", pwd));
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

    private ArrayList<NameValuePair> search(boolean sn, boolean ss, boolean sc, String keyword) {

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
        return params;
    }

    private ArrayList<NameValuePair> memberInfo(){
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("group_no", "1"));
        return params;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SupportMenuInflater inflater = new SupportMenuInflater(this);
        inflater.inflate(R.menu.menu_main_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu == null) {
            return false;
        }

        /**
         * 0 : login, 1 : logout, 2 : write, 3 : home, 4 : search
         */
        if (isLogin) {// 로그인 상태일 경우
            menu.findItem(R.id.sub_menu_login).setVisible(false);
            menu.findItem(R.id.sub_menu_logout).setVisible(true);
            menu.findItem(R.id.sub_menu_write).setVisible(true);
        } else {// 비로그인 상태일 경우
            menu.findItem(R.id.sub_menu_login).setVisible(true);
            menu.findItem(R.id.sub_menu_logout).setVisible(false);
            menu.findItem(R.id.sub_menu_write).setVisible(false);
        }
        // menu.findItem(R.id.sub_menu_history).setVisible(true);

        return true;
    }

    @SuppressLint("NewApi")
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login:
            case R.id.sub_menu_login:
                showLoginDialog();
                return true;
            case R.id.menu_write:
            case R.id.sub_menu_write:
                showWriteDialog();
                return true;

            case R.id.menu_logout:
            case R.id.sub_menu_logout:
//                if (Preference.getAutoLogin(getApplicationContext())) {
//                    Preference.setAutoLogin(getApplicationContext(), false);
//                }
                MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);

                Utils.showAlternateAlertDialog(this, getString(R.string.menu_logout), getString(R.string.logout_msg_01), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestNetwork(FLAG_REQ_LOGOUT, BASE_URL + Constants.URL_LOGOUT, logout());
                    }
                });

                return true;
            case R.id.menu_home:
            case R.id.sub_menu_home:
                Intent intent = new Intent(BestizBoxMainListActivity.this, BoardSelectActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;

            case R.id.menu_search:
            case R.id.sub_menu_search:
                Intent search = new Intent(this, BestizBoxSearchActivity.class);
                search.putExtra(Constants.INTENT_NAME_BOARD_DATA, mBoardData);
                search.putExtra(Constants.INTENT_NAME_IS_LOGIN, isLogin);
                startActivity(search);
                return true;

//            case R.id.sub_menu_history:
//                Intent history = new Intent(this, ArticleHistoryActivity.class);
//                startActivity(history);
//                return true;
        }

        return false;
    }

    private void showLoginDialog() {
        final View loginView = Utils.getView(this, R.layout.layout_login);

        Utils.showCompositeDialog(this,
                getString(R.string.menu_login),
                loginView,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        EditText id = (EditText) loginView.findViewById(R.id.editText_login_id);
                        EditText pwd = (EditText) loginView.findViewById(R.id.editText_login_pwd);

                        loginId = id.getText().toString();
                        loginPwd = pwd.getText().toString();

                        requestNetwork(FLAG_REQ_LOGIN, BASE_URL + Constants.URL_LOGIN, login(id.getText().toString(), pwd.getText().toString()));
                    }
                },
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (isShowWriteDialog) isShowWriteDialog = false;
                    }
                }
        );
    }

    private void showWriteDialog(){
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
                if (TextUtils.isEmpty(Preference.getTumblrToken(BestizBoxMainListActivity.this))
                        || TextUtils.isEmpty(Preference.getTumblrSecret(BestizBoxMainListActivity.this))) {
                    startActivityForResult(new Intent(BestizBoxMainListActivity.this, TumblrOAuthActivity.class), REQ_CODE_TUMBLR_AUTH);
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
                    Utils.showAlternateAlertDialog(BestizBoxMainListActivity.this, getString(R.string.menu_write),
                            getString(R.string.alert_msg_still_img_upload), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (which == DialogInterface.BUTTON_POSITIVE) {
                                        requestNetwork(FLAG_REQ_WRITE, BASE_URL + Constants.URL_WRITE, write(subject.getText().toString(), totalContents));
                                    }
                                }
                            });

                } else {
                    requestNetwork(FLAG_REQ_WRITE, BASE_URL + Constants.URL_WRITE, write(subject.getText().toString(), totalContents));
                }

                if (imgList.size() > 0) {
                    imgList.clear();
                }

            }
        });
    }

    Uri mUplaodUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.RESULT_REFRESH) { // 어떤 상황이든지 리로딩 시도
            requestNetwork(FLAG_REQ_MAIN_ARTICLE, BASE_SERVER_URL + DETAIL_URL);
        }

        if (data == null)
            return;

        switch (requestCode) {
            case REQ_CODE_DETAIL_ARTICLE:
                isLogin = data.getBooleanExtra(Constants.INTENT_NAME_IS_LOGIN, false);
                break;
            case REQ_CODE_GET_PHOTO:
                if (writeView != null && data != null) {
                    startImgUpload = true;
                    mUplaodUri = data.getData();
                    uploadPictures(Preference.getTumblrToken(BestizBoxMainListActivity.this), Preference.getTumblrSecret(BestizBoxMainListActivity.this));
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
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPictures(String token, String secret) {
        new TumblrImgUpload().execute(token, secret, Utils.getRealPathFromURI(mUplaodUri, BestizBoxMainListActivity.this));
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
                mProgress = new ProgressDialog(BestizBoxMainListActivity.this);
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
            if (mProgress != null) {
                mProgress.dismiss();
            }

            if (result != null && result.getPhotos() != null && result.getPhotos().size() > 0) {
                addImageToContainer(result.getPhotos().get(0).getOriginalSize().getUrl());
                Toast.makeText(BestizBoxMainListActivity.this, getString(R.string.error_msg_success_upload_image), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BestizBoxMainListActivity.this, getString(R.string.error_msg_failed_to_save_link), Toast.LENGTH_SHORT).show();
            }

            mImgUpload = null;
        }

    }

    private void addImageToContainer(final String imgUrl) {
        if (writeView != null) {
            hsvImage.setVisibility(View.VISIBLE);
            startImgUpload = false;

            final ImageView iv = new ImageView(BestizBoxMainListActivity.this);
            iv.setAdjustViewBounds(true);

            int size = (int) getResources().getDimension(R.dimen.img_default_size);

            LayoutParams params = new LayoutParams(size, size);
            iv.setLayoutParams(params);
            iv.setPadding(5, 0, 5, 0);

            if (!TextUtils.isEmpty(imgUrl)) {
                ImageData data = new ImageData(imgUrl, false);
                imgList.add(data);
            }

            Glide.with(BestizBoxMainListActivity.this).load(imgUrl).into(iv);
            containerImg.addView(iv);
        }
    }
}
