package com.dgmltn.sonnet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SonosListActivity extends Activity {

	private static final String TAG = SonosListActivity.class.getSimpleName();

	@InjectView(R.id.device_text)
	protected TextView vDeviceText;

	@InjectView(R.id.device_list)
	protected SonosDeviceListView vDeviceList;

	@InjectView(R.id.playlist_text)
	protected TextView vPlaylistText;

	@InjectView(R.id.playlists_list)
	protected SonosPlaylistsListView vPlaylistsList;

	private SonosDevice mChosenDevice;
	private SonosPlaylist mChosenPlaylist;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upnp);
		ButterKnife.inject(this);

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
