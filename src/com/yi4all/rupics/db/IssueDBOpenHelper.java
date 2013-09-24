package com.yi4all.rupics.db;

import java.sql.SQLException;
import java.util.Date;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class IssueDBOpenHelper extends OrmLiteSqliteOpenHelper {
	
	public  static final int DATABASE_VERSION = 2;
    
	public static final String DATABASE_NAME = "issues.db";
	
	// we do this so there is only one helper
		private static IssueDBOpenHelper helper = null;
		
		private Dao<UserModel, Integer> userDao;
	private Dao<CategoryModel, Integer> categoryDao;
	private Dao<IssueModel, Integer> issueDao;
	private Dao<ImageModel, Integer> imageDao;

    public IssueDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized IssueDBOpenHelper getHelper(Context context) {
		if (helper == null) {
			helper = new IssueDBOpenHelper(context);
			helper.getWritableDatabase();
		}
		return helper;
	}
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
    	try {
			Log.i(IssueDBOpenHelper.class.getName(), "onCreate");
			TableUtils.createTable(connectionSource, CategoryModel.class);
			TableUtils.createTable(connectionSource, IssueModel.class);
			TableUtils.createTable(connectionSource, ImageModel.class);
			TableUtils.createTable(connectionSource, UserModel.class);

		} catch (SQLException e) {
			Log.e(IssueDBOpenHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
        
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(IssueDBOpenHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, IssueModel.class, true);
			TableUtils.dropTable(connectionSource, CategoryModel.class, true);
			TableUtils.dropTable(connectionSource, ImageModel.class, true);
			TableUtils.dropTable(connectionSource, UserModel.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(IssueDBOpenHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
		
	}
	
	public Dao<CategoryModel, Integer> getCategoryDAO() throws SQLException{
    	if(categoryDao == null){
    		categoryDao = getDao(CategoryModel.class);
    	}
    	return categoryDao;
    }
    
    public Dao<IssueModel, Integer> getIssueDAO() throws SQLException{
    	if(issueDao == null){
    		issueDao = getDao(IssueModel.class);
    	}
    	return issueDao;
    }
    
    public Dao<UserModel, Integer> getUserDAO() throws SQLException{
    	if(userDao == null){
    		userDao = getDao(UserModel.class);
    	}
    	return userDao;
    }
    
    public Dao<ImageModel, Integer> getImageDAO() throws SQLException{
    	if(imageDao == null){
    		imageDao = getDao(ImageModel.class);
    	}
    	return imageDao;
    }
    
    @Override
	public void close() {
		super.close();
		categoryDao = null;
		issueDao = null;
		imageDao = null;
		userDao = null;
	}

}
