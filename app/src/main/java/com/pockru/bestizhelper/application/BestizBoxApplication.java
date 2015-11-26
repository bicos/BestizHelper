package com.pockru.bestizhelper.application;

import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.firebase.client.Firebase;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.pockru.bestizhelper.R;
import com.pockru.network.WebkitCookieManagerProxy;

import java.util.HashMap;

public class BestizBoxApplication extends MultiDexApplication {

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     * <p/>
     * A single tracker is usually enough for most purposes. In case you do need
     * multiple trackers, storing them all in Application object helps ensure
     * that they are created only once per application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company.
        // eg:roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
        // company.
    }

    //	private static AsyncHttpClient client;
    private static HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    @Override
    public void onCreate() {
        super.onCreate();

		/* 쿠키 동기화를 위해 실행하는 코드 */
        CookieSyncManager.createInstance(this);
        WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, java.net.CookiePolicy.ACCEPT_ALL);
        java.net.CookieHandler.setDefault(coreCookieManager);

        /* 파이어버드 셋팅 */
        Firebase.setAndroidContext(this);

        /* 웹뷰 디버그 셋팅 */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(getString(R.string.ga_property_id)) : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics
                    .newTracker(R.xml.global_tracker) : analytics.newTracker(R.xml.ecommerce_tracker);
            t.enableAdvertisingIdCollection(true);
            t.enableExceptionReporting(true);
            t.enableAutoActivityTracking(true);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }
}
