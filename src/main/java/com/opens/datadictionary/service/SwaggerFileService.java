package com.opens.datadictionary.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.opens.datadictionary.solr.dtos.SearchRequest;

public interface SwaggerFileService {

	public Resource loadFile(String filename);

	void store(MultipartFile file);

	List<String> search(SearchRequest searchRequest);
	
	String searchOneDoc(SearchRequest searchRequest);
}
