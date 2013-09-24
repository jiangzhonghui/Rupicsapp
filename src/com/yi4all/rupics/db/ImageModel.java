package com.yi4all.rupics.db;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "image")
public class ImageModel implements Serializable{

public final static String LOGTAG = "ImageModel";
	
	public  static final String URL = "URL";
	
	public  static final String ISSUE = "ISSUE";
	
	public  static final String ORDER = "ORDER";
	
	@DatabaseField(generatedId = true)
	private long id;

	@DatabaseField(index = true, columnName = URL) 
	private String url;
	
	@DatabaseField(columnName = ORDER)
	private int issueOrder;
	
	@DatabaseField(foreignAutoRefresh = true, foreign = true, columnName = ISSUE)
	private IssueModel issue; // 

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public IssueModel getIssue() {
		return issue;
	}

	public void setIssue(IssueModel issue) {
		this.issue = issue;
	}

	public int getIssueOrder() {
		return issueOrder;
	}

	public void setIssueOrder(int issueOrder) {
		this.issueOrder = issueOrder;
	}

	
}
