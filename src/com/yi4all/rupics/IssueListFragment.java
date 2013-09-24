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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import android.R.color;
import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView.ScaleType;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.devspark.appmsg.AppMsg;
import com.yi4all.rupics.R;
import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.extra.MultiColumnPullToRefreshListView;
import com.huewu.pla.lib.extra.MultiColumnPullToRefreshListView.OnRefreshListener;
import com.huewu.pla.lib.internal.PLA_AbsListView;
import com.huewu.pla.lib.internal.PLA_AbsListView.OnScrollListener;
import com.huewu.pla.lib.internal.PLA_AdapterView;
import com.huewu.pla.lib.internal.PLA_AdapterView.OnItemClickListener;
import com.yi4all.rupics.CategoryListFragment.ImageCallback;
import com.yi4all.rupics.db.CategoryModel;
import com.yi4all.rupics.db.IssueModel;
import com.yi4all.rupics.service.ServiceImpl.QueryCallBack;
import com.yi4all.rupics.util.Utils;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView implementation with the
 * key addition being the ImageWorker class w/ImageCache to load children asynchronously, keeping the UI nice and smooth
 * and caching thumbnails for quick retrieval. The cache is retained over configuration changes like orientation change
 * so the images are populated quickly if, for example, the user rotates the device.
 */
public class IssueListFragment extends Fragment {
	private static final String LOGTAG = "IssueListFragment";

	private int page = 0; // current page of local source
	private boolean isNoMoreIssue = false;

	protected DownloadUtil util = new DownloadUtil();

	private List<IssueModel> issueList = new ArrayList<IssueModel>();
	private CategoryModel category;

	private MultiColumnPullToRefreshListView postListView;
	private PostAdapter adapter;

	protected int selected_item;

	private DownloadPostTask postTask = null;

	private Bitmap no_image;

	private Bitmap default_image;

	int SCREEN_WIDTH;
	int SCREEN_HEIGHT;

	private Display display;

	private int gridNumColumns = 2;

	public IssueListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setHasOptionsMenu(true);

		category = (getArguments() != null ? (CategoryModel) getArguments().getSerializable("category") : null);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.issue_list_fragment, container, false);
		postListView = (MultiColumnPullToRefreshListView) v.findViewById(R.id.issueListView);
		// postListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		default_image = BitmapFactory.decodeResource(getResources(), R.drawable.download_32);
		no_image = BitmapFactory.decodeResource(getResources(), R.drawable.no_image);
		
		TextView tv = (TextView) v.findViewById(R.id.issue_title_txt);
		tv.setText(category.getName());
		
		ImageView iv = (ImageView) v.findViewById(R.id.issue_back_btn);
		iv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});

		display = getActivity().getWindowManager().getDefaultDisplay();
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		SCREEN_WIDTH = width;
		SCREEN_HEIGHT = height;

		initPostList();

		return v;
	}

	private void initPostList() {

		// post list
		postListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(PLA_AdapterView<?> adapter, View arg1, int position, long arg3) {
				// for the last row
				if (issueList.size() == 0 || position > issueList.size())
					return;

				selected_item = position;

				final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
				i.putExtra("issue", issueList.get(position - 1));

				startActivity(i);
			}

		});
		
		postListView.setIconDrawable(R.drawable.refresh_icon);

		postListView.setShowLastUpdatedText(true);

		postListView.setLastUpdatedDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

		postListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
					
						List<IssueModel> list = ((BaseActivity) getActivity()).getService().getIssueByCategoryRemote(category, 1);
						
						boolean isUpdated = false;
						int updateAmount = 0;
						
						if(issueList.size() > 0){
							for(IssueModel im : list){
								if(im.getId() == issueList.get(0).getId()){
									break;
								}else{
									updateAmount ++;
								}
							}
							isUpdated = updateAmount > 0;
						}else{
							if(list.size() > 0){
								updateAmount = list.size();
							}
						}
						
						if(isUpdated){
							Utils.toastMsg(getActivity(), R.string.update_amount, String.valueOf(updateAmount));
							issueList = list;
						}else{
							Utils.toastMsg(getActivity(), R.string.nomore);
						}

						refreshList(0);
					}
				}).start();

					}
		});

		LayoutInflater inflater = LayoutInflater.from(this.getActivity());
		final View row = inflater.inflate(R.layout.more_list_item, null);

		final ProgressBar pb = (ProgressBar) row.findViewById(R.id.postListPB);
		pb.setVisibility(View.GONE);

		final TextView title = (TextView) row.findViewById(R.id.row_title);

		postListView.addFooterView(row);

		postListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(PLA_AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(PLA_AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (issueList.size() == 0)
					return;

				if ((firstVisibleItem + visibleItemCount) >= (issueList.size() - 1)
						&& (postTask.getStatus() != postTask.getStatus().RUNNING)) {
					if (category == null) {
						title.setText(R.string.need_select_source);
						pb.setVisibility(View.GONE);
					} else {

						// set the title
						if (isNoMoreIssue) {
							title.setText(R.string.nomore);
							pb.setVisibility(View.GONE);
							
							new Handler().postDelayed(new Runnable() {
								
								@Override
								public void run() {
									row.setVisibility(View.GONE);
								}
							}, 1000);
							
						} else {
							pb.setVisibility(View.VISIBLE);
							if (postTask != null && !postTask.isCancelled()) {
								postTask.cancel(true);
							}
							postTask = new DownloadPostTask(pb, title, issueList.size() - 1);
							postTask.execute();
						}
					}
				}

			}
		});

		if (category == null) {
			title.setText(R.string.need_select_source);
			pb.setVisibility(View.GONE);
		} else {
			if (postTask != null && !postTask.isCancelled()) {
				postTask.cancel(true);
			}
			postTask = new DownloadPostTask(pb, title, issueList.size() - 1);
			postTask.execute();
		}
	}

	public void refreshList(final int scrollTo) {
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				
				postListView.onRefreshComplete();

				if (adapter == null) {
					adapter = new PostAdapter(getActivity());
					postListView.setAdapter(adapter);
				} else {
					adapter.notifyDataSetChanged();
				}
			}
		});

	}

	private class PostAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public PostAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return issueList.size();
		}

		public IssueModel getItem(int i) {
			return issueList.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		public View getView(final int position, View convertView, ViewGroup vg) {
			if (issueList == null || position < 0 || position > issueList.size())
				return null;

			final View row = mInflater.inflate(R.layout.category_list_item, null);

			ViewHolder holder = (ViewHolder) row.getTag();
			if (holder == null) {
				holder = new ViewHolder(row);
				row.setTag(holder);
			}

			// other normal row
			final IssueModel rm = issueList.get(position);

			// set name to label
			holder.title.setText(rm.getImageAmount() + "p");

			// set triangle for the add
			if (rm.getCover() != null && rm.getCover().length() > 0) {
				holder.demo.setVisibility(View.VISIBLE);
				String path = rm.getCover();
				String localpath = Utils.convertUrl2Path(getActivity(), path);
				File f = new File(localpath);

				// 1. check f is exist
				if (f.exists()) {
					holder.demo.setImageBitmap(Utils.dealImg(localpath,
							false,
							SCREEN_WIDTH,
							SCREEN_HEIGHT,
							display.getRotation(),
							gridNumColumns,
							no_image));
				} else {
					holder.demo.setImageBitmap(decodeFile(path, localpath, false, new ImageCallback(holder.demo)));
				}

				// if (path.startsWith("http://")) {
				// ImageLoader.getInstance().displayImage(path, holder.demo);
				// }else{
				// ImageLoader.getInstance().displayImage("file://" + path,
				// holder.demo);
				// }
			} else {
				holder.demo.setVisibility(View.GONE);
			}

			return (row);
		}

	}


	class ImageCallback {
		WeakReference<ImageView> iv;
		int position;

		ImageCallback(ImageView iv) {
			this.iv = new WeakReference<ImageView>(iv);
			this.position = -1;
		}

		ImageCallback(int position) {
			this.iv = null;
			this.position = position;
		}

		public void setImageViewWH(boolean flag) {
			if (iv == null || iv.get() == null)
				return;
			if (flag) {
				int width_tmp = 0, height_tmp = 0;
				if (display.getRotation() == 0) {
					width_tmp = SCREEN_WIDTH / gridNumColumns;
					height_tmp = width_tmp * 4 / 3;
				} else {
					width_tmp = SCREEN_HEIGHT / gridNumColumns;
					height_tmp = width_tmp * 3 / 4;
				}

				iv.get().setLayoutParams(new FrameLayout.LayoutParams(width_tmp, height_tmp));
				iv.get().setScaleType(ScaleType.CENTER_CROP);
			} else {
				iv.get().setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.WRAP_CONTENT));
				iv.get().setScaleType(ScaleType.FIT_CENTER);
			}
		}

		public void setDrawable(final Bitmap bitmap) {
			if (bitmap == null || getActivity() == null)
				return;
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (iv != null && iv.get() != null) {
						int width = bitmap.getWidth();
						int height = bitmap.getHeight();
						iv.get().setLayoutParams(new FrameLayout.LayoutParams(SCREEN_WIDTH / 2, SCREEN_WIDTH * height
								/ (width * 2)));
						iv.get().setImageBitmap(bitmap);
					}
				}
			});
		}
	}

	private Bitmap decodeFile(final String url,
			final String path,
			final boolean isOriginal,
			final ImageCallback callback) {

		if(((BaseActivity)getActivity()).getService().sureLimitation()){
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.arg1 == 0) {
					callback.setImageViewWH(true);
					String tmp = (String) msg.obj;
					callback.setDrawable(Utils.dealImg(tmp, isOriginal, SCREEN_WIDTH, SCREEN_HEIGHT, display.getRotation(), gridNumColumns, no_image));
					
					if(msg.arg2 > 0){
						((BaseActivity)getActivity()).getService().addUserConsumedKbytes(msg.arg2);
					}
				} else if(msg.arg1 > 0) {
					//update progress
//					callback.processViewOnProgress(msg.arg1, msg.arg2);
				} else {
					callback.setImageViewWH(false);
					callback.setDrawable(no_image);
				}
			}
		};
		util.runSaveUrl(path, url, handler);
		callback.setImageViewWH(false);
		}
		return default_image;
	}

	class ViewHolder {
		TextView title = null;
		ImageView demo = null;

		ViewHolder(View base) {
			this.title = (TextView) base.findViewById(R.id.row_title);
			this.demo = (ImageView) base.findViewById(R.id.demo);
		}
	}

	public class DownloadPostTask extends AsyncTask<String, Integer, List<IssueModel>> implements MyTask {

		private ProgressBar pb;
		private TextView infoTxt;
		private int oldPosition;

		public DownloadPostTask(ProgressBar pb, TextView infoTxt, int oldPosition) {
			this.pb = pb;
			this.infoTxt = infoTxt;
			this.oldPosition = oldPosition;
		}

		@Override
		protected void onPreExecute() {
			infoTxt.setText(R.string.waitPost);
		}

		@Override
		protected List<IssueModel> doInBackground(String... inputUrls) {

			List<IssueModel> pmList = null;

			page++;

			pmList = ((BaseActivity) getActivity()).getService().getIssueByCategory(category, page);

			if (pmList.size() == 0) {
				pmList = ((BaseActivity) getActivity()).getService().getIssueByCategoryRemote(category, page);

				if (pmList.size() == 0) {
					setMyTitle(R.string.nomore);
					isNoMoreIssue = true;
					return pmList;
				} else {
					return pmList;
				}
			}

			publishProgress(100);
			return pmList;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			setMyTitle(R.string.canceledPostListDownload);
			setMyProgress(0);
			isNoMoreIssue = true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			setMyProgress(values[0]);
		}

		@Override
		protected void onPostExecute(List<IssueModel> result) {
			if (result != null) {
				issueList.addAll(result);
				// runUpdatePost(result);
				refreshList(oldPosition);
			}
			pb.setVisibility(View.GONE);
		}

		public void setMyProgress(final int value) {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {

					@Override
					public void run() {
						pb.setProgress(value);
					}

				});
			}

		}

		public void setMyTitle(final int value, final String... other) {
			if (getActivity() == null)
				return;
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					infoTxt.setText(getString(value, other));

				}

			});

		}

	}

	@Override
	public void onResume() {
		super.onResume();

		// page = 1;
		// if(currentSource != null){
		// postList = (ArrayList<PostModel>)
		// ((PostListActivity)getActivity()).getOtherPosts(page, 30,
		// isHasImages);
		// }else{
		// postList = new ArrayList<PostModel>();
		// }
		// refreshList(-1);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (postTask != null) {
			postTask.cancel(true);
			postTask = null;
		}
	}

	public void toastMsg(int resId) {
		final String msg = this.getString(resId);
		AppMsg.makeText(this.getActivity(), msg, AppMsg.STYLE_INFO).show();
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
}
