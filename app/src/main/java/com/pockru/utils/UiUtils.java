package com.pockru.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;

public class UiUtils {
    public static float getDpi(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static int getPixelFromDp(Context context, float dp) {
        return (int) (getDpi(context) * dp);
    }

    public static int getDisplayWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getDisplayPotraitWidth(Context context) {
        if (context instanceof Activity
                && ((Activity) context).getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return getDisplayHeight(context);
        }

        return getDisplayWidth(context);
    }

}
