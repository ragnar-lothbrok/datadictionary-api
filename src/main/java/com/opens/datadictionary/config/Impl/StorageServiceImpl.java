package com.opens.datadictionary.config.Impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opens.datadictionary.config.exceptions.GenericException;
import com.opens.datadictionary.config.service.StorageService;
import com.opens.datadictionary.mongo.repository.CommonService;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

@Service
public class StorageServiceImpl implements StorageService {

	Logger log = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private CommonService commonService;

	private final Path rootLocation = Paths.get("files");

	@Autowired
	private SwaggerParser swaggerParser;

	@Override
	public void store(MultipartFile file) {
		try {
			if (file != null) {
				String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
				Files.copy(file.getInputStream(), this.rootLocation.resolve(fileName));
				// Saving to mongo database
				Map<String, String> metaData = new HashMap<String, String>();
				metaData.put("fileName", fileName);
				
				Swagger swagger = validateValidSwaggerFile(this.rootLocation.resolve(fileName).toAbsolutePath().toString());
				
				commonService.storeImage(file.getInputStream(), fileName, metaData);
				log.info("File stored successfully = {} File name = {} ", metaData, fileName);
			}
		} catch (Exception e) {
			throw new GenericException("FAILED TO LOAD swagger file.");
		}

	}

	
	public Swagger validateValidSwaggerFile(String location){
		return swaggerParser.read(location);
	}
	public String getSwaggerDataFromFile(InputStream input) {
		StringBuilder fileContent = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line);
			}
		} catch (Exception e) {
			// TODO
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// TODO
				}
			}
		}
		return fileContent.toString();
	}

	public Resource loadFile(String filename) {
		try {
			Path file = rootLocation.resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new GenericException("FAILED TO LOAD IMAGES.");
			}
		} catch (MalformedURLException e) {
			throw new GenericException("FAILED TO LOAD IMAGES.");
		}
	}

	@PostConstruct
	public void init() {
		try {
			if (!Files.exists(rootLocation)) {
				Files.createDirectory(rootLocation);
			}
		} catch (IOException e) {
			log.error("Directory already exists = {} ", e);
		}
	}

}
