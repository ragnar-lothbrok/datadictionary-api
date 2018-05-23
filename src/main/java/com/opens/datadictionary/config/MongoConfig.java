package com.opens.datadictionary.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.google.common.collect.ImmutableList;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

@Configuration
@EnableMongoRepositories(basePackages = { "com.opens.datadictionary" })
public class MongoConfig extends AbstractMongoConfiguration {

	@Value("${mongo.db.name:taskmanager}")
	private String dbName;

	@Value("${mongo.db.host:localhost}")
	private String dbHost;

	@Value("${mongo.db.port:27017}")
	private String dbPort;

	@Value("${mongo.db.username:}")
	private String dbUsername;

	@Value("${mongo.db.password:}")
	private String dbPassword;

	Logger logger = LoggerFactory.getLogger(MongoConfig.class);

	@Override
	protected String getDatabaseName() {
		return dbName;
	}

	@Override
	@Bean(name = "mongo")
	public Mongo mongo() {
		try {
			if(dbUsername == null || dbUsername.trim().length() == 0) {
				return new MongoClient(dbHost + ":" + dbPort);
			}
			return new MongoClient(new ServerAddress(dbHost, Integer.parseInt(dbPort)),
					ImmutableList.of(MongoCredential.createCredential(dbUsername, dbName, dbPassword.toCharArray())),
					MongoClientOptions.builder().socketTimeout(3000).socketKeepAlive(true).minHeartbeatFrequency(25)
							.heartbeatSocketTimeout(3000).build());
		} catch (NumberFormatException e) {
			logger.error("Couldn't connect mongo db host. Exception [" + e.getMessage() + "]");
		} catch (Exception e) {
			logger.error("Couldn't connect mongo db host. Exception [" + e.getMessage() + "]");
			return new MongoClient(dbHost + ":" + dbPort);
		}
		System.exit(-1);
		return null;
	}

	public MongoOperations mongoTemplate(@Qualifier("mongo") Mongo mongo) {
		return new MongoTemplate(mongo, getDatabaseName());
	}

	@Bean
	public GridFsTemplate gridFsTemplate() throws Exception {
		return new GridFsTemplate(mongoDbFactory(), mappingMongoConverter());
	}
}