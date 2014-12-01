package com.unkur.affnetui.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table(name = "tbl_shops")
public class Shop {
	

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id = 0;
	
	@Column(name = "name")
	private String name = null;
	
	@Column(name = "url")
	private String url = null;
	
	@OneToMany
	List<Notification> notifications = null;
	
	public Shop(){}
	
	/**
	 * Public constructor
	 * @param name
	 * @param url
	 */
	public Shop(String name, String url) {
		this.name = name;
		this.url = url;
		this.notifications = new ArrayList<Notification>();
	}
	
	public void addNotification(Notification n) {
		this.notifications.add(n);
	}

	public int getId() {
		return id;
	}

	public void setId(int dbId) {
		this.id = dbId;
	}

	public String getName() {
		return name;
	}
	
	public String getUrl() {
		return url;
	}

	public void setName(String name) {
		if(name != null && !name.isEmpty()) {
			this.name = name;
		}
	}

	public void setUrl(String url) {
		if(url != null && !url.isEmpty()) {
			this.url = url;
		}
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	public List<Notification> getNotifications() {
		return notifications;
	}

	public void setNotifications(List<Notification> notifications) {
		this.notifications = notifications;
	}
	
	

}
