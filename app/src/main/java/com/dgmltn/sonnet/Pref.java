package com.dgmltn.sonnet;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by dmelton on 9/19/14.
 */
public class Pref {

	private static final String TAG = Pref.class.getSimpleName();

	@Nullable
	public static SonosConfig getSonosConfig(Context context) {
		String key = generateKey(context);
		Log.e(TAG, "getSonosConfig for key = " + key);
		SharedPreferences prefs = getPrefs(context);
		String json = prefs.getString(key, null);
		if (json == null) {
			return null;
		}
		return new Gson().fromJson(json, SonosConfig.class);
	}

	public static void setSonosConfig(Context context, SonosConfig config) {
		String key = generateKey(context);
		getPrefs(context).edit()
			.putString(key, new Gson().toJson(config))
			.commit();
	}

	private static SharedPreferences getPrefs(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/////////////////////////////////////////////////////////////////////////
	// Generating a key for the current configuration instance
	/////////////////////////////////////////////////////////////////////////

	public static String generateKey(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String result = wifiInfo.getSSID()
			+ wifiInfo.getNetworkId();
		return result;
	}

}
