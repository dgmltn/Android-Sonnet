package com.dgmltn.sonnet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SonosListActivity extends Activity {

	private static final String TAG = SonosListActivity.class.getSimpleName();

	@Bind(R.id.device_text)
	protected TextView vDeviceText;

	@Bind(R.id.device_list)
	protected SonosDeviceListView vDeviceList;

	@Bind(R.id.playlist_text)
	protected TextView vPlaylistText;

	@Bind(R.id.playlists_list)
	protected SonosPlaylistsListView vPlaylistsList;

	private SonosDevice mChosenDevice;
	private SonosPlaylist mChosenPlaylist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upnp);
		ButterKnife.bind(this);

		chooseDevice();

		vDeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mChosenDevice = vDeviceList.getAdapter().getItem(position);
				choosePlaylist();
			}
		});

		vPlaylistsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mChosenPlaylist = vPlaylistsList.getAdapter().getItem(position);
				setConfig();
			}
		});
	}

	private void chooseDevice() {
		vDeviceText.setVisibility(View.VISIBLE);
		vDeviceText.setText("Choose Device:");
		vDeviceList.setVisibility(View.VISIBLE);
		vPlaylistText.setVisibility(View.GONE);
		vPlaylistsList.setVisibility(View.GONE);
	}

	private void choosePlaylist() {
		vDeviceText.setVisibility(View.VISIBLE);
		vDeviceText.setText("Device: " + mChosenDevice.getRoomName());
		vDeviceList.setVisibility(View.GONE);
		vPlaylistText.setVisibility(View.VISIBLE);
		vPlaylistText.setText("Choose Playlist:");
		vPlaylistsList.setDevice(mChosenDevice);
		vPlaylistsList.setVisibility(View.VISIBLE);
	}

	private void setConfig() {
		SonosConfig config = new SonosConfig(mChosenDevice, mChosenPlaylist);
		Pref.setSonosConfig(this, config);
		finish();
	}
}
