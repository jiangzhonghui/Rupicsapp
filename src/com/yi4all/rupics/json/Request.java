package com.yi4all.rupics.json;

/**
 * JSON格式请求的通用定义
 * 
 * @author tom
 *
 */
public class Request<T> {
	/**
	 * 登录成功后返回的身份令牌
	 */
	private String tokenid;
	
	/**
	 * 请求的数据
	 */
	private T data;

	public Request() {
		super();
	}

	public Request(String tokenid, T data) {
		super();
		this.tokenid = tokenid;
		this.data = data;
	}

	public String getTokenid() {
		return tokenid;
	}

	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Request [tokenid=" + tokenid + ", data=" + data + "]";
	}

}
