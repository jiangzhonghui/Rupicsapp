package com.yi4all.rupics.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.yi4all.rupics.db.CategoryModel;
import com.yi4all.rupics.db.ImageModel;
import com.yi4all.rupics.db.IssueDBOpenHelper;
import com.yi4all.rupics.db.IssueModel;
import com.yi4all.rupics.db.UserModel;

public class DBServiceImpl implements IDBService {

	private static final String LOG_TAG = "DBServiceImpl";

	private IssueDBOpenHelper userHelper;

	private DBServiceImpl(Context context) {
		// this.commonHelper = CommonDBOpenHelper.getHelper(context);
		this.userHelper = IssueDBOpenHelper.getHelper(context);
	}

	public static IDBService getInstance(Context context) {
		return new DBServiceImpl(context);
	}

	@Override
	public void close() {
		if (userHelper != null) {
			OpenHelperManager.releaseHelper();
			userHelper = null;
		}
	}

	public IssueDBOpenHelper getUserHelper() {
		return userHelper;
	}

	@Override
	public UserModel queryUserByEmail(String email, String password) {
		try {
			Dao<UserModel, Integer> udao = userHelper.getUserDAO();

			QueryBuilder<UserModel, Integer> queryBuilder = udao.queryBuilder();
			Where<UserModel, Integer> where = queryBuilder.where();
			where.eq(UserModel.FIELD_EMAIL, email);
			if (password != null) {
				where.and();
				where.eq(UserModel.FIELD_PASSWORD, password);
			}

			return udao.queryForFirst(queryBuilder.prepare());

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return null;
	}

	@Override
	public boolean createUser(UserModel user) {
		try {
			Dao<UserModel, Integer> udao = userHelper.getUserDAO();

			// 1.query user by phone and email
			if (checkDuplicatedUser(user)) {

			} else {
				udao.create(user);
			}

			return true;
		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return false;
	}

	public boolean checkDuplicatedUser(UserModel user) {
		if (user == null)
			return false;
		try {
			Dao<UserModel, Integer> udao = userHelper.getUserDAO();

			if (user.getEmail() != null && user.getEmail().length() > 0) {
				List<UserModel> list;

				list = udao.queryForEq(UserModel.FIELD_EMAIL, user.getEmail());

				if (list != null && list.size() > 0) {
					return true;
				}
			}
		} catch (SQLException e) {
			Log.e(LOG_TAG, "DB error:" + e.getMessage());
		}
		return false;
	}

	@Override
	public UserModel queryUserBySid(String sid) {
		try {
			Dao<UserModel, Integer> udao = userHelper.getUserDAO();

			List<UserModel> list = udao.queryForEq(UserModel.FIELD_SID, sid);

			if (list != null && list.size() > 0) {
				return list.get(0);
			}

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return null;
	}

	@Override
	public UserModel queryDefaultUser() {
		try {
			Dao<UserModel, Integer> udao = userHelper.getUserDAO();

			List<UserModel> list = udao.queryForAll();

			if (list != null && list.size() > 0) {
				return list.get(0);
			}

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return null;
	}

	@Override
	public boolean updateUser(UserModel user) {
		try {
			Dao<UserModel, Integer> udao = userHelper.getUserDAO();

			udao.update(user);

			return true;
		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return false;
	}

	@Override
	public List<CategoryModel> getAllCategory() {
		try {
			Dao<CategoryModel, Integer> udao = userHelper.getCategoryDAO();

			QueryBuilder<CategoryModel, Integer> queryBuilder = udao.queryBuilder();
			queryBuilder.orderBy(IssueModel.CREATED_AT, false);
			
			return udao.query(queryBuilder.prepare());

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return new ArrayList<CategoryModel>();
	}

	@Override
	public List<IssueModel> getIssueByCategory(CategoryModel catgegory, int page) {
		try {
			Dao<IssueModel, Integer> dba = userHelper.getIssueDAO();
			QueryBuilder<IssueModel, Integer> queryBuilder = dba.queryBuilder();
			queryBuilder.limit((long) Constants.AMOUNT_PER_PAGE);
			queryBuilder.offset((long) (page - 1) * Constants.AMOUNT_PER_PAGE);
			queryBuilder.orderBy(IssueModel.CREATED_AT, false);

			Where<IssueModel, Integer> where = queryBuilder.where();
			where.eq(IssueModel.CATEGORY, catgegory);

			return dba.query(queryBuilder.prepare());

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return new ArrayList<IssueModel>();
	}

	@Override
	public List<ImageModel> getImageByIssue(IssueModel issue) {
		try {
			Dao<ImageModel, Integer> dba = userHelper.getImageDAO();

			QueryBuilder<ImageModel, Integer> queryBuilder = dba.queryBuilder();
			queryBuilder.orderBy(ImageModel.ORDER, true);

			Where<ImageModel, Integer> where = queryBuilder.where();
			where.eq(ImageModel.ISSUE, issue);

			return dba.query(queryBuilder.prepare());

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
		return new ArrayList<ImageModel>();
	}

	@Override
	public void updateCategories(List<CategoryModel> list) {
		try {
			Dao<CategoryModel, Integer> udao = userHelper.getCategoryDAO();

			for (CategoryModel cm : list) {
				CategoryModel im2 = udao.queryForId((int) cm.getId());
				if (im2 != null) {
					udao.create(cm);
				}
			}

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}
	}

	@Override
	public void updateIssues(List<IssueModel> list, CategoryModel cm) {
		try {
			Dao<IssueModel, Integer> udao = userHelper.getIssueDAO();

			for (IssueModel im : list) {
				im.setCategory(cm);
				IssueModel im2 = udao.queryForId((int) im.getId());
				if (im2 != null) {
					udao.create(im);
				}
			}

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}

	}

	@Override
	public void updateImages(List<ImageModel> list, IssueModel iim) {
		try {
			Dao<ImageModel, Integer> udao = userHelper.getImageDAO();

			for (ImageModel im : list) {
				im.setIssue(iim);
				ImageModel im2 = udao.queryForId((int) im.getId());
				if (im2 != null) {
					udao.create(im);
				}
			}

		} catch (SQLException e) {

			Log.e(LOG_TAG, e.getMessage());
		}

	}

}
