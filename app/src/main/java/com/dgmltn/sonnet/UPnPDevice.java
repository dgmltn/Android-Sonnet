package com.dgmltn.sonnet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by doug on 3/24/15.
 */
public class UPnPDevice {

	private String mRaw;
	private URL mLocation;
	private String mServer;

	private UPnPDevice() {
	}

	public String getIPAddress() {
		return mLocation.getHost();
	}

	public URL getLocation() {
		return mLocation;
	}

	public String getRaw() {
		return mRaw;
	}

	public String getServer() {
		return mServer;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Parse methods
	////////////////////////////////////////////////////////////////////////////////

	public static UPnPDevice getInstance(String raw) {
		HashMap<String, String> parsed = parseRaw(raw);
		try {
			URL location = new URL(parsed.get("location"));
			String server = parsed.get("server");
			UPnPDevice device = new UPnPDevice();
			device.mRaw = raw;
			device.mLocation = location;
			device.mServer = server;
			return device;
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static HashMap<String, String> parseRaw(String raw) {
		HashMap<String, String> results = new HashMap<>();
		for (String line : raw.split("\r\n")) {
			int colon = line.indexOf(":");
			if (colon != -1) {
				String key = line.substring(0, colon).trim().toLowerCase();
				String value = line.substring(colon + 1).trim();
				results.put(key, value);
			}
		}
		return results;
	}

}
