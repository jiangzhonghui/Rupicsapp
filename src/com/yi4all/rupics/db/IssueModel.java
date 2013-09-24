package com.yi4all.rupics.db;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "issue")
public class IssueModel implements Serializable{

	public  static final String URL = "URL";
	public  static final String NAME = "NAME";
	public  static final String IMG_URL = "IMG_URL";
	public  static final String FAVORITE = "FAVORITE";
	public  static final String CATEGORY = "CATEGORY";
	public  static final String IMAGE_AMOUNT = "IMAGE_AMOUNT";
	public  static final String FIELD_SERVERID = "SERVERID";
	public  static final String COVER = "COVER";
	public  static final String CREATED_AT = "CREATED_AT";
	
	@DatabaseField(generatedId = true)
	private long id = -1;
	@DatabaseField(columnName = FIELD_SERVERID)
	private Long serverId;// ID of server
	@DatabaseField(index = true, columnName = NAME)
	private String name;
	@DatabaseField( columnName = FAVORITE)
	private boolean isFavorite; // 0 - false ; 1 - true
	@DatabaseField(foreignAutoRefresh = true, foreign = true, columnName = CATEGORY)
	private CategoryModel category; 
	@DatabaseField(columnName = IMAGE_AMOUNT)
	private int imageAmount = 0;
	@DatabaseField(columnName = COVER)
	private String cover;//image path
	@DatabaseField(columnName = CREATED_AT)
	private Date createdAt;
	
	public IssueModel(){
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getImageAmount() {
		return imageAmount;
	}
	public void setImageAmount(int imageAmount) {
		this.imageAmount = imageAmount;
	}
	public boolean isFavorite() {
		return isFavorite;
	}
	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}
	
	public CategoryModel getCategory() {
		return category;
	}

	public void setCategory(CategoryModel category) {
		this.category = category;
	}


	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	
}
