package com.yi4all.rupics.db;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@JsonIgnoreProperties(ignoreUnknown = true)
@DatabaseTable(tableName = "USER")
public class UserModel implements Serializable {

	public static final String TAG = "UserModel";
	
	public static final String FIELD_EMAIL = "EMAIL";
	public final static String FIELD_PASSWORD = "PASSWORD";
	public final static String FIELD_SID = "DEVICE_ID";
	public final static String FIELD_VIP = "VIP";
	public final static String FIELD_ENDDATE = "ENDDATE";

	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(columnName = FIELD_EMAIL)
	private String email;
	@DatabaseField(columnName = FIELD_PASSWORD)
	private String password;
	@DatabaseField(columnName = FIELD_SID)
	private String deviceId;
	
	private int limitKBytes; 
	
	private Date validEndDate;
	
	private String tokenid;
    
	private Long tokenExpirationTime;

	public UserModel() {

	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String sid) {
		this.deviceId = sid;
	}

	public int getLimitKBytes() {
		return limitKBytes;
	}

	public void setLimitKBytes(int limitKBytes) {
		this.limitKBytes = limitKBytes;
	}

	public Date getValidEndDate() {
		return validEndDate;
	}

	public void setValidEndDate(Date validEndDate) {
		this.validEndDate = validEndDate;
	}

	public String getTokenid() {
		return tokenid;
	}

	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}

	public Long getTokenExpirationTime() {
		return tokenExpirationTime;
	}

	public void setTokenExpirationTime(Long tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

}
