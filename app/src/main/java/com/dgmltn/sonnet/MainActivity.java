package com.dgmltn.sonnet;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends NFCActivity implements SonosItemAdapter.ItemClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private SonosConfig mConfig;

	private SonosItemAdapter mAdapter;
	private Handler mHandler = new Handler();

	@InjectView(R.id.root)
	protected FrameLayout vRoot;

	@InjectView(R.id.device_name)
	protected TextView vDeviceName;

	@InjectView(R.id.playlist)
	protected RecyclerView vPlaylist;

	private Subscription mSubscription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.inject(this);

		// Initialize playlist
		vPlaylist.setHasFixedSize(true);
		GridLayoutManager glm = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
		vPlaylist.setLayoutManager(glm);
//		LinearLayoutManager llm = new LinearLayoutManager(this);
//		llm.setOrientation(LinearLayoutManager.VERTICAL);
//		vPlaylist.setLayoutManager(llm);
	}

	@Override
	protected void onResume() {
		super.onResume();

		populatePlaylist();

		showDeviceName(false, 5);
	}

	private void populatePlaylist() {
		if (mConfig == null) {
			mConfig = Pref.getSonosConfig(this);
			if (mConfig == null) {
				startActivity(new Intent(this, SonosListActivity.class));
				return;
			}
		}
		if (mAdapter == null) {
			mAdapter = new SonosItemAdapter(this);
			mAdapter.setItemClickListener(this);
			vPlaylist.setAdapter(mAdapter);
		}
		if (mSubscription != null && !mSubscription.isUnsubscribed()) {
			mSubscription.unsubscribe();
		}
		mAdapter.clear();
		mSubscription = mConfig.getDevice()
			.getPlaylistItems(mConfig.getPlaylist())
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe(new Action1<SonosItem>() {
				@Override
				public void call(SonosItem item) {
					mAdapter.add(item);
				}
			});
	}

	@Override
	public void onClick(SonosItem item, int position) {
		mConfig.getDevice()
			.playSonosItemNow(item)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe();
	}

	private void showDeviceName(boolean flash, int numberOfSeconds) {
		if (vRoot == null || mConfig == null) {
			return;
		}

		if (flash) {
			Drawable flasher = new ColorDrawable(Color.WHITE);
			vRoot.setForeground(flasher);
			ObjectAnimator anim = ObjectAnimator.ofInt(flasher, "alpha", 255, 0);
			anim.setInterpolator(new DecelerateInterpolator());
			anim.setDuration(1000).start();
		}

		vDeviceName.setText(mConfig.getDevice().getRoomName());
		vDeviceName.setVisibility(View.VISIBLE);
		vDeviceName.setAlpha(1f);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				vDeviceName.animate().alpha(0f).setDuration(500).withEndAction(new Runnable() {
					@Override
					public void run() {
						vDeviceName.setVisibility(View.INVISIBLE);
					}
				}).start();
			}
		}, numberOfSeconds * DateUtils.SECOND_IN_MILLIS);
	}

	@Override
	public void onNFCTagDiscovered(Tag tag, String id, boolean foreground) {
		Log.e(TAG, "NFC Tag Discovered: " + id + " foreground? " + foreground);

		if (mConfig == null) {
			mConfig = new SonosConfig(SonosConfig.ZONE_OFFICE, SonosConfig.PLAYLIST_GIRLS);
		}

		switch (id) {
		case "AED4D515":
			mConfig.setDevice(SonosConfig.ZONE_OFFICE);
			mConfig.setPlaylist(SonosConfig.PLAYLIST_GIRLS);
			break;
		case "AE5BD215":
			mConfig.setDevice(SonosConfig.ZONE_OFFICE);
			mConfig.setPlaylist(SonosConfig.PLAYLIST_HIP_HOP);
			break;
		case "4E2BD115":
			mConfig.setDevice(SonosConfig.ZONE_FAMILY_ROOM);
			mConfig.setPlaylist(SonosConfig.PLAYLIST_GIRLS);
			break;
		}

		Pref.setSonosConfig(this, mConfig);
		populatePlaylist();
		showDeviceName(foreground, 5);
	}
}
