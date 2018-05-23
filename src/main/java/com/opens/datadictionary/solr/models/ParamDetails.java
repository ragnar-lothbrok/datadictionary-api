package com.opens.datadictionary.solr.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ParamDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;

	private String paramHttpType;

	private String description;

	private boolean required;

	private String paramjavaType;

	List<ParamDetails> postParamDetails = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getParamHttpType() {
		return paramHttpType;
	}

	public void setParamHttpType(String paramHttpType) {
		this.paramHttpType = paramHttpType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getParamjavaType() {
		return paramjavaType;
	}

	public void setParamjavaType(String paramjavaType) {
		this.paramjavaType = paramjavaType;
	}

	public List<ParamDetails> getPostParamDetails() {
		return postParamDetails;
	}

	public void setPostParamDetails(List<ParamDetails> postParamDetails) {
		this.postParamDetails = postParamDetails;
	}

}
