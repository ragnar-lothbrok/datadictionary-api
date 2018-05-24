package com.opens.datadictionary.solr.dtos;

import java.io.Serializable;

public class FieldFilter implements Serializable {

	private static final long serialVersionUID = -6190136872550562224L;

	private String filter;
	private String filterValue;

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

}
