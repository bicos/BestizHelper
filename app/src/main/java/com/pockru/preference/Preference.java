package com.pockru.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preference {

	private static final String PREF_NAME = "bestiz";

	private static final String PARAM_POST_NUM = "post_num";

	private static final String PARAM_USER_ID = "user_id";
	private static final String PARAM_USER_PWD = "user_pwd";
	private static final String PARAM_IMGUR_TOKEN = "imgur_token";
	private static final String PARAM_IMGUR_SECRET = "imgur_secret";
	
	private static final String PARAM_TUMBLR_TOKEN = "tumblr_token";
	private static final String PARAM_TUMBLR_SECRET = "tumblr_secret";
	
	private static final String PARAM_SERVER1_ID = "server1_id";
	private static final String PARAM_SERVER1_PWD = "server1_fwd";

	private static final String PARAM_SERVER2_ID = "server2_id";
	private static final String PARAM_SERVER2_PWD = "server2_fwd";

	private static final String PARAM_SERVER3_ID = "server3_id";
	private static final String PARAM_SERVER3_PWD = "server3_fwd";
	
	private static final String PARAM_SERVER4_ID = "server4_id";
	private static final String PARAM_SERVER4_PWD = "server4_fwd";
	
	private static final String PARAM_SERVER5_ID = "server5_id";
	private static final String PARAM_SERVER5_PWD = "server5_fwd";
	
	private static final String PARAM_AUTO_LOGIN = "auto_login";
	
	private static final String PARAM_VERSION_NAME = "version_name";
	
	private static final String PARAM_SELECTED_MAIN_BTN = "main_btn";
	
	private static final String PARAM_IS_SHOW_MODE = "is_show_mode";

	private static String getString(Context context, String key) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return pref.getString(key, "");
	}

	private static void setString(Context context, String key, String value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	private static int getInt(Context context, String key) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return pref.getInt(key, 0);
	}

	private static void setInt(Context context, String key, int value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	private static boolean getBooean(Context context, String key) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return pref.getBoolean(key, false);
	}

	private static void setBoolean(Context context, String key, boolean value) {
		SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

//	public static void setUserInfo(Context context, UserData data) {
//		setString(context, PARAM_USER_ID, data.id);
//		setString(context, PARAM_USER_PWD, data.pwd);
//	}
//
//	public static UserData getUserInfo(Context context) {
//		String id = getString(context, PARAM_USER_ID);
//		String pwd = getString(context, PARAM_USER_PWD);
//
//		if (!id.equals("") && !pwd.equals("")) {
//			return new UserData(id, pwd);
//		} else {
//			return null;
//		}
//	}

	public static void setImgurToken(Context context, String value) {
		setString(context, PARAM_IMGUR_TOKEN, value);
	}

	public static String getImgurToken(Context context) {
		return getString(context, PARAM_IMGUR_TOKEN);
	}

	public static void setImgurSecret(Context context, String value) {
		setString(context, PARAM_IMGUR_SECRET, value);
	}

	public static String getImgurSecret(Context context) {
		return getString(context, PARAM_IMGUR_SECRET);
	}
	
	public static void setVersionName(Context context , String value){
		setString(context, PARAM_VERSION_NAME, value);
	}
	
	public static String getVersionName(Context context){
		return getString(context, PARAM_VERSION_NAME);
	}
	
	public static void setSelectedMainRadioBtn(Context context , int value){
		setInt(context, PARAM_SELECTED_MAIN_BTN, value);
	}
	
	public static int getSelectedMainRadioBtn(Context context){
		return getInt(context, PARAM_SELECTED_MAIN_BTN);
	}
	
	public static boolean isShowMode(Context context){
		return getBooean(context, PARAM_IS_SHOW_MODE);
	}
	
	public static void setShowMode(Context context , boolean value){
		setBoolean(context, PARAM_IS_SHOW_MODE, value);
	}
	
	/** tumblr token */
	
	public static void setTumblrToken(Context context, String value) {
		setString(context, PARAM_TUMBLR_TOKEN, value);
	}

	public static String getTumblrToken(Context context) {
		return getString(context, PARAM_TUMBLR_TOKEN);
	}

	public static void setTumblrSecret(Context context, String value) {
		setString(context, PARAM_TUMBLR_SECRET, value);
	}

	public static String getTumblrSecret(Context context) {
		return getString(context, PARAM_TUMBLR_SECRET);
	}
}
