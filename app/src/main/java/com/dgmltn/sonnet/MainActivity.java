package com.dgmltn.sonnet;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends NFCActivity implements SonosItemAdapter.ItemClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private SonosConfig mConfig;

	private Handler mHandler = new Handler();

	@Bind(R.id.root)
	protected FrameLayout vRoot;

	@Bind(R.id.playlist)
	protected SonosItemGridView vPlaylist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		vPlaylist.setItemClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		populatePlaylist();

		showCurrentConfigDialog(false, 5);
	}

	private void populatePlaylist() {
		if (mConfig == null) {
			mConfig = Pref.getSonosConfig(this);
			if (mConfig == null) {
				startActivity(new Intent(this, SonosListActivity.class));
				return;
			}
		}
		vPlaylist.populatePlaylist(mConfig.getDevice(), mConfig.getPlaylist());
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int action = event.getAction();
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_DOWN) {
				//TODO: volume control
				Log.e("DOUG", "pressed volume up");
				mConfig.getDevice()
					.getVolumeString()
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(new Action1<String>() {
						@Override
						public void call(String volume) {
							Log.e("DOUG", "getVolume: " + volume);
						}
					});
			}
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				//TODO
			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	@Override
	public void onClick(SonosItem item, int position) {
		mConfig.getDevice()
			.playSonosItemNow(item)
			.subscribeOn(Schedulers.io())
			.observeOn(AndroidSchedulers.mainThread())
			.subscribe();
	}

	private void showCurrentConfigDialog(boolean flash, int numberOfSeconds) {
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

		final long dismiss = System.currentTimeMillis() + numberOfSeconds * DateUtils.SECOND_IN_MILLIS;

		View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_and_playlist, null);
		TextView deviceName = (TextView) view.findViewById(R.id.device_name);
		deviceName.setText(mConfig.getDevice().getRoomName());
		TextView playlistName = (TextView) view.findViewById(R.id.playlist_name);
		playlistName.setText(mConfig.getPlaylist().title);

		final MaterialDialog dialog = new MaterialDialog.Builder(this)
			.theme(Theme.DARK)
			.customView(view, false)
			.show();

		final Runnable dismissCallback = new Runnable() {
			@Override
			public void run() {
				dialog.dismiss();
			}
		};

		final Runnable dismissAndChooseDevice = new Runnable() {
			@Override
			public void run() {
				dialog.dismiss();
				Log.e("DOUG", "long pressed device name button");
			}
		};

		final Runnable dismissAndChoosePlaylist = new Runnable() {
			@Override
			public void run() {
				dialog.dismiss();
				Log.e("DOUG", "long pressed playlist button");
			}
		};

		View.OnTouchListener touchListener = new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mHandler.removeCallbacksAndMessages(null);
					Runnable runnable = v.getId() == R.id.device_name ? dismissAndChooseDevice : dismissAndChoosePlaylist;
					mHandler.postDelayed(runnable, 5 * DateUtils.SECOND_IN_MILLIS);
					return true;
				case MotionEvent.ACTION_UP:
					mHandler.removeCallbacksAndMessages(null);
					mHandler.postDelayed(dismissCallback, dismiss - System.currentTimeMillis());
					return true;
				}
				return false;
			}
		};

		deviceName.setOnTouchListener(touchListener);
		playlistName.setOnTouchListener(touchListener);

		mHandler.postDelayed(dismissCallback, dismiss - System.currentTimeMillis());
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
		showCurrentConfigDialog(foreground, 5);
	}
}
