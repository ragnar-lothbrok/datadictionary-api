package com.opens.datadictionary.solr.models;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private Set<String> responseDef = new HashSet<>();

	@JsonIgnore
	private Set<String> requestFields = new HashSet<>();

	@JsonIgnore
	private Set<String> headerFields = new HashSet<>();

	@JsonIgnore
	private Set<String> queryFields = new HashSet<>();

	@JsonIgnore
	private Set<String> pathFields = new HashSet<>();

	private String meataData;

	public ApiResource() {

	}

	public ApiResource(String resourceUrl, String methodName, List<String> tags, String summary, String description,
			String operationId, String meataData) {
		super();
		this.resourceUrl = resourceUrl;
		this.methodName = methodName;
		this.tags = tags;
		this.summary = summary;
		this.description = description;
		this.operationId = operationId;
		this.meataData = meataData;
	}

	@Override
	public ApiResource clone() {
		ApiResource apiResource = new ApiResource(resourceUrl, methodName, tags, summary, description, operationId,
				meataData);
		return apiResource;
	}

	public Set<String> getResponseDef() {
		return responseDef;
	}

	public Set<String> getRequestFields() {
		return requestFields;
	}

	public void setRequestFields(Set<String> requestFields) {
		this.requestFields = requestFields;
	}

	public Set<String> getHeaderFields() {
		return headerFields;
	}

	public void setHeaderFields(Set<String> headerFields) {
		this.headerFields = headerFields;
	}

	public void setResponseDef(Set<String> responseDef) {
		this.responseDef = responseDef;
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

	public Set<String> getQueryFields() {
		return queryFields;
	}

	public void setQueryFields(Set<String> queryFields) {
		this.queryFields = queryFields;
	}

	public Set<String> getPathFields() {
		return pathFields;
	}

	public void setPathFields(Set<String> pathFields) {
		this.pathFields = pathFields;
	}

	public String getMeataData() {
		return meataData;
	}

	public void setMeataData(String meataData) {
		this.meataData = meataData;
	}

}
