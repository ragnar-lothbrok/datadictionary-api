package com.opens.datadictionary.config.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

	public Resource loadFile(String filename);

	void store(MultipartFile file);
}
