package com.yi4all.rupics.service;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yi4all.rupics.db.UserModel;
import com.yi4all.rupics.json.MessageModel;
import com.yi4all.rupics.json.RemoteInvokeModel;

public interface IRemoteService {
	
	public void setBaseUrl(String url);
	public String getBaseUrl();

	/**********User Module****************/
	
	public boolean isLogin();
	
	/**
	 * Login by device id
	 * @param sid android device id
	 * @return return user's model
	 */
	public UserModel loginDirect() throws ServiceException;
	
	/**
	 * Login by user's phone number or email
	 * @param phone user's phone number
	 * @param email user's e-mail address
	 * @param password user's password, it should be encrypted in future 
	 * @return if login is successful, return user's model, otherwise, null
	 */
	public UserModel login(String email, String password) throws ServiceException;
	
	/**
	 * Forget password business by phone or email 
	 * @param phone user's phone number
	 * @param email user's e-mail address
	 * @return if the business is successful, return true, otherwise, false
	 */
	public MessageModel<String> forgetPassword(String phone, String email) throws ServiceException;
	
	/**
	 * User register, take a model with email or phone number
	 * @param user UserModel
	 * @return if the business is successful, return true, otherwise, false
	 */
	public MessageModel<String> register(UserModel user) throws ServiceException;
	
	/**********User Module****************/
	
	public void setSid(String sid) ;
	public String getSid();
	
	/**********General Query*************/
	public <T> MessageModel<T> generalQuery(RemoteInvokeModel rim, TypeReference type) throws ServiceException;
	
	/**********Push data to server*************/
	public <T> MessageModel<T> pushData2Server(RemoteInvokeModel rim) throws ServiceException;
}
