package com.unkur.affnetui.entity;

import javax.persistence.*;

@Entity
@Table(name = "tbl_supported_file_formats")
public class SupportedFileFormat {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id = 0;
	
	@Column(name = "name")
	private String name = null;
	
	@Column(name = "extension", length = 4)
	private String extension = null;

	
	public SupportedFileFormat() {}

	public SupportedFileFormat(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
	
}
