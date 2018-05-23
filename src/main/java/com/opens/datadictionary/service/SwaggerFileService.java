package com.opens.datadictionary.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface SwaggerFileService {

	public Resource loadFile(String filename);

	void store(MultipartFile file);
}
