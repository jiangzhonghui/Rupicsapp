package com.yi4all.rupics;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadImageViewHolder {

	View base = null;
	TextView title = null;
	ProgressBar pb = null;

	LoadImageViewHolder(View base) {
		this.base = base;
		this.title = (TextView) base.findViewById(R.id.imageWaitTxt);
		this.pb = (ProgressBar) base.findViewById(R.id.imageWaitPB);
	}
}
