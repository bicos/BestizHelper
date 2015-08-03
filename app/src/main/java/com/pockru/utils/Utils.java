package com.pockru.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.pockru.bestizhelper.R;
import com.pockru.preference.Preference;

public class Utils {

	public static AlertDialog showCompositeDialog(Context context, String title, View view, DialogInterface.OnClickListener listener) {
		AlertDialog dlg;
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle(title).setView(view)
				.setNegativeButton(context.getString(R.string.dialog_cancel), null).setPositiveButton(context.getString(R.string.dialog_confirm), listener);

		dlg = builder.create();
		dlg.show();
		return dlg;
	}

	public static void showBasicAlertDialog(Context context, String title, String msg) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg).setPositiveButton(context.getString(R.string.dialog_confirm), null).show();
	}

	public static void showAlternateAlertDialog(Context context, String title, String msg, DialogInterface.OnClickListener listener) {
		new AlertDialog.Builder(context).setTitle(title).setMessage(msg).setPositiveButton(context.getString(R.string.dialog_confirm), listener)
				.setNegativeButton(context.getString(R.string.dialog_cancel), null).show();
	}

	public static void showCustomListDialog(Context context, String titleName, String[] array, DialogInterface.OnClickListener itemListener) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(titleName);
		builder.setAdapter(getStringArrayAdapter(context, array), itemListener);
		builder.show();
	}

	private static ArrayAdapter<String> getStringArrayAdapter(Context context, String[] array) {
		return new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, array);
	}

	public static String getURLParam(String params, String name) {
		String result = "";

		if (params == null)
			return result;

		String[] paramList = params.split("&");

		if (paramList == null)
			return result;

		try {
			for (int i = 0; i < paramList.length; i++) {
				String[] param = paramList[i].split("=");

				if (param != null && param.length == 2) {
					if (param[0] != null && param[0].equalsIgnoreCase(name)) {
						result = param[1].trim();
					}
				}

			}
			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return result;
		}

	}

	/**
	 * 버전 체크 후 realPath 가져옴
	 * 
	 * @param contentUri
	 * @return
	 */
	public static String getRealPathFromURI(Uri contentUri, Context mContext) {

		String ret = "";

		if (contentUri.getScheme().equals("file")) {
			ret = contentUri.getPath();
		} else {
			int version = android.os.Build.VERSION.SDK_INT;

			if (version < 12) {
				ret = oldGetRealPathFromURI(mContext, contentUri);
			} else {
				ret = newGetRealPathFromURI(mContext, contentUri);
			}
		}
		// Log.e(TAG, "version : " + version);

		return ret;
	}

	/**
	 * uri 로부터 실제 file 주소를 가져온다. 버전 11 이상부터만 사용 가능
	 * 
	 * @param mContext
	 * @param contentUri
	 * @return
	 */
	@TargetApi(11)
	private static String newGetRealPathFromURI(Context mContext, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(mContext, contentUri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	/**
	 * uri 로부터 실제 file 주소를 가져온다. 버전 11 미만 사용가능
	 * 
	 * @param mContext
	 * @param contentUri
	 * @return
	 */
	private static String oldGetRealPathFromURI(Context mContext, Uri contentUri) {
		String[] proj = { MediaColumns.DATA };
		Cursor cursor = ((Activity) mContext).managedQuery(contentUri, proj, null, null, null);
		if (cursor == null)
			return null;
		int column_index = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	public static String getStringCurrentTime() {
		return String.valueOf(Calendar.getInstance().getTimeInMillis());
	}

	public static View getView(Activity activity, int vId) {
		LayoutInflater inflater = activity.getLayoutInflater();
		return inflater.inflate(vId, null);
	}

	public static String getText(InputStream in) {
		String text = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			text = sb.toString();
		} catch (Exception ex) {

		} finally {
			try {

				in.close();
			} catch (Exception ex) {
			}
		}
		return text;
	}

	public static String getText(HttpResponse response) {
		String text = "";
		try {
			text = EntityUtils.toString(response.getEntity());
		} catch (Exception ex) {
		}
		return text;
	}

	/**
	 * 
	 * 
	 * @param versionCode
	 * @return
	 */
	public static int isOverCurrentAndroidVersion(int versionCode) {
		if (android.os.Build.VERSION.SDK_INT > versionCode) {
			return 1;
		} else if (android.os.Build.VERSION.SDK_INT == versionCode) {
			return 0;
		} else {
			return -1;
		}
	}

	public static boolean isShowNotice(Context context, String version) {
		String prevVersion = Preference.getVersionName(context);
		if (TextUtils.isEmpty(prevVersion) == false && prevVersion.equals(version)) {
			return true;
		} else {
			return false;
		}
	}

	public static Bitmap createOutline(Bitmap src) {
		Paint p = new Paint();
		p.setMaskFilter(new BlurMaskFilter(10, Blur.OUTER));
		return src.extractAlpha(p, null);
	}
}
