package com.unkur.affnetui.entity;

import javax.persistence.*;

@Entity
@Table(name = "tbl_notifications")
public class Notification {

	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id = 0;
	
	@Column(name = "title")
	private String title = null;
	
	@Column(name = "text")
	private String text = null;
	
	@Column(name = "time")
	private long time = 0;
	
	@Column(name = "is_new")
	private boolean isNew = true;
	
	@Column(name = "shop_id")
	private int shopId = 0;

	public Notification(String title, String text, long time, boolean isNew, int shopId) {
		this.title = title;
		this.text = text;
		this.time = time;
		this.isNew = isNew;
		this.shopId = shopId;
	}

	public Notification() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public int getShopId() {
		return shopId;
	}

	public void setShopId(int shopId) {
		this.shopId = shopId;
	}
	
	
	
	
	
}
