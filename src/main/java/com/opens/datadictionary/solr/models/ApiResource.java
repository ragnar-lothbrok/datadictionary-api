package com.opens.datadictionary.solr.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ApiResource implements Serializable {

	private static final long serialVersionUID = 1L;

	private String resourceUrl;
	private String methodName;
	private List<String> tags;
	private String summary;
	private String description;
	private String operationId;

	private List<ParamDetails> params = new ArrayList<>();

	public List<ParamDetails> getParams() {
		return params;
	}

	public void setParams(List<ParamDetails> params) {
		this.params = params;
	}

	public String getResourceUrl() {
		return resourceUrl;
	}

	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getOperationId() {
		return operationId;
	}

	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

}
