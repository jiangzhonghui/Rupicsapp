package com.yi4all.rupics.service;

import java.util.List;

import com.yi4all.rupics.db.CategoryModel;
import com.yi4all.rupics.db.ImageModel;
import com.yi4all.rupics.db.IssueModel;
import com.yi4all.rupics.db.UserModel;

public interface IDBService {

	public void close();
	
	//users
	public UserModel queryUserByEmail(String email, String password);
	public UserModel queryUserBySid(String sid);
	public UserModel queryDefaultUser();
	public boolean createUser(UserModel user);
	public boolean updateUser(UserModel user);
	
	public List<CategoryModel> getAllCategory();
	
	public List<IssueModel> getIssueByCategory(CategoryModel catgegory, int page);
	
	public List<ImageModel> getImageByIssue(IssueModel issue);
	
	public void updateCategories(List<CategoryModel> list);
	
	public void updateIssues(List<IssueModel> list, CategoryModel cm);
	
	public void updateImages(List<ImageModel> list, IssueModel im);
}
