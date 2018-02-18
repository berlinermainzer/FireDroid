package de.felten.firedroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Toast;

@TargetApi(16)
public class MainActivity extends Activity implements OnClickListener {

	public enum LedButtonState {
		RANDOM, ON, OFF
	};

	private NumberPicker redPicker;
	private NumberPicker yellowPicker;
	private NumberPicker orangePicker;
	private String ip;
	private String port;
	ImageButton redLedButton;
	ImageButton yellowLedButton;
	ImageButton orangeLedButton;
	@SuppressLint("UseSparseArrays")
	Map<Integer, LedButtonState> buttonStates = new HashMap<Integer, MainActivity.LedButtonState>(
			3);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		redPicker = (NumberPicker) findViewById(R.id.redNumberPicker);
		redPicker.setMaxValue(250);
		redPicker.setMinValue(1);
		redPicker.setValue(5);

		yellowPicker = (NumberPicker) findViewById(R.id.yellowNumberPicker);
		yellowPicker.setMaxValue(250);
		yellowPicker.setMinValue(1);
		yellowPicker.setValue(3);

		orangePicker = (NumberPicker) findViewById(R.id.orangeNumberPicker);
		orangePicker.setMaxValue(250);
		orangePicker.setMinValue(1);
		orangePicker.setValue(5);

		ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(this);

		ImageButton updateButton = (ImageButton) findViewById(R.id.updateButton);
		updateButton.setOnClickListener(this);

		redLedButton = (ImageButton) findViewById(R.id.redButton);
		redLedButton.setOnClickListener(this);
		buttonStates.put(R.id.redButton, LedButtonState.RANDOM);

		yellowLedButton = (ImageButton) findViewById(R.id.yellowButton);
		yellowLedButton.setOnClickListener(this);
		buttonStates.put(R.id.yellowButton, LedButtonState.RANDOM);

		orangeLedButton = (ImageButton) findViewById(R.id.orangeButton);
		orangeLedButton.setOnClickListener(this);
		buttonStates.put(R.id.orangeButton, LedButtonState.RANDOM);

		loadPrefs();
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		/*
		 * Because it's onlt ONE option in the menu. In order to make it simple,
		 * We always start SetPreferenceActivity without checking.
		 */

		Intent intent = new Intent();
		intent.setClass(MainActivity.this, PreferenceActivity.class);
		startActivityForResult(intent, 0);

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
		/*
		 * To make it simple, always re-load Preference setting.
		 */
		loadPrefs();
	}

	private void loadPrefs() {
		SharedPreferences mySharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);

		String serverNamePref = mySharedPreferences.getString(
				"prefs_server_name_key", "192.168.0.42");
		ip = serverNamePref;

		String serverPortPref = mySharedPreferences.getString(
				"prefs_server_port_key", "8080");
		port = serverPortPref;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.refreshButton:
			refresh();
			break;
		case R.id.updateButton:
			update();
			break;
		case R.id.redButton:
			changeState(R.id.redButton);
			break;
		case R.id.yellowButton:
			changeState(R.id.yellowButton);
			break;
		case R.id.orangeButton:
			changeState(R.id.orangeButton);
			break;
		default:
			break;
		}
	}

	private void changeState(int buttonId) {
		LedButtonState currentState = buttonStates.get(buttonId);
		switch (currentState) {
		case RANDOM:
			buttonStates.put(buttonId, LedButtonState.ON);
			break;
		case ON:
			buttonStates.put(buttonId, LedButtonState.OFF);
			break;
		case OFF:
			buttonStates.put(buttonId, LedButtonState.RANDOM);
		default:
			break;
		}
		updateGUI();
	}

	private void updateGUI() {
		switch (buttonStates.get(R.id.yellowButton)) {
		case RANDOM:
			yellowLedButton
					.setImageResource(R.drawable.ic_led_yellow_square_random);
			yellowPicker.setEnabled(true);
			break;
		case ON:
			yellowLedButton.setImageResource(R.drawable.ic_led_yellow_square_1);
			yellowPicker.setEnabled(false);
			break;
		case OFF:
			yellowLedButton.setImageResource(R.drawable.ic_led_yellow_square_0);
			yellowPicker.setEnabled(false);
			break;
		}
		switch (buttonStates.get(R.id.redButton)) {
		case RANDOM:
			redLedButton.setImageResource(R.drawable.ic_led_red_square_random);
			redPicker.setEnabled(true);
			break;
		case ON:
			redLedButton.setImageResource(R.drawable.ic_led_red_square_1);
			redPicker.setEnabled(false);
			break;
		case OFF:
			redLedButton.setImageResource(R.drawable.ic_led_red_square_0);
			redPicker.setEnabled(false);
			break;
		}
		switch (buttonStates.get(R.id.orangeButton)) {
		case RANDOM:
			orangeLedButton
					.setImageResource(R.drawable.ic_led_orange_square_random);
			orangePicker.setEnabled(true);
			break;
		case ON:
			orangeLedButton.setImageResource(R.drawable.ic_led_orange_square_1);
			orangePicker.setEnabled(false);
			break;
		case OFF:
			orangeLedButton.setImageResource(R.drawable.ic_led_orange_square_0);
			orangePicker.setEnabled(false);
			break;
		}
		redPicker.invalidate();
		yellowPicker.invalidate();
		orangePicker.invalidate();
	}

	private void update() {
		String uri = "http://" + ip + ":" + port + "?";
		List<NameValuePair> params = new LinkedList<NameValuePair>();

		// "led1" Orange
		// "led2" Yellow
		// "led3" Red
		// http://192.168.0.42:8080/?action=update&id=led3&mode=0&delay=200
		params.add(new BasicNameValuePair("action", "update"));
		params.add(new BasicNameValuePair("id", "led1"));
		params.add(new BasicNameValuePair("mode", Integer.toString(buttonStates
				.get(R.id.orangeButton).ordinal())));
		params.add(new BasicNameValuePair("delay", Integer
				.toString(orangePicker.getValue())));
		params.add(new BasicNameValuePair("id", "led2"));
		params.add(new BasicNameValuePair("mode", Integer.toString(buttonStates
				.get(R.id.yellowButton).ordinal())));
		params.add(new BasicNameValuePair("delay", Integer
				.toString(yellowPicker.getValue())));
		params.add(new BasicNameValuePair("id", "led3"));
		params.add(new BasicNameValuePair("mode", Integer.toString(buttonStates
				.get(R.id.redButton).ordinal())));
		params.add(new BasicNameValuePair("delay", Integer.toString(redPicker
				.getValue())));
		uri += URLEncodedUtils.format(params, "utf-8");

		new RequestTask(this, getString(R.string.wait_update)).execute(uri);
	}

	/**
	 * Pull data from Arduino and push it into GUI
	 */
	private void refresh() {
		String uri = "http://" + ip + ":" + port;
		new RequestTask(this, getString(R.string.wait_refresh)).execute(uri);
	}

	class RequestTask extends AsyncTask<String, String, String> {
		private ProgressDialog mDialog;
		private Context context;
		private String progressMsg;

		public RequestTask(Context context, String progressMsg) {
			this.context = context;
			this.progressMsg = progressMsg;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = new ProgressDialog(context);
			mDialog.setMessage(progressMsg);
			mDialog.setCancelable(false);
			mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mDialog.show();
		}

		@Override
		protected String doInBackground(String... uri) {
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			HttpClient httpclient = new DefaultHttpClient(httpParameters);

			HttpResponse response;
			String responseString = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				Log.e("", "Exception occured", e);
			} catch (IOException e) {
				Log.e("", "Exception occured", e);
			} catch (Exception e) {
				Log.e("", "Exception occured", e);
			}
			return responseString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}

			processResult(result);
			updateGUI();

			// Do anything with response..
		}
	}

	public void processResult(String result) {
		if (result == null) {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle(R.string.server_not_found_title);
			String msg = getString(R.string.server_not_found);
			msg += "\n(" + ip + ":" + port + ")";
			alertDialog.setMessage(msg);
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			alertDialog.show();
			return;
		}

		// {"status": [{"id": "led1", "mode": "0", "delay": "5"}, {"id": "led2",
		// "mode": "0", "delay": "3"}, {"id": "led3", "mode": "0", "delay":
		// "5"}]}
		// ON, OFF, RANDOM
		try {
			JSONObject json = new JSONObject(result);
			JSONArray stati = json.getJSONArray("status");
			for (int s = 0; s < stati.length(); s++) {
				JSONObject status = stati.getJSONObject(s);
				String id = status.getString("id");
				int mode = status.getInt("mode");
				int delay = status.getInt("delay");

				if (id.equals("led1")) {
					// orange
					setState(R.id.orangeButton, mode);
					orangePicker.setValue(delay);

				} else if (id.equals("led2")) {
					// yellow
					setState(R.id.yellowButton, mode);
					yellowPicker.setValue(delay);

				} else if (id.equals("led3")) {
					// red
					setState(R.id.redButton, mode);
					redPicker.setValue(delay);
				}
			}

		} catch (JSONException e) {
			Toast.makeText(this, "json exexption: " + e.getLocalizedMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	private void setState(int buttonId, int mode) {
		switch (mode) {
		case 0:
			buttonStates.put(buttonId, LedButtonState.RANDOM);
			break;
		case 1:
			buttonStates.put(buttonId, LedButtonState.ON);
			break;
		case 2:
			buttonStates.put(buttonId, LedButtonState.OFF);
			break;
		default:
			break;
		}
	}
}
