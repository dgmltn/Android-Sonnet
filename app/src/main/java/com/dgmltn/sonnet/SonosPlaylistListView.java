package com.dgmltn.sonnet;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * A ListView that automatically populates itself with a list of SonosDevice devices.
 * Created by doug on 3/26/15.
 */
public class SonosPlaylistListView extends ListView {

	private static final String TAG = SonosPlaylistListView.class.getSimpleName();

	private SonosItemAdapter mAdapter;

	public SonosPlaylistListView(Context context) {
		super(context);
		init(context);
	}

	public SonosPlaylistListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SonosPlaylistListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public SonosPlaylistListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private void init(Context context) {
		mAdapter = new SonosItemAdapter(context);
	//	setAdapter(mAdapter);


		setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ClipboardManager clipboard = (ClipboardManager) getContext()
					.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("data", mAdapter.getItem(position).albumArtUri);
				clipboard.setPrimaryClip(clip);
			}
		});
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		getPlaylist(SonosConfig.ZONE_OFFICE, SonosConfig.PLAYLIST_HIP_HOP);
	}

	private void getPlaylist(final SonosDevice device, SonosPlaylist playlist) {
		Log.e(TAG, "getPlaylist");

		mAdapter.clear();
		device.getPlaylistItems(playlist)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Action1<SonosItem>() {
				@Override
				public void call(SonosItem item) {
					mAdapter.add(item);
				}
			});
	}

}
