package com.dgmltn.sonnet;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by doug on 3/20/15.
 */
public class NFCActivity extends Activity {

	public static final String TAG = NFCActivity.class.getSimpleName();

	public static final String FOREGROUND_DISPATCH = "NFCActivity.FOREGROUND_DISPATCH";

	private NfcAdapter mNfcAdapter;
	private boolean mForeground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

		Log.e(TAG, "onCreate");
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		/**
		 * This method gets called, when a new Intent gets associated with the current activity instance.
		 * Instead of creating a new activity, onNewIntent will be called. For more information have a look
		 * at the documentation.
		 *
		 * In our case this method gets called, when the user attaches a Tag to the device.
		 */
		Log.e(TAG, "onNewIntent");
		handleIntent(intent);
	}

	public void onNFCTagDiscovered(Tag tag, String id, boolean foreground) {
		Log.e(TAG, "NFC Tag Discovered: " + id);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mForeground = false;
		Log.e(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();

		/**
		 * It's important, that the activity is in the foreground (resumed). Otherwise
		 * an IllegalStateException is thrown.
		 */
		setupForegroundDispatch(this, mNfcAdapter);
		mForeground = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
		mForeground = false;
		Log.e(TAG, "onStop");
	}

	@Override
	protected void onPause() {
		/**
		 * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
		 */
		stopForegroundDispatch(this, mNfcAdapter);

		super.onPause();
	}

	private void handleIntent(Intent intent) {
		String action = intent.getAction();
		Log.e(TAG, "handleIntent: " + action);

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
			// In case we would still use the Tech Discovered Intent
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			String id = bytesToHex(tag.getId());
			Log.e(TAG, "TAG ID = " + id);
			onNFCTagDiscovered(tag, id, mForeground);
		}
	}

	/**
	 * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
	 * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());

		final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

		IntentFilter[] filters = new IntentFilter[1];
		String[][] techList = new String[][]{
			{"android.nfc.tech.IsoDep"},
			{"android.nfc.tech.NfcA"},
			{"android.nfc.tech.NfcB"},
			{"android.nfc.tech.NfcF"},
			{"android.nfc.tech.NfcV"},
			{"android.nfc.tech.Ndef"},
			{"android.nfc.tech.NdefFormatable"},
			{"android.nfc.tech.MifareClassic"},
			{"android.nfc.tech.MifareUltralight"}
		};

		// Notice that this is the same filter as in our manifest.
		filters[0] = new IntentFilter();
		filters[0].addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

		adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
	}

	/**
	 * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
	 * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
		adapter.disableForegroundDispatch(activity);
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
