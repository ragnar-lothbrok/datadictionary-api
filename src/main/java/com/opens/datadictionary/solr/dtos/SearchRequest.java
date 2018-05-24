package com.opens.datadictionary.solr.dtos;

import java.io.Serializable;
import java.util.List;

public class SearchRequest implements Serializable {

	private static final long serialVersionUID = 4757960866089546166L;

	private String searchText;

	private List<FieldFilter> filters;

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public List<FieldFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<FieldFilter> filters) {
		this.filters = filters;
	}

}
