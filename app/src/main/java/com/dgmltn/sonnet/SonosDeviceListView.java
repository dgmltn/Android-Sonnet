package com.dgmltn.sonnet;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * A ListView that automatically populates itself with a list of SonosDevice devices.
 * Created by doug on 3/26/15.
 */
public class SonosDeviceListView extends ListView {

	private static final String TAG = SonosDeviceListView.class.getSimpleName();

	private SonosDeviceAdapter mAdapter;

	public SonosDeviceListView(Context context) {
		super(context);
		init(context);
	}

	public SonosDeviceListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SonosDeviceListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SonosDeviceListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		mAdapter = new SonosDeviceAdapter(context);
		setAdapter(mAdapter);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		searchUPnPdevices();
	}

	@Override
	public SonosDeviceAdapter getAdapter() {
		return mAdapter;
	}

	private void searchUPnPdevices() {
		Log.e(TAG, "searchUPnPdevices");

		mAdapter.clear();
		new UPnPDeviceFinder().observe()
			.map(new Func1<UPnPDevice, SonosDevice>() {
				@Override
				public SonosDevice call(UPnPDevice device) {
					return SonosDevice.newInstance(device);
				}
			})
			.filter(new Func1<SonosDevice, Boolean>() {
				@Override
				public Boolean call(SonosDevice sonosDevice) {
					return sonosDevice != null;
				}
			})
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Action1<SonosDevice>() {
				@Override
				public void call(SonosDevice device) {
					mAdapter.add(device);
				}
			});
	}

}
