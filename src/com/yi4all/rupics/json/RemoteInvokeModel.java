package com.yi4all.rupics.json;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.yi4all.rupics.service.enumtype.RemoteInvokeStatus;
import com.yi4all.rupics.service.enumtype.RemoteRequestAction;

public class RemoteInvokeModel implements Serializable {

	private RemoteRequestAction action;// POST or GET
	private String url;// complete request url
	private String requestBody;// request body
	private String responseBody;// response body
	private RemoteInvokeStatus status;// invoke status
	private String message;// error message
	
	public RemoteInvokeModel(){
		
	}
	
	public RemoteRequestAction getAction() {
		return action;
	}
	public void setAction(RemoteRequestAction action) {
		this.action = action;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getRequestBody() {
		return requestBody;
	}
	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	public RemoteInvokeStatus getStatus() {
		return status;
	}
	public void setStatus(RemoteInvokeStatus status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
}
