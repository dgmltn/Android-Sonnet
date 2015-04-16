package com.dgmltn.sonnet;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ListView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A ListView that automatically populates itself with a list of SonosDevice devices.
 * Created by doug on 3/26/15.
 */
public class SonosPlaylistsListView extends ListView {

	private static final String TAG = SonosPlaylistsListView.class.getSimpleName();

	private SonosDevice mDevice = SonosConfig.ZONE_OFFICE;
	private SonosPlaylistsAdapter mAdapter;

	public SonosPlaylistsListView(Context context) {
		super(context);
		init(context);
	}

	public SonosPlaylistsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SonosPlaylistsListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SonosPlaylistsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		mAdapter = new SonosPlaylistsAdapter(context);
		setAdapter(mAdapter);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		populate();
	}

	@Override
	public SonosPlaylistsAdapter getAdapter() {
		return mAdapter;
	}

	public void setDevice(SonosDevice device) {
		if (device == null || device != mDevice) {
			mDevice = device;
			populate();
		}
	}

	private void populate() {
		Log.e(TAG, "searchUPnPdevices");

		mAdapter.clear();

		if (mDevice == null) {
			return;
		}

		mDevice.getPlaylists()
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Action1<SonosPlaylist>() {
				@Override
				public void call(SonosPlaylist playlist) {
					mAdapter.add(playlist);
				}
			});
	}

}
