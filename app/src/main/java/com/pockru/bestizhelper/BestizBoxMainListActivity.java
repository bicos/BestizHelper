package com.pockru.bestizhelper;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.SupportMenuInflater;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.pockru.bestizhelper.adapter.ArticleListAdapter;
import com.pockru.bestizhelper.data.ArticleData;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.ChatData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.bestizhelper.data.UserData;
import com.pockru.bestizhelper.database.helper.MemberDatabaseHelper;
import com.pockru.bestizhelper.dialog.WriteDialog;
import com.pockru.bestizhelper.view.ChatView;
import com.pockru.firebase.UrlConstants;
import com.pockru.network.BestizParamsUtil;
import com.pockru.network.BestizUrlUtil;
import com.pockru.utils.UiUtils;
import com.pockru.utils.Utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BestizBoxMainListActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    public static final int REQ_CODE_DETAIL_ARTICLE = 103;

    int pageNum = 1;

    private String sn = "";
    private String ss = "";
    private String sc = "";

    private String keyword;

    private boolean isLogin = false;

    private UserData userData;

    private SwipeRefreshLayout mSwipeMain;
    private ListView mListMain;
    private ArticleListAdapter mAdapter;

    private AdView adView;

    private Button btnWrite;
    private int maxTansY, transY;

    //  로그인 후 로직 제어
    private boolean isShowWriteDialog = false;

    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private ChatView chatView;

    // 채팅 관련
    private Firebase mRef;
    private ValueEventListener mConnectedListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        mBoardData = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);
        if (mBoardData != null) {
            BASE_SERVER_URL = mBoardData.baseUrl;
            BOARD_ID = mBoardData.id;
        }

        getSupportActionBar().setTitle(mBoardData == null ? "" : mBoardData.name);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.ad_unit_id));
        adView.setAdSize(AdSize.BANNER);

        LinearLayout layout = (LinearLayout) findViewById(R.id.adViewContainer);
        layout.addView(adView);

        AdRequest request = new AdRequest.Builder().setGender(AdRequest.GENDER_FEMALE).build();
        adView.loadAd(request);

        mSwipeMain = (SwipeRefreshLayout) findViewById(R.id.swipe_main);
        mListMain = (ListView) findViewById(R.id.lv_main);
        mAdapter = new ArticleListAdapter(this);
        mListMain.setAdapter(mAdapter);
        mListMain.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ArticleData data = mAdapter.getItem(arg2);
                if (data != null) {
                    Intent intent = new Intent(BestizBoxMainListActivity.this, BestizBoxDetailActivity.class);
//                    intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, mBoardData);
                    intent.putExtra(Constants.INTENT_NAME_DETAIL_ARTICLE_URL, BestizUrlUtil.createDetailArticleUrl(BASE_SERVER_URL, data.getAtcLink()));
//                    intent.putExtra(Constants.INTENT_NAME_BOARD_ID, BOARD_ID);
//                    intent.putExtra(Constants.INTENT_NAME_IS_LOGIN, isLogin);
//                    intent.putExtra(Constants.INTENT_NAME_BASE_SERVER_URL, BASE_SERVER_URL);
//                    intent.putExtra(Constants.INTENT_NAME_ARTICLE_DATA, data);
                    startActivityForResult(intent, REQ_CODE_DETAIL_ARTICLE);
                }

            }
        });
        mListMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ArticleData data = mAdapter.getItem(position);

                if (data != null) {
                    saveUrl(BestizUrlUtil.createDetailArticleUrl(BASE_SERVER_URL, data.getAtcLink()));
                }

                return true;
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
                    requestNetwork(FLAG_REQ_NEXT_ARTICLE,
                            BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID),
                            BestizParamsUtil.createMovePageParams(BOARD_ID, String.valueOf(pageNum), keyword, sn, ss, sc));
                }
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
        mSwipeMain.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                pageNum = 1;
                requestNetwork(FLAG_REQ_MAIN_ARTICLE, BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
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

        //채팅 뷰 셋팅
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer_chat, R.string.close_drawer_chat) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (userData == null) {
                    Utils.showAlternateAlertDialog(BestizBoxMainListActivity.this,
                            "알림",
                            "로그인을 하셔야 채팅을 이용할 수 있습니다. 로그인을 하시겠습니까?",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showLoginDialog();
                                }
                            });
                    drawerLayout.closeDrawer(chatView);
                } else {
                    mConnectedListener = mRef.getRoot().child(UrlConstants.FIREBASE_CONNECTED).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean connected = (Boolean) dataSnapshot.getValue();
                            if (connected) {
                                Toast.makeText(BestizBoxMainListActivity.this, "채팅방에 접속하였습니다.", Toast.LENGTH_SHORT).show();
                                chatView.initChatView(mRef.limitToLast(10), userData.id);
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Toast.makeText(BestizBoxMainListActivity.this, "채팅방에 접속을 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                if (mConnectedListener != null) {
                    mRef.getRoot().child(UrlConstants.FIREBASE_CONNECTED).removeEventListener(mConnectedListener);
                    chatView.cleanUp();

                    Toast.makeText(BestizBoxMainListActivity.this, "채팅방 접속을 해제하였습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        drawerLayout.setDrawerListener(toggle);


        chatView = (ChatView) findViewById(R.id.chat_drawer_view);
        chatView.getInputChat().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    sendChatMsg();
                }
                return false;
            }
        });
        chatView.getBtnSendMsg().setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendChatMsg();
            }
        });

        mRef = new Firebase(UrlConstants.FIREBASE_URL).child(UrlConstants.CHAT);

        pb = (ProgressBar) findViewById(R.id.progressBar1);

        userData = MemberDatabaseHelper.getData(getApplicationContext(), BASE_SERVER_URL);
        if (userData != null) {
            requestNetwork(FLAG_REQ_LOGIN,
                    BestizUrlUtil.createLoginUrl(BASE_SERVER_URL),
                    BestizParamsUtil.createLoginParams(BOARD_ID, userData.id, userData.pwd));
        } else {
            requestNetwork(FLAG_REQ_MAIN_ARTICLE, BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
        }
    }

    private void sendChatMsg() {
        if (chatView != null && chatView.getAdapter() != null && chatView.getInputChat() != null) {
            String msg = chatView.getInputChat().getText().toString();
            if (TextUtils.isEmpty(msg) == false) {
                mRef.push().setValue(new ChatData(userData.id, msg, userData.name), new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError == null) { // 채팅작성 성공
                            chatView.getInputChat().setText("");
                        } else { // 채팅작성 실패
                            Toast.makeText(getApplicationContext(), "채팅 작성을 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(getApplicationContext(), "채팅 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
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

        if (resCode != 200 || html == null) {
            Toast.makeText(getApplicationContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            switch (flag) {
                case FLAG_REQ_NEXT_ARTICLE:
                    pageNum--;
                    break;
            }
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
                requestNetwork(FLAG_REQ_MAIN_ARTICLE, BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
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
                // db에서 해당 데이터 제거
                MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);

                Toast.makeText(this, getString(R.string.msg_logout_success), Toast.LENGTH_SHORT).show();
                requestNetwork(FLAG_REQ_MAIN_ARTICLE, BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
                break;
            case FLAG_REQ_SEARCH:
                setMainArticleList(html);
                break;
            case FLAG_REQ_MEM_INFO:
                setMemberInfo(html);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(chatView)) {
            drawerLayout.closeDrawer(chatView);
        } else {
            super.onBackPressed();
        }
    }

    private void nextLoginStep(String arg2) {
        Document doc = Jsoup.parse(arg2);
        String httpEquiv = doc.getElementsByAttribute("http-equiv").attr("http-equiv");
        String content = doc.getElementsByAttribute("content").attr("content");

        if ((httpEquiv != null && content != null) && (httpEquiv.equalsIgnoreCase("refresh"))) { // 로그인 성공
            isLogin = true;

            // login값 셋팅
            UserData data = MemberDatabaseHelper.getData(getApplicationContext(), BASE_SERVER_URL);
            boolean isFirstLogin;

            if (data == null) {
                isFirstLogin = true;
                data = new UserData(userData.id, userData.pwd, BASE_SERVER_URL);
            } else {
                isFirstLogin = false;
                data.id = userData.id;
                data.pwd = userData.pwd;
                data.server = BASE_SERVER_URL;
            }

            MemberDatabaseHelper.insertOrUpdate(getApplicationContext(), data);

            Toast.makeText(this, getString(R.string.msg_login_success), Toast.LENGTH_SHORT).show();

            if (isShowWriteDialog) { //  로그인을 성공한 경우에만 다음 작업 진행
                showWriteDialog();
            }

            if (isFirstLogin) { // 처음 로그인 시 멤버 정보 가져오는 로직 추가
                reqMemberInfo();
            }
        } else {
            isLogin = false;
            MemberDatabaseHelper.delete(getApplicationContext(), BASE_SERVER_URL);
        }

        // 메뉴 리프레시
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                supportInvalidateOptionsMenu();
            }
        });

        requestNetwork(FLAG_REQ_MAIN_ARTICLE, BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));

        if (isShowWriteDialog) {
            isShowWriteDialog = false;
        }
    }

    private void setMemberInfo(String html) {
        UserData data = MemberDatabaseHelper.getData(getApplicationContext(), BASE_SERVER_URL);

        if (data == null) {
            return;
        }

        Document doc = Jsoup.parse(html);

        String level = doc.select("body > div > table > tbody > tr:nth-child(7) > td:nth-child(2)").text();
        String nickName = doc.select("body > div > table > tbody > tr:nth-child(9) > td:nth-child(2) > input").val();
        String email = doc.select("body > div > table > tbody > tr:nth-child(11) > td:nth-child(2) > input.input").val();
        String homepage = doc.select("body > div > table > tbody > tr:nth-child(13) > td:nth-child(2) > input.input").val();
        String disclosedInfo = doc.select("body > div > table > tbody > tr:nth-child(15) > td:nth-child(2) > input[type=\"checkbox\"]").val();
        String comment = doc.select("body > div > table > tbody > tr:nth-child(17) > td:nth-child(2) > textarea").text();
        String point = doc.select("body > div > table > tbody > tr:nth-child(19) > td:nth-child(2)").text();
        String isShowComment = doc.select("body > div > table > tbody > tr:nth-child(17) > td:nth-child(2) > input[type=\"checkbox\"]").val();

        try {
            if (level != null) {
                data.level = Utils.getDigit(level);
            }
            if (nickName != null) {
                data.name = nickName;
            }
            if (email != null) {
                data.email = email;
            }
            if (homepage != null) {
                data.homepage = homepage;
            }
            if (disclosedInfo != null) {
                data.discloseInfo = Utils.parseBoolean(disclosedInfo.trim());
            }
            if (point != null) {
                data.point = point;
            }
            if (comment != null) {
                data.comment = comment;
            }
            if (isShowComment != null) {
                data.isShowComment = Utils.parseBoolean(isShowComment.trim());
            }

            this.userData = data;
            MemberDatabaseHelper.update(getApplicationContext(), data);
        } catch (Exception e) {
            e.printStackTrace();
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
                requestNetwork(FLAG_REQ_NEXT_ARTICLE,
                        BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID),
                        BestizParamsUtil.createMovePageParams(BOARD_ID, String.valueOf(pageNum), keyword, sn, ss, sc));
            } else {
                mAdapter.setDataList(totalDataList);
            }
            // mAdapter.addAllData(dataList);
        }
    }

    /**
     * http://bestjd.bestiz.net/zboard/member_modify.php?group_no=1
     *
     * @return
     */
    private void reqMemberInfo() {
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("group_no", "1"));

        requestNetwork(FLAG_REQ_MEM_INFO,
                BestizUrlUtil.createUserInfoUrl(BASE_SERVER_URL),
                params);
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

                        if (userData == null) {
                            userData = new UserData(id.getText().toString(), pwd.getText().toString(), BASE_SERVER_URL);
                        } else {
                            userData.id = id.getText().toString();
                            userData.pwd = pwd.getText().toString();
                        }

                        requestNetwork(FLAG_REQ_LOGIN,
                                BestizUrlUtil.createLoginUrl(BASE_SERVER_URL),
                                BestizParamsUtil.createLoginParams(BOARD_ID, id.getText().toString(), pwd.getText().toString()));
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

    private WriteDialog writeDialog;

    private void showWriteDialog() {
        if (writeDialog == null) {
            writeDialog = new WriteDialog(this, BASE_SERVER_URL, BOARD_ID);
        }
        writeDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.RESULT_REFRESH) { // 어떤 상황이든지 리로딩 시도
            requestNetwork(FLAG_REQ_MAIN_ARTICLE, BestizUrlUtil.createBoardListUrl(BASE_SERVER_URL, BOARD_ID));
        }

        if (data == null)
            return;

        switch (requestCode) {
            case REQ_CODE_DETAIL_ARTICLE:
                isLogin = data.getBooleanExtra(Constants.INTENT_NAME_IS_LOGIN, false);
                break;
            default:
                break;
        }

        if (writeDialog != null) {
            writeDialog.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void saveUrl(String extra) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ClipboardManager mgr = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData date = ClipData.newPlainText(extra, extra);
            mgr.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {

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
}
