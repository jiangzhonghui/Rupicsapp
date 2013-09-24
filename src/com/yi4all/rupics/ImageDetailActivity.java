/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yi4all.rupics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.umeng.socialize.controller.UMServiceFactory;
import com.yi4all.rupics.db.ImageModel;
import com.yi4all.rupics.db.IssueModel;
import com.yi4all.rupics.util.Utils;

public class ImageDetailActivity extends BaseActivity implements OnClickListener {
	public static final String EXTRA_IMAGE = "extra_image";

	public final static String PREF_WALLPAPER = "ppwallpaper";

	private static final String LOGTAG = "ImageDetailActivity";

	protected DownloadUtil util;

	private ImagePagerAdapter mAdapter;
	private ViewPager mPager;

	private View topBarPanel;
	
	private View popMenuPanel;

	private boolean isSlideshow;

	protected int imageSequence = 0;

	private IssueModel issue;
	private List<ImageModel> imgList;

	private Bitmap default_image;
	private Bitmap no_image;

	private int SCREEN_WIDTH;

	private int SCREEN_HEIGHT;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_detail_pager);

		util = new DownloadUtil();

		default_image = BitmapFactory.decodeResource(getResources(), R.drawable.download_32);
		no_image = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);

		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		// if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;

		issue = (IssueModel) getIntent().getSerializableExtra("issue");

		// Set up activity to go full screen
		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		initBtn();

		refreshData();

	}

	private void initPager() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), imgList.size());
				mPager = (ViewPager) findViewById(R.id.pager);
				mPager.setAdapter(mAdapter);
				mPager.setPageMargin(5);
				mPager.setOffscreenPageLimit(2);
				final TextView displayLbl = (TextView) findViewById(R.id.displayLbl);
				displayLbl.setText(imageSequence + 1 + "/" + imgList.size());
				mPager.setOnPageChangeListener(new OnPageChangeListener() {

					@Override
					public void onPageSelected(int position) {
						imageSequence = position;

						displayLbl.setText(imageSequence + 1 + "/" + imgList.size());
						if (imgList.size() != 0) {
							if (imageSequence == imgList.size() - 1) {
								Utils.toastMsg(ImageDetailActivity.this, R.string.alreadyLast);
							} else if (imageSequence == 0) {
								Utils.toastMsg(ImageDetailActivity.this, R.string.alreadyFirst);
							}
						}
					}

					@Override
					public void onPageScrolled(int arg0, float arg1, int arg2) {

					}

					@Override
					public void onPageScrollStateChanged(int arg0) {

					}
				});

				mPager.setCurrentItem(0);
				
			}
		});

	}

	private void refreshData() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				imgList = ImageDetailActivity.this.getService().getImageByIssueRemote(issue);

				if (imgList != null && imgList.size() > 0) {
					initPager();
				} else {
					Utils.toastMsg(ImageDetailActivity.this, R.string.noImages);
				}
			}
		};

		new Thread(runnable).start();
	}

	private void initBtn() {
		topBarPanel = findViewById(R.id.image_top_bar);
		topBarPanel.setVisibility(View.GONE);
		
		popMenuPanel = findViewById(R.id.image_pop_menu);
		
		TextView tv = (TextView) findViewById(R.id.image_title_txt);
		tv.setText(issue.getCategory().getName() + "-" + issue.getName());
		
		ImageView back = (ImageView) findViewById(R.id.image_back_btn);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		ImageView pop = (ImageView) findViewById(R.id.image_pop_btn);
		pop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(popMenuPanel.getVisibility() == View.VISIBLE){
					popMenuPanel.setVisibility(View.GONE);
				}else{
				popMenuPanel.setVisibility(View.VISIBLE);
				}
			}
		});
		
//		final TextView wallpaperBtn = (TextView) findViewById(R.id.wallpaperBtn);

		final TextView slideshowBtn = (TextView) findViewById(R.id.slideshowBtn);
		
		final TextView shareBtn = (TextView) findViewById(R.id.shareBtn);
		
		final TextView downloadBtn = (TextView) findViewById(R.id.downloadBtn);

//		wallpaperBtn.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				String img = imgList.get(imageSequence).getUrl();
//				String path = Utils.convertUrl2Path(ImageDetailActivity.this, img);
//				if (path != null && new File(path).exists()) {
//					try {
//						ImageDetailActivity.this.setWallpaper(BitmapFactory.decodeFile(path));
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				} else {
//					// give a tip to user
//					Utils.toastMsg(ImageDetailActivity.this, R.string.noImageWallpaper);
//				}
//				Utils.toastMsg(ImageDetailActivity.this, R.string.setWallpaperSuccess);
//			}
//		});

		slideshowBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				isSlideshow = !isSlideshow;
				if (isSlideshow) {

					startSlideShow();
					topBarPanel.setVisibility(View.GONE);
					popMenuPanel.setVisibility(View.GONE);
					
					slideshowBtn.setText(R.string.slideshow_pause);
				} else {
					topBarPanel.setVisibility(View.VISIBLE);
					
					slideshowBtn.setText(R.string.slideshow);
				}

			}
		});
		
		shareBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

//				Intent intent = createShareIntent();
				byte[] bytes = getCurrentImage();
				if(bytes == null){
				Utils.toastMsg(ImageDetailActivity.this, R.string.noImageShare);	
				}else{
//	    		ImageDetailActivity.this.startActivity(intent);
					popMenuPanel.setVisibility(View.GONE);
					UMServiceFactory.shareTo(ImageDetailActivity.this, getString(R.string.shareImageMmsBody), bytes);
				}

			}
		});
		
		downloadBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if(getService().sureLimitation()){
					popMenuPanel.setVisibility(View.GONE);
					
				final ProgressDialog pd = ProgressDialog.show(ImageDetailActivity.this, getString(R.string.waitTitle), getString(R.string.downloadAllImage), false, false);
				final Handler handler = new Handler(){
					int current = 0;
					int total = imgList.size();
					int size = 0;
					
					@Override
					public void handleMessage(Message msg) {
							current ++;
							
							pd.setMessage(getString(R.string.downloadAllImage) + current + "/" + total);
							
							if(msg.arg2 > 0){
								size += msg.arg2;
							}
							
							if(current >= total){
								if(size > 0){
									getService().addUserConsumedKbytes(size);
								}
								pd.dismiss();
							}
					}
				};
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						for(ImageModel im : imgList){
							String imgUrl = im.getUrl();
							String path = Utils.convertUrl2Path(ImageDetailActivity.this, imgUrl);
						util.runSaveUrl(path, imgUrl, handler);
						}
						
					}
				}).start();
				}
			}
		});

	}
	
	@Override
	public boolean onKeyDown(int keyCoder, KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (popMenuPanel.getVisibility() == View.VISIBLE) {
				popMenuPanel.setVisibility(View.GONE);
				return true;
			}else{
				return super.onKeyDown(keyCoder, event);
			}
			
		default:
			return false;
		}
	}
	
	private byte[] getCurrentImage(){
		String path = Utils.convertUrl2Path(this, imgList.get(imageSequence).getUrl());
		if (path != null && new File(path).exists()) {
			try {
				return IOUtils.toByteArray(new FileReader(path));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
        return null;
	}
	
	private Intent createShareIntent() {
		String path = Utils.convertUrl2Path(this, imgList.get(imageSequence).getUrl());
		if (path != null && new File(path).exists()) {
			Intent sendIntent = new Intent(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_SUBJECT,
					getString(R.string.sendShareMmsSubject));
			sendIntent.putExtra(Intent.EXTRA_TEXT,
					getString(R.string.sendShareMmsBody));
			sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
			sendIntent.setType("image/*");
			return sendIntent;
		}
		
        return null;
    }

	public void startSlideShow() {
		Runnable slide = new Runnable() {

			@Override
			public void run() {
				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, LOGTAG);
				wl.acquire();
				while (isSlideshow) {
					if (imageSequence < imgList.size() - 1) {
						// back to the next image
						imageSequence++;
						setCurrentPage();
					} else {
						// give a tip to user
						Utils.toastMsg(ImageDetailActivity.this, R.string.alreadyLast);
						isSlideshow = false;
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						Log.e(LOGTAG, "startSlideShow:" + ((e != null) ? e.getMessage() : "exception is null"));
					}

					// if(ImageDetailActivity.this.)
				}
				if (wl.isHeld())
					wl.release();
			}
		};
		new Thread(slide).start();

	}

	private void setCurrentPage() {
		final TextView displayLbl = (TextView) findViewById(R.id.displayLbl);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mPager.setCurrentItem(imageSequence, true);
				displayLbl.setText(imageSequence + 1 + "/" + imgList.size());
			}
		});
	}

	public Bitmap decodeFile(final String url, final boolean isOriginal, final ImageCallback callback) {
		String path = Utils.convertUrl2Path(ImageDetailActivity.this, url);

		File f = new File(path);

		// 1. check f is exist
		if (f.exists()) {
			callback.setImageViewWH();
			callback.hidePB();
			return Utils.dealImg(path, isOriginal, SCREEN_WIDTH, SCREEN_HEIGHT, no_image);
		} else if(getService().sureLimitation()){
			final Handler imgHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					if (msg.arg1 == 0) {
						callback.setImageViewWH();
						String tmp = (String) msg.obj;
						callback.setDrawable(Utils.dealImg(tmp, isOriginal, SCREEN_WIDTH, SCREEN_HEIGHT, no_image));
						
						if(msg.arg2 > 0){
							getService().addUserConsumedKbytes(msg.arg2);
						}
					} else if(msg.arg1 > 0) {
						//update progress
						callback.processViewOnProgress(msg.arg1, msg.arg2);
					}else {
						callback.setImageViewWH();
						callback.setDrawable(no_image);
					}
				}
			};
			util.runSaveUrl(path, url, imgHandler);
		}
		callback.setImageViewWH();
		return default_image;
	}

	public class ImageCallback {
		WeakReference<ImageView> iv;
		int position;

		public ImageCallback setIV(ImageView iv) {
			this.iv = new WeakReference(iv);
			return this;
		}

		public ImageCallback(ImageView iv) {
			this.iv = new WeakReference(iv);
			this.position = -1;
		}

		ImageCallback(int position) {
			this.iv = null;
			this.position = position;
		}

		public void setImageViewWH() {
			if (iv == null || iv.get() == null)
				return;
			iv.get().setScaleType(ScaleType.FIT_CENTER);
		}

		public void setDrawable(final Bitmap bitmap) {
			if (bitmap == null)
				return;
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (iv != null && iv.get() != null) {
						iv.get().setImageBitmap(bitmap);
						iv.get().invalidate();
						hidePB();
					}
				}
			});
		}
		
		public void processViewOnProgress(
				Integer bytesRead, Integer bytesTotal) {

			if (iv == null || iv.get() == null)
				return;
			LoadImageViewHolder holder = (LoadImageViewHolder) iv.get().getTag();
			float percent = 0.0f;
			
			if(bytesTotal>0)
			{
				percent = (bytesRead * 100.0f) / (bytesTotal + 100) ;
			}
			else
			{
				percent = (bytesRead/(20.0f * 1024.0f)) * 80.0f;
				
				if(percent > 80.0f)
				{
					percent = 80.0f + ((bytesRead - (20.0f * 1024.0f)) / (280.0f * 1024.0f) ) * 20.0f;
				}
				
				if(percent > 99.0f) percent = 100.0f;
			}
			
			if(holder != null){
				holder.title.setText(String.format("%0$.0f", percent) + "%");
				if(percent > 99.0f){
					holder.base.setVisibility(View.GONE);
				}
			}
		}
		
		public void hidePB(){
			LoadImageViewHolder holder = (LoadImageViewHolder) iv.get().getTag();
			
			if(holder != null){
					holder.base.setVisibility(View.GONE);
			}
		}
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	public DownloadUtil getUtil() {
		return util;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (isSlideshow) {
			startSlideShow();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		isSlideshow = false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (util != null) {
			util = null;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// We do nothing here. We're only handling this to keep orientation
		// or keyboard hiding from causing the WebView activity to restart.
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there could be a large
	 * number of items in the ViewPager and we don't want to retain them all in memory at once but create/destroy them
	 * on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
			// String path = Utils.convertUrl2Path(ImageDetailActivity.this, imgList.get(position).getUrl());
			return ImageDetailFragment.newInstance(imgList.get(position));
		}
	}

	/**
	 * Set on the ImageView in the ViewPager children fragments, to enable/disable low profile mode when the ImageView
	 * is touched.
	 */
	@TargetApi(11)
	@Override
	public void onClick(View v) {

		if (topBarPanel.getVisibility() == View.VISIBLE) {
			topBarPanel.setVisibility(View.GONE);
		} else {
			topBarPanel.setVisibility(View.VISIBLE);
		}
	}

}
