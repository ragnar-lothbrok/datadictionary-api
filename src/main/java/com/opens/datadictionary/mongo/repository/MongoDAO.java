package com.opens.datadictionary.mongo.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * 
 * @author raghunandan.gupta
 *
 */
@Service
public class MongoDAO {

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private GridFsTemplate gridFsTemplate;

	public void storeImage(InputStream inputStream, String fileName, Map<String, String> metaData) {
		try {
			gridFsTemplate.store(inputStream, fileName, metaData);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Exception occured while closing streams = {} ", e);
				}
			}
		}
	}

	public GridFsResource[] getSpaceImages(String spaceIdWildCard) {
		return gridFsTemplate.getResources(spaceIdWildCard);
	}

	public void insert(Object object) {
		mongoTemplate.insert(object);
	}

	public void insert(Object objectToSave, String collectionName) {
		mongoTemplate.insert(objectToSave, collectionName);
	}

	public void insert(Collection<? extends Object> batchToSave, Class<?> entityClass) {
		mongoTemplate.insert(batchToSave, entityClass);
	}

	public void insert(Collection<? extends Object> batchToSave, String collectionName) {
		mongoTemplate.insert(batchToSave, collectionName);
	}

	public void insertAll(Collection<? extends Object> objectsToSave) {
		mongoTemplate.insertAll(objectsToSave);
	}

	public long count(Class<?> entityClass) {
		return mongoTemplate.count(null, entityClass);
	}

	public <T> List<T> findAllByRegex(Map<String, String> params, Class<T> entityClass) {
		Query query = new Query();
		if (params == null || params.isEmpty()) {
			return null;
		} else {
			for (Entry<String, String> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).regex(entry.getValue(), "i"));
			}
		}
		return mongoTemplate.find(query, entityClass);
	}

	public <T> List<T> findLastN(Map<String, Object> params, String sortingField, int limit, Class<T> entityClass) {
		Query query = new Query();
		if (params == null || params.isEmpty()) {
			return null;
		} else {
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
			}
			query.with(new Sort(Sort.Direction.DESC, sortingField));
			query.limit(limit);
		}
		return mongoTemplate.find(query, entityClass);
	}

	public <T> T findOne(Map<String, Object> params, Class<T> entityClass) {
		Query query = new Query();
		if (params == null || params.isEmpty()) {
			return null;
		} else {
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
			}
		}
		return mongoTemplate.findOne(query, entityClass);
	}

	public <T> boolean deleteOne(Map<String, Object> params, Class<T> entityClass) {
		Query query = new Query();
		if (params == null || params.isEmpty()) {
			return false;
		} else {
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
			}
		}
		mongoTemplate.remove(query, entityClass);
		return true;
	}

	public <T> T findOne(Query query, Class<T> entityClass) {
		return mongoTemplate.findOne(query, entityClass);
	}

	public <T> void upsert(Map<String, Object> params, Map<String, Object> updateParams, Class<T> entityClass) {
		Query query = new Query();
		Update update = new Update();
		if (params == null || params.isEmpty()) {
			return;
		} else {
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
			}
			for (Entry<String, Object> entryUpdate : updateParams.entrySet()) {
				update.set(entryUpdate.getKey(), entryUpdate.getValue());
			}
		}
		mongoTemplate.upsert(query, update, entityClass);
	}

	public <T> void save(T entityToSave) {
		if (entityToSave == null) {
			return;
		} else {
			mongoTemplate.save(entityToSave);
		}
	}

	public <T> void save(T entityToSave, String collectionName) {
		if (entityToSave == null) {
			return;
		} else {
			mongoTemplate.save(entityToSave, collectionName);
		}
	}

	public <T> void save(List<T> entityList) {
		if (entityList == null || entityList.isEmpty()) {
			return;
		} else {
			for (T t : entityList) {
				mongoTemplate.save(t);
			}
		}
	}

	public <T> List<T> findAll(Map<String, Object> params, Class<T> entityClass) {
		Query query = new Query();
		if (params == null || params.isEmpty()) {
			return null;
		} else {
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).is(entry.getValue()));
			}
		}
		return mongoTemplate.find(query, entityClass);
	}

	public <T> List<T> findAll(Query query, Class<T> entityClass) {
		if (query == null) {
			return null;
		}
		return mongoTemplate.find(query, entityClass);
	}

	public <T> List<T> findAll(Class<T> entityClass) {
		return mongoTemplate.findAll(entityClass);
	}

	public <T, K> List<T> findAllInIds(Map<String, List<K>> params, Class<T> entityClass) {
		Query query = new Query();
		if (params == null || params.isEmpty()) {
			return null;
		} else {
			for (Entry<String, List<K>> entry : params.entrySet()) {
				query.addCriteria(Criteria.where(entry.getKey()).in(entry.getValue()));
			}
		}
		return mongoTemplate.find(query, entityClass);
	}

	public <T> T findAndModify(String collectionName, Class<T> entityClass, Integer increment) {
		Query query = new Query();
		if (collectionName == null || collectionName.isEmpty()) {
			return null;
		} else {
			if (increment == null || increment == 0) {
				increment = 1;
			}
			query.addCriteria(Criteria.where("name").is(collectionName));
			Update update = new Update();
			update.inc("seqId", increment);
			return mongoTemplate.findAndModify(query, update, entityClass);
		}
	}

	public <T> List<T> findAllBatchExists(String indexedColumn, Set<Object> indexedIds, Class<T> entityClass,
			String collectionName) {
		Query query = new Query().addCriteria(Criteria.where(indexedColumn).in(indexedIds));
		query.fields().include(indexedColumn);
		return mongoTemplate.find(query, entityClass);
	}

	public <T, E> List<T> findAll(String indexedColumn, Set<E> indexedIds, Class<T> entityClass, String collectionName,
			Map<String, Object> params, List<String> includedColumns) {
		Query query = new Query();
		if (indexedColumn != null && indexedIds.size() > 0) {
			query.addCriteria(Criteria.where(indexedColumn).in(indexedIds));
		}
		if (params != null && params.size() > 0) {
			for (Entry<String, Object> keyValuePair : params.entrySet()) {
				query.addCriteria(Criteria.where(keyValuePair.getKey()).in(keyValuePair.getValue()));
			}
		}
		if (includedColumns != null && includedColumns.size() > 0) {
			for (String column : includedColumns) {
				query.fields().include(column);
			}
		}
		return mongoTemplate.find(query, entityClass);
	}

	public String deleteSpaceImageFiles(String id, String clientId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(id)).addCriteria(Criteria.where("metadata.clientId").is(clientId));
		GridFSDBFile gridFSDBFile = gridFsTemplate.findOne(query);
		gridFsTemplate.delete(query);
		return gridFSDBFile.getFilename();
	}

}