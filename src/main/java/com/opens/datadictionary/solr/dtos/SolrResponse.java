package com.opens.datadictionary.solr.dtos;

import java.io.Serializable;

public class SolrResponse implements Serializable {

	private static final long serialVersionUID = 2880043279494007377L;
	
	private String id;
	private String fileName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public SolrResponse(String id, String fileName) {
		super();
		this.id = id;
		this.fileName = fileName;
	}

}
