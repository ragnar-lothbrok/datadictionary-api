package com.opens.datadictionary.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.opens.datadictionary.service.SwaggerFileService;
import com.opens.datadictionary.solr.dtos.SearchRequest;

@RestController
@RequestMapping(com.opens.datadictionary.constants.APIEndpoints.SEARCH_BASE_URL)
public class APISearchController {

	private final static Logger LOGGER = LoggerFactory.getLogger(APISearchController.class);

	@Autowired
	private SwaggerFileService swaggerFileService;

	@RequestMapping(method = RequestMethod.POST)
	public String searchAPI(@RequestBody SearchRequest searchRequest, HttpServletRequest request,
			HttpServletResponse response) {
		LOGGER.info("Received search request = {} ", searchRequest);
		return swaggerFileService.searchOneDoc(searchRequest);
	}

}
