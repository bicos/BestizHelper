package com.pockru.bestizhelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.pockru.bestizhelper.data.CommonData;
import com.pockru.bestizhelper.data.Constants;
import com.pockru.preference.Preference;
import com.pockru.utils.Utils;

import java.util.List;
import java.util.Map;

public class BoardSelectActivity extends BaseActivity {

//	private static final String MY_AD_UNIT_ID = "a151cb961585d3e";

	private static final int BTN_TYPE_WEB = 0;
	private static final int BTN_TYPE_MOBILE = 1;

	private AdView adView;

	private RadioGroup rgMode;
	private RadioButton btnWeb;
	private RadioButton btnMobile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_board);
		
		getSupportActionBar().hide();

		adView = new AdView(this);
		adView.setAdUnitId(getString(R.string.ad_unit_id));
		adView.setAdSize(AdSize.BANNER);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.adViewContainer);
		layout.addView(adView);
		
		AdRequest request = new AdRequest.Builder().setGender(AdRequest.GENDER_FEMALE).build();
		adView.loadAd(request);

		rgMode = (RadioGroup) findViewById(R.id.rg_select_mode);
		rgMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.rb_web) {
					Preference.setSelectedMainRadioBtn(getApplicationContext(), BTN_TYPE_WEB);
				} else {
					Preference.setSelectedMainRadioBtn(getApplicationContext(), BTN_TYPE_MOBILE);
				}

			}
		});
		
		btnWeb = (RadioButton) findViewById(R.id.rb_web);
		btnMobile = (RadioButton) findViewById(R.id.rb_mobile);
		
		if (Preference.getSelectedMainRadioBtn(getApplicationContext()) == BTN_TYPE_WEB) {
			btnWeb.setChecked(true);
		} else {
			btnMobile.setChecked(true);
		}

		showNoticeDialog();
		showSelectModeDialog();
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

	public void menuClick(View v) {

		int id = rgMode.getCheckedRadioButtonId();
		Intent intent = null;
		if (id == R.id.rb_mobile) {
			intent = new Intent(this, BestizBoxMainListActivity.class);
		} else {
			intent = new Intent(this, BestizBoxMainActivity.class);
		}

		switch (v.getId()) {
		case R.id.button1:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEACHON_ID));
			break;
		case R.id.button2:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEAJAP_ID));
			break;
		case R.id.button3:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEABOM_ID));
			break;
		case R.id.button4:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEAYEO_ID));
			break;
		case R.id.button5:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEAMOT_ID));
			break;
		case R.id.button6:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEASU_ID));
			break;
		case R.id.button7:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEAJIC_ID));
			break;
		case R.id.button8:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.YEONJAP_ID));
			break;
		case R.id.button9:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.HATBIT_ID));
			break;
		case R.id.button10:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GEABAL_ID));
			break;
		case R.id.button11:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.JICCHIN_ID));
			break;
		case R.id.button12:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.DUBANG_ID));
			break;
		case R.id.button13:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJAK_ID));
			break;
		case R.id.button14:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJKB_ID));
			break;
		case R.id.button15:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJAD_ID));
			break;
		case R.id.button16:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJHI_ID));
			break;
		case R.id.button17:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.MOVIE_ID));
			break;
		case R.id.button18:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.MUSIC_ID));
			break;
		case R.id.button19:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GIMYO_ID));
			break;
		case R.id.button20:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.USER_MUSIC_ID));
			break;
		case R.id.button21:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.HEAD));
			break;
		case R.id.button22:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.NABI));
			break;
		case R.id.button23:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GBYD));
			break;
		case R.id.button24:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GBYC));
			break;
		case R.id.button25:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GB_EVENT));
			break;
		case R.id.button26:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GY_HB));
			break;
		case R.id.button27:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GY_YD));
			break;
		case R.id.button28:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.JSTAR));
			break;
		case R.id.button29:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJGAME));
			break;
		case R.id.button30:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.YJ_SHINEE));
			break;
		case R.id.button31:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.YJ_EXO));
			break;
		case R.id.button32:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.YJ_INF));
			break;
		case R.id.button33:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.YJ_MBQ));
			break;
		case R.id.button34:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJ_TEST));
			break;
		case R.id.button35:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.DOLMENGYEE));
			break;
		case R.id.button36:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GI_EVENT));
			break;
		case R.id.button37:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GI_YC));
			break;
		case R.id.button38:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GI_YD));
			break;
		case R.id.button39:
			intent.putExtra(Constants.INTENT_NAME_BOARD_DATA, CommonData.getInstance().getHashData().get(Constants.GJ_WC));
			break;

		default:
			return;
		}

		startActivity(intent);
	}

	private void showNoticeDialog() {
		
		String version = null;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		if (!Utils.isShowNotice(this, version)) {
			new AlertDialog.Builder(this).
			setTitle(R.string.notice_dialog_title)
			.setMessage(R.string.notice_dialog_desc)
			.setPositiveButton("확인", null)
			.show();
		}
		
		Preference.setVersionName(this, version);
	}
	
	private void showSelectModeDialog(){
		if (!Preference.isShowMode(this)) {
			new AlertDialog.Builder(BoardSelectActivity.this)
			.setTitle(R.string.mode_select_desc)
			.setItems(R.array.mode_list, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case 0:
						btnWeb.setChecked(true);
						break;
					case 1:
						btnMobile.setChecked(true);
						break;
					default:
						break;
					}
				}
			})
			.show();
			Preference.setShowMode(this, true);
		}
	}

	@Override
	public void onResponse(int resCode, Map<String, List<String>> headers, String html, int flag) {
		// TODO Auto-generated method stub
		
	}
}
