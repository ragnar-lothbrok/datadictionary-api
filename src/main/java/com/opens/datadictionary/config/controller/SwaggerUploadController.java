package com.opens.datadictionary.config.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opens.datadictionary.config.service.StorageService;

@RestController
@RequestMapping(com.opens.datadictionary.config.constants.APIEndpoints.UPLOAD_BASE_URL)
public class SwaggerUploadController {

	private final static Logger LOGGER = LoggerFactory.getLogger(SwaggerUploadController.class);

	@Autowired
	private StorageService storageService;

	@RequestMapping(method = RequestMethod.POST)
	public void uploadSwagger(@RequestParam("file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		LOGGER.info("Received swagger files.");
		storageService.store(file);
	}

}
