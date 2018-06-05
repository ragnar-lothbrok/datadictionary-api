package com.opens.datadictionary.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.opens.datadictionary.exceptions.GenericException;
import com.opens.datadictionary.mongo.models.SwaggerDetail;
import com.opens.datadictionary.service.SwaggerFileService;

@RestController
@RequestMapping(com.opens.datadictionary.constants.APIEndpoints.UPLOAD_BASE_URL)
public class APISwaggerUploadController {

	private final static Logger LOGGER = LoggerFactory.getLogger(APISwaggerUploadController.class);

	@Autowired
	private SwaggerFileService swaggerService;

	@RequestMapping(value = "upload", method = RequestMethod.POST)
	public void uploadSwagger(@RequestParam("file") MultipartFile file, HttpServletRequest request,
			HttpServletResponse response) {
		LOGGER.info("Received swagger files.");
		swaggerService.store(file);
	}

	@RequestMapping(value = "uploadHistory", method = RequestMethod.GET)
	public List<SwaggerDetail> history(HttpServletRequest request, HttpServletResponse response) {
		LOGGER.info("Upload history request received.");
		return swaggerService.uploadHistory();
	}

	@RequestMapping(value = "download", method = RequestMethod.GET)
	public ResponseEntity<FileSystemResource> downloadFile(@RequestParam(value = "fileName") String fileName) {
		String path = swaggerService.getFilePath(fileName);
		File file = new File(path);
		if (file.exists()) {
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
					.contentType(MediaType.valueOf("application/pdf")).contentLength(file.length())
					.body(new FileSystemResource(file));
		}
		throw new GenericException("File not found.");
	}

}
