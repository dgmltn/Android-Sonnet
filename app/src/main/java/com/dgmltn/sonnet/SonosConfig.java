package com.dgmltn.sonnet;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by doug on 3/29/15.
 */
public class SonosConfig {

	private static final String TAG = SonosConfig.class.getSimpleName();

	/**
	 * The device to use in this configuration instance.
	 */
	private SonosDevice mDevice;

	/**
	 * The playlist to use in this configuration instance.
	 */
	private SonosPlaylist mPlaylist;

	public SonosConfig(SonosDevice device, SonosPlaylist playlist) {
		mDevice = device;
		mPlaylist = playlist;
	}

	public SonosDevice getDevice() {
		return mDevice;
	}

	public void setDevice(SonosDevice device) {
		mDevice = device;
	};

	public SonosPlaylist getPlaylist() {
		return mPlaylist;
	}

	public void setPlaylist(SonosPlaylist playlist) {
		mPlaylist = playlist;
	}

	/////////////////////////////////////////////////////////////////////////
	// Temporary config for Doug's House
	/////////////////////////////////////////////////////////////////////////

	public static final SonosDevice ZONE_TV = SonosDevice.newInstance(
		"http://10.5.23.196:1400/xml/device_description.xml",
		"TV",
		"RINCON_000E58B7172801400");

	public static final SonosDevice ZONE_BEDROOM = SonosDevice.newInstance(
		"http://10.5.23.114:1400/xml/device_description.xml",
		"Bedroom",
		"RINCON_B8E9373B65D801400");

	public static final SonosDevice ZONE_BATHROOM = SonosDevice.newInstance(
		"http://10.5.23.156:1400/xml/device_description.xml",
		"Bathroom",
		"RINCON_B8E93753707001400");

	public static final SonosDevice ZONE_LIVING_ROOM = SonosDevice.newInstance(
		"http://10.5.23.114:1400/xml/device_description.xml",
		"Living Room",
		"RINCON_B8E9378E5CD601400");

	public static final SonosDevice ZONE_KITCHEN = SonosDevice.newInstance(
		"http://10.5.23.204:1400/xml/device_description.xml",
		"Kitchen",
		"RINCON_000E58C1D23001400");

	public static final SonosDevice ZONE_OFFICE = SonosDevice.newInstance(
		"http://10.5.23.191:1400/xml/device_description.xml",
		"Office",
		"RINCON_B8E93783163601400");

	public static final SonosPlaylist PLAYLIST_GIRLS = new SonosPlaylist(
		"SQ:10",
		"Girls",
		"file:///jffs/settings/savedqueues.rsq#6"
	);

	public static final SonosPlaylist PLAYLIST_HIP_HOP = new SonosPlaylist(
		"SQ:6",
		"Hip Hop",
		"file:///jffs/settings/savedqueues.rsq#10"
	);

	public static SonosConfig getTempConfig(Context context) {
		return new SonosConfig(ZONE_OFFICE, PLAYLIST_GIRLS);
	}

}
