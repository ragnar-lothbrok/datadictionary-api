package com.opens.datadictionary.solr.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ApiResource implements Serializable, Cloneable {

	private static final long serialVersionUID = 7283599013921879783L;
	private String resourceUrl;
	private String methodName;
	private List<String> tags;
	private String summary;
	private String description;
	private String operationId;

	@JsonIgnore
	private List<String> responseDef = new ArrayList<>();

	@JsonIgnore
	private List<ParamDetails> paramDetails = new ArrayList<ParamDetails>();

	public ApiResource() {

	}

	public ApiResource(String resourceUrl, String methodName, List<String> tags, String summary, String description,
			String operationId) {
		super();
		this.resourceUrl = resourceUrl;
		this.methodName = methodName;
		this.tags = tags;
		this.summary = summary;
		this.description = description;
		this.operationId = operationId;
	}

	@Override
	public ApiResource clone() {
		ApiResource apiResource = new ApiResource(resourceUrl, methodName, tags, summary, description, operationId);
		apiResource.setParamDetails(null);
		return apiResource;
	}

	public List<String> getResponseDef() {
		return responseDef;
	}

	public void setResponseDef(List<String> responseDef) {
		this.responseDef = responseDef;
	}

	public List<ParamDetails> getParamDetails() {
		return paramDetails;
	}

	public void setParamDetails(List<ParamDetails> paramDetails) {
		this.paramDetails = paramDetails;
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
