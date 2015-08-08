package com.pockru.bestizhelper.application;

import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.webkit.CookieSyncManager;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.pockru.bestizhelper.R;
import com.pockru.network.WebkitCookieManagerProxy;

public class BestizBoxApplication extends Application {

	/**
	 * Enum used to identify the tracker that needs to be used for tracking.
	 * 
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
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		
		/* 쿠키 동기화를 위해 실행하는 코드 */
		CookieSyncManager.createInstance(this);
		WebkitCookieManagerProxy coreCookieManager = new WebkitCookieManagerProxy(null, java.net.CookiePolicy.ACCEPT_ALL);
		java.net.CookieHandler.setDefault(coreCookieManager);
	}
	
	public static Context getAppContext(){ return context; }

//	public static AsyncHttpClient getClientInstance() {
//		if (client == null) {
//			client = new AsyncHttpClient();
//			client.setUserAgent("Mozilla/5.0 (Linux; U; Android 2.2.1; en-us; Nexus One Build/FRG83) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
//		}
//		return client;
//	}

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
