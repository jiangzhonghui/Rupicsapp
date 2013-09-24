package com.yi4all.rupics;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.devspark.appmsg.AppMsg;

public class SettingsActivity extends BaseActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);

		Button saveBtn = (Button) findViewById(R.id.saveBtn);
		saveBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveAccount();

			}
		});

		Button getAccountBtn = (Button) findViewById(R.id.getAccountBtn);
		getAccountBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// send an email for account
				// Intent i = new Intent(Intent.ACTION_SEND);
				// i.setType("text/plain");
				// i.putExtra(Intent.EXTRA_EMAIL , new
				// String[]{"vncntkarl2@gmail.com"});
				// i.putExtra(Intent.EXTRA_SUBJECT,
				// getString(R.string.sendMmsSubject));
				// i.putExtra(Intent.EXTRA_TEXT ,
				// getString(R.string.sendMmsBody));
				// try {
				// startActivity(Intent.createChooser(i, "Send mail..."));
				// } catch (android.content.ActivityNotFoundException ex) {
				// Toast.makeText(AccountEditorActivity.this,
				// "There are no email clients installed.",
				// Toast.LENGTH_SHORT).show();
				// }
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://ck101.com/member.php?mod=register.php"));
				startActivity(browserIntent);

			}
		});

		String fileName = DownloadUtil.initBaseDir() + "/.nomedia";
		File file = new File(fileName);
		final CheckBox hideBtn = (CheckBox) findViewById(R.id.hideImgBtn);
		hideBtn.setChecked(file.exists());

		hideBtn.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				String fileName = DownloadUtil.initBaseDir() + "/.nomedia";
				File file = new File(fileName);
				if (hideBtn.isChecked()) {
					// hide images
					try {
						file.createNewFile();
					} catch (IOException e) {
						Log.e("AccountEditorActivity", e.getMessage());
					}
				} else {
					// show images
					if (file.exists()) {
						file.delete();
					}
				}
				toastMsg(R.string.hideImgGalleryHint);
			}
		});

		// is only wifi
		CheckBox isOnlyWifiChk = (CheckBox) findViewById(R.id.onlyWifiChk);
		isOnlyWifiChk.setChecked(getOnlyWifi());
		isOnlyWifiChk.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				savePrefOnlyWifi(isChecked);

			}
		});

		// turn on the screen
		CheckBox onScreenWhileSlideshowChk = (CheckBox) findViewById(R.id.onScreenWhileSlideshowChk);
		onScreenWhileSlideshowChk.setChecked(getOnScreen());
		onScreenWhileSlideshowChk
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						savePrefOnScreen(isChecked);

					}
				});

		// get account from pref
		EditText emailTxt = (EditText) findViewById(R.id.emailTxt);
		emailTxt.setText(getAccount());

		EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);
		passwordTxt.setText(getPassword());

	}

	private void saveAccount() {

		EditText emailTxt = (EditText) findViewById(R.id.emailTxt);

		EditText passwordTxt = (EditText) findViewById(R.id.passwordTxt);

		savePrefRelease(emailTxt.getText().toString(), passwordTxt.getText()
				.toString());
		// successful message
		toastMsg(R.string.saveSuccess);
		// this.finish();

	}

	private void savePrefOnlyWifi(boolean flag) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putBoolean("onlywifi", flag);
		editor.commit();
	}

	public boolean getOnlyWifi() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getBoolean("onlywifi", false);

	}

	private void savePrefOnScreen(boolean flag) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putBoolean("onScreen", flag);
		editor.commit();
	}

	public boolean getOnScreen() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getBoolean("onScreen", false);

	}

	private void savePrefRelease(String account, String password) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString("account", account);
		editor.putString("password", password);
		editor.commit();
	}

	public String getAccount() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getString("account", "");

	}

	public String getPassword() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		return prefs.getString("password", "");
	}
	
	public void toastMsg(int resId, String... args) {
		final String msg = this.getString(resId, args);
		AppMsg.makeText(this, msg, AppMsg.STYLE_INFO).show();
	}
}