package com.pockru.bestizhelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pockru.bestizhelper.adapter.ArticleListAdapter;
import com.pockru.bestizhelper.data.ArticleData;
import com.pockru.bestizhelper.data.BoardData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.utils.Utils;

public class BestizBoxSearchActivity extends BaseActivity implements OnItemClickListener {

	private BoardData mBoardData;
	
	private String BASE_SERVER_URL;
	private String BASE_URL;
	private String DETAIL_URL;
	private String BOARD_ID;

	private String no = "";
	private int pageNum = 0;
//	private boolean isSelectName = false, isSelectSubject = true, isSelectContents = false;
	private String selectOrder = Constants.SEARCH_ORDER_HEADNUM;
	private String selectOrderDesc = Constants.SEARCH_ORDER_ASC;

	private EditText etSearch;
	private ListView lvSearch;
	private ArticleListAdapter mAdapter;
	private LinearLayout containerIndicator;
	
	private CheckBox cbOptName , cbOptTitle , cbOptContents;
	private RadioGroup rgOrder01 , rgOrder02;
	
	private boolean prevCbOptName , prevCbOptTitle , prevOptContents;
	private int prevRgOrder01Id , prevRgOrder02Id;

	private AlertDialog optionDlg , orderDlg;

	private boolean isLogin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mBoardData = (BoardData) getIntent().getSerializableExtra(Constants.INTENT_NAME_BOARD_DATA);
		if (mBoardData != null) {
			BASE_SERVER_URL = mBoardData.baseUrl;
			DETAIL_URL = mBoardData.id;
			BASE_URL = BASE_SERVER_URL.replace("/zboard.php", "");
			BOARD_ID = DETAIL_URL.replace("?id=", "");			
		}
		
		getSupportActionBar().setTitle(R.string.label_search_activity);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		
		isLogin = getIntent().getBooleanExtra(Constants.INTENT_NAME_IS_LOGIN, false);

		pb = (ProgressBar) findViewById(R.id.pbSearch);
		etSearch = (EditText) findViewById(R.id.etSearch);
		containerIndicator = (LinearLayout) findViewById(R.id.containerIndex);
		lvSearch = (ListView) findViewById(R.id.lvSearch);
		lvSearch.setOnItemClickListener(this);
		mAdapter = new ArticleListAdapter(this);
		lvSearch.setAdapter(mAdapter);

		View optionView = LayoutInflater.from(this).inflate(R.layout.layout_search_option, null);
		cbOptName = (CheckBox) optionView.findViewById(R.id.cbName);
		cbOptTitle = (CheckBox) optionView.findViewById(R.id.cbSubject);
		cbOptContents = (CheckBox) optionView.findViewById(R.id.cbContents);
		optionDlg = new AlertDialog.Builder(this).setTitle(R.string.search_title_option)
				.setView(optionView)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setOptionBtn();
					}
				}).create();
		optionDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				cbOptName.setChecked(prevCbOptName);
				cbOptTitle.setChecked(prevCbOptTitle);
				cbOptContents.setChecked(prevOptContents);
			}
		});
		optionDlg.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				prevCbOptName = cbOptName.isChecked();
				prevCbOptTitle = cbOptTitle.isChecked();
				prevOptContents = cbOptContents.isChecked();
			}
		});
		
		View orderView = LayoutInflater.from(this).inflate(R.layout.layout_search_order, null);
		rgOrder01 = (RadioGroup) orderView.findViewById(R.id.rgOrder01);
		rgOrder02 = (RadioGroup) orderView.findViewById(R.id.rgOrder02);
		orderDlg = new AlertDialog.Builder(this).setTitle(R.string.search_title_order).setView(orderView)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setOrderBtn();
					}
				}).create();
		orderDlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				RadioButton btn1 = (RadioButton) rgOrder01.findViewById(prevRgOrder01Id);
				btn1.setChecked(true);
				RadioButton btn2 = (RadioButton) rgOrder02.findViewById(prevRgOrder02Id);
				btn2.setChecked(true);
			}
		});
		orderDlg.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				prevRgOrder01Id = rgOrder01.getCheckedRadioButtonId();
				prevRgOrder02Id = rgOrder02.getCheckedRadioButtonId();
			}
		});
	}

	public void btnClick(View v) {
		switch (v.getId()) {
		case R.id.btnSearch:
			requestNetwork(0, BASE_URL + "/zboard.php", search(etSearch.getText().toString()));
			break;
		case R.id.btnSelectBoard:
			new AlertDialog.Builder(this).setMessage("준비중입니다.").setPositiveButton("확인", null).show();
			break;
		case R.id.btnSelelctOption:
			if (optionDlg != null) {
				optionDlg.show();
			}
			break;
		case R.id.btnSelelctOrder:
			if (orderDlg != null) {
				orderDlg.show();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
		if (resCode != 200) {
			Toast.makeText(getApplicationContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}

		if (html == null) {
			Toast.makeText(getApplicationContext(), "네트워크가 불안정합니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		setMainArticleList(html);
	}

	private ArrayList<NameValuePair> search(String keyword) {

		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("page", "1"));
		params.add(new BasicNameValuePair("id", BOARD_ID));
		params.add(new BasicNameValuePair("no", no));
		params.add(new BasicNameValuePair("select_arrange", getOrderArrangeTxt()));
		params.add(new BasicNameValuePair("desc", getOrderTxt()));
		params.add(new BasicNameValuePair("sn", cbOptName.isChecked() ? "on" : "off"));
		params.add(new BasicNameValuePair("ss", cbOptTitle.isChecked() ? "on" : "off"));
		params.add(new BasicNameValuePair("sc", cbOptContents.isChecked() ? "on" : "off"));
		params.add(new BasicNameValuePair("keyword", keyword));
		return params;
	}

	private void setMainArticleList(String arg) {
		if (containerIndicator.getChildCount() != 0) {
			containerIndicator.removeAllViews();
		}
		
		String params = privUrl.substring(privUrl.indexOf("?") + 1, privUrl.length());

		if (!Utils.getURLParam(params, "page").equals(""))
			pageNum = Integer.parseInt(Utils.getURLParam(params, "page"));

		ArticleData data;
		ArrayList<ArticleData> dataList = new ArrayList<ArticleData>();
		Document doc = Jsoup.parse(arg);
		Elements elements = doc.getElementsByAttributeValueContaining("onMouseOver", "this.style.backgroundColor='#F9F9F9'");
		Element element;

		int size = elements.size();

		for (int i = 0; i < size; i++) {
			data = new ArticleData();
			element = elements.get(i);
			data.setAtcNum(element.getElementsByAttributeValueContaining("class", "listnum").get(0).text());
			data.setAtcDate(element.getElementsByAttributeValueContaining("class", "listnum").get(1).text());
			data.setAtcHit(element.getElementsByAttributeValueContaining("class", "listnum").get(2).text());
			data.setAtcVote("+" + element.getElementsByAttributeValueContaining("class", "listnum").get(3).text());

			data.setAtcLink(element.getElementsByAttributeValueContaining("style", "word-break:break-all;").get(0).getElementsByAttribute("href").attr("href"));
			data.setAtcTitle(element.getElementsByAttributeValueContaining("style", "word-break:break-all;").text());

			data.setAtcUser(element.getElementsByAttribute("nowrap").text());
			dataList.add(data);
		}

		mAdapter.setDataList(dataList);

		// elements = doc.getElementsByAttributeValue("class",
		// "listnum").select("a[href]");
		elements = doc.getElementsByAttributeValue("colspan", "2").select("a[href],b");
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).toString().contains("Zeroboard")) {
				continue;
			}
			if (elements.get(i).hasAttr("href")) {
				final String url = BASE_SERVER_URL.replace("/zboard/zboard.php", "") + elements.get(i).attr("href").toString();
				String txt = elements.get(i).text();

				TextView idx = new TextView(this);
				idx.setText(txt);
				idx.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						requestNetwork(0, encodeGetUrl(url));
					}
				});
				idx.setPadding((int) getResources().getDimension(R.dimen.search_idx_padding), (int) getResources().getDimension(R.dimen.search_idx_padding),
						(int) getResources().getDimension(R.dimen.search_idx_padding), (int) getResources().getDimension(R.dimen.search_idx_padding));
				idx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				containerIndicator.addView(idx);
			} else {
				String txt = elements.get(i).text();

				TextView idx = new TextView(this);
				idx.setText(txt);
				idx.setPadding((int) getResources().getDimension(R.dimen.search_idx_padding), (int) getResources().getDimension(R.dimen.search_idx_padding),
						(int) getResources().getDimension(R.dimen.search_idx_padding), (int) getResources().getDimension(R.dimen.search_idx_padding));
				idx.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
				idx.setTypeface(idx.getTypeface(), Typeface.BOLD);
				containerIndicator.addView(idx);
			}
		}

	}
	
	private void setOptionBtn(){
		String btnTxt = cbOptName.isChecked() ? "이름" : "";
		if (btnTxt.equals("")) {
			btnTxt = btnTxt + (cbOptTitle.isChecked() ? "제목":"");
		}else{
			btnTxt = btnTxt + (cbOptTitle.isChecked() ? ",제목" : "");
		}
		if (btnTxt.equals("")) {
			btnTxt = btnTxt + (cbOptContents.isChecked() ? "내용":"");
		}else{
			btnTxt = btnTxt + (cbOptContents.isChecked() ? ",내용":"");
		}
		
		((Button)findViewById(R.id.btnSelelctOption)).setText(btnTxt);
	}
	
	private void setOrderBtn(){
		RadioButton btn1 = (RadioButton) rgOrder01.findViewById(rgOrder01.getCheckedRadioButtonId());
		RadioButton btn2 = (RadioButton) rgOrder02.findViewById(rgOrder02.getCheckedRadioButtonId());
		
		String btnTxt = btn1.getText() + "," +btn2.getText();
		((Button)findViewById(R.id.btnSelelctOrder)).setText(btnTxt);
	}
	
	private String getOrderArrangeTxt(){
		if (rgOrder01.getCheckedRadioButtonId() == R.id.rbHeadnum) {
			return Constants.SEARCH_ORDER_HEADNUM;
		} else if(rgOrder01.getCheckedRadioButtonId() == R.id.rbHit){
			return Constants.SEARCH_ORDER_HIT;
		} else if (rgOrder01.getCheckedRadioButtonId() == R.id.rbVote) {
			return Constants.SEARCH_ORDER_VOTE;
		}else {
			return "";
		}
	}
	
	private String getOrderTxt(){
		return rgOrder02.getCheckedRadioButtonId() == R.id.rbAsc ? Constants.SEARCH_ORDER_ASC:Constants.SEARCH_ORDER_DESC;
	}
	
	private String encodeGetUrl(String url){
		String retUrl = url.substring(0, url.indexOf("?"));
		String params[] = url.substring(url.indexOf("?")).split("&");
		for (int i = 0; i < params.length; i++) {
			String values[] = params[i].split("=");
			if (values != null && values.length == 2) {
				try {
					if (values[0].equals("keyword")) {
						values[1] = URLEncoder.encode(etSearch.getText().toString(), "euc-kr");
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				retUrl = retUrl + (values[0] +"=" +values[1] + "&");
			}
		}
		return retUrl.substring(0, retUrl.length() - 1);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(BestizBoxSearchActivity.this, BestizBoxDetailActivity.class);
		intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, mBoardData);
		String detailUrl = BASE_URL + "/" + ((String) view.findViewById(R.id.txt_main_atc_title).getTag());
		detailUrl = detailUrl.replace("keyword=","");
		intent.putExtra(Constants.INTENT_NAME_DETAIL_ARTICLE_URL, detailUrl);
		intent.putExtra(Constants.INTENT_NAME_BASE_URL, BASE_URL);
		intent.putExtra(Constants.INTENT_NAME_BOARD_ID, BOARD_ID);
		intent.putExtra(Constants.INTENT_NAME_IS_LOGIN, isLogin);
		intent.putExtra(Constants.INTENT_NAME_BASE_SERVER_URL, BASE_SERVER_URL);
		intent.putExtra(Constants.INTENT_NAME_ARTICLE_DATA, (ArticleData)mAdapter.getItem(position));
		startActivityForResult(intent, BestizBoxMainListActivity.REQ_CODE_DETAIL_ARTICLE);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
}
