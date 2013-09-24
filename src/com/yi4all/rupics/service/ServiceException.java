package com.yi4all.rupics.service;

import java.io.IOException;

import org.restlet.resource.ResourceException;

public class ServiceException extends Exception{

	//Remote access error
	public final static int ERROR_CODE_NO_CONNECTION = 1001;
	public final static int ERROR_CODE_SERVER_NO_RESPONSE = 1002;
	public final static int ERROR_CODE_REMOTE_ERROR = 1003;
	public final static int ERROR_CODE_IO_ERROR = 1004;
	public final static int ERROR_CODE_UNKNOWN_ERROR = 1;
	public final static int ERROR_CODE_UNAVAILABLE_TOKENID = 2;//会话过期,请重新登录
	public final static int ERROR_CODE_LOGIN_ERROR = 3;//帐号密码错误,请重新登录
	public final static int ERROR_CODE_NO_DATA_ERROR = 5;//没有数据
	
	//DB access error
	public final static int ERROR_CODE_DB_EXCEPTION = 1101;
	
	//Business logic status code
	public final static int BUSINESS_CODE_NO_CONDITION = 2001;
	public final static int BUSINESS_CODE_COURSE_CONFLICTION = 6;//添加食材相克
	
	private int errorCode;
	private String message;
	
	public ServiceException(Exception e){
		if(e instanceof ResourceException){
			this.errorCode = ((ResourceException) e).getStatus().getCode();
			this.message = "网络异常,请稍后再试";
		}else if( e instanceof IOException){
			this.errorCode = ERROR_CODE_IO_ERROR;
			this.message = "访问网络异常,请稍后再试";
		}else{
			this.errorCode = ERROR_CODE_UNKNOWN_ERROR;
			this.message = e.getMessage();
		}
	}
	
	public ServiceException(String msg) {
		this.errorCode = ERROR_CODE_UNKNOWN_ERROR;
		this.message = msg;
	}
	
	public ServiceException(int erroCode, String msg) {
		this.errorCode = erroCode;
		this.message = msg;
	}

	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
