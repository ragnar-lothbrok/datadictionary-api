package com.opens.datadictionary.Impl;

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

import com.opens.datadictionary.exceptions.GenericException;
import com.opens.datadictionary.exceptions.InvalidSwaggerFileException;
import com.opens.datadictionary.mongo.repository.CommonService;
import com.opens.datadictionary.service.SolrIndexService;
import com.opens.datadictionary.service.SwaggerFileService;
import com.opens.datadictionary.solr.models.SolrDocumentDto;

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;

@Service
public class SwaggerFileServiceImpl implements SwaggerFileService {

	private static final Logger logger = LoggerFactory.getLogger(SwaggerFileServiceImpl.class);

	@Autowired
	private CommonService commonService;

	private final Path rootLocation = Paths.get("files");

	@Autowired
	private SwaggerParser swaggerParser;

	@Autowired
	private SolrIndexService solrIndexService;

	@Override
	public void store(MultipartFile file) {
		try {
			if (file != null) {
				String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
				Files.copy(file.getInputStream(), this.rootLocation.resolve(fileName));

				Swagger swagger = validateValidSwaggerFile(getSwaggerDataFromFile(file.getInputStream()));

				SolrDocumentDto solrDocumentDto = solrIndexService.transform(swagger);

				// Saving to mongo database
				Map<String, String> metaData = new HashMap<String, String>();
				metaData.put("fileName", fileName);

				// Saving file to mongo database
				commonService.storeImage(file.getInputStream(), fileName, metaData);
				logger.info("File stored successfully = {} File name = {} ", metaData, fileName);
			}
		} catch (Exception e) {
			throw new GenericException("FAILED TO LOAD swagger file.");
		}

	}

	public Swagger validateValidSwaggerFile(String content) {
		try {
			return swaggerParser.parse(content);
		} catch (Exception e) {
			logger.error("Exception occured while parsing swagger file content = {} ", e);
			throw new InvalidSwaggerFileException("Invalid swagger file");
		}
	}

	/**
	 * This method will read swagger file content and print it in logs
	 * 
	 * @param input
	 * @return
	 */
	public String getSwaggerDataFromFile(InputStream input) {
		StringBuilder fileContent = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(input));
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line);
			}
			logger.info("Swagger file content = {} ", fileContent.toString());
		} catch (Exception e) {
			logger.error("Exception occured while reading file = {} ", e);
			throw new InvalidSwaggerFileException("Invalid swagger file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error("Exception occured while closing file = {} ", e);
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
				throw new GenericException("FAILED TO LOAD SWAGGER FILE.");
			}
		} catch (MalformedURLException e) {
			throw new GenericException("FAILED TO LOAD SWAGGER FILE.");
		}
	}

	@PostConstruct
	public void init() {
		try {
			if (!Files.exists(rootLocation)) {
				Files.createDirectory(rootLocation);
			}
		} catch (IOException e) {
			logger.error("Directory already exists = {} ", e);
		}
	}

}
