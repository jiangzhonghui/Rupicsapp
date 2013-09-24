package com.yi4all.rupics;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.WindowManager.LayoutParams;

import com.yi4all.rupics.db.CategoryModel;

public class IssueListActivity extends BaseActivity {

	private final static String LOGTAG = "IssueListActivity";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

//		getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
		
		CategoryModel category = (CategoryModel) getIntent().getSerializableExtra("category");

		if (getSupportFragmentManager().findFragmentByTag(LOGTAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.add(android.R.id.content, createFragment(category), LOGTAG);
			ft.commit();
		}

	}


	private IssueListFragment createFragment(CategoryModel category) {
		Bundle args = new Bundle();
		args.putSerializable("category", category);

		IssueListFragment f = new IssueListFragment();
		f.setArguments(args);
		return f;
	}

}
