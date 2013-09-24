package com.yi4all.rupics;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.devspark.appmsg.AppMsg;
import com.umeng.fb.FeedbackAgent;
import com.umeng.socialize.controller.UMServiceFactory;
import com.yi4all.rupics.R;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.yi4all.rupics.db.IssueDBOpenHelper;
import com.yi4all.rupics.util.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class MainActivity extends BaseActivity {

	private final static String LOGTAG = "PostListActivity";

	protected IssueDBOpenHelper dbHelper = null;

	private boolean isTwiceQuit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_feedback:
				FeedbackAgent agent = new FeedbackAgent(this);
			    agent.startFeedbackActivity();
				break;
				
			case R.id.action_share:
				Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.share_app);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
				byte[] bitmapdata = stream.toByteArray();
				UMServiceFactory.shareTo(this, getString(R.string.shareAppMmsBody), bitmapdata);
				break;

			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (isTwiceQuit) {
				this.finish();
			} else {
				Utils.toastMsg(this, R.string.sure_quit_app);
				isTwiceQuit = true;

				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						isTwiceQuit = false;

					}
				}, 2000);
			}
			return true;
		default:
			return false;
		}
	}
	
	private IssueDBOpenHelper getHelper() {
		if (dbHelper == null) {
			dbHelper = OpenHelperManager
					.getHelper(this, IssueDBOpenHelper.class);
		}
		return dbHelper;
	}
	
}
