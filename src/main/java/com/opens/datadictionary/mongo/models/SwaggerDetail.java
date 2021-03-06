package com.opens.datadictionary.mongo.models;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "swaggerdetail")
public class SwaggerDetail implements Serializable {

	public SwaggerDetail(String id, String fileName, String content, String uploadDate) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.content = content;
		this.uploadDate = uploadDate;
	}

	private static final long serialVersionUID = 9148030041953822710L;

	private String id;

	private String fileName;

	private String content;

	private String uploadDate;

	public String getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}

	public SwaggerDetail() {

	}

	public SwaggerDetail(String id, String fileName, String content) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.content = content;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
