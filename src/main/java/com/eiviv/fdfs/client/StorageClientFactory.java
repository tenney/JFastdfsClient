package com.eiviv.fdfs.client;

import java.io.IOException;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.eiviv.fdfs.config.FastdfsClientConfig;
import com.eiviv.fdfs.exception.FastdfsClientException;

public class StorageClientFactory implements KeyedPooledObjectFactory<String, StorageClient> {
	
	private Integer connectTimeout = FastdfsClientConfig.DEFAULT_CONNECT_TIMEOUT * 1000;
	private Integer networkTimeout = FastdfsClientConfig.DEFAULT_NETWORK_TIMEOUT * 1000;
	
	public StorageClientFactory() {
	}
	
	/**
	 * 实例化
	 * 
	 * @param connectTimeout 连接超时时间(秒)
	 * @param networkTimeout 传输超时时间(秒)
	 */
	public StorageClientFactory(Integer connectTimeout, Integer networkTimeout) {
		this.connectTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
	}
	
	@Override
	public PooledObject<StorageClient> makeObject(String key) {
		StorageClient storageClient = new StorageClient(key, connectTimeout, networkTimeout);
		PooledObject<StorageClient> pooledStorageClient = new DefaultPooledObject<StorageClient>(storageClient);
		return pooledStorageClient;
	}
	
	@Override
	public void destroyObject(String key, PooledObject<StorageClient> pooledStorageClient) throws IOException {
		StorageClient storageClient = pooledStorageClient.getObject();
		try {
			storageClient.close();
		} catch (FastdfsClientException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean validateObject(String key, PooledObject<StorageClient> p) {
		StorageClient storageClient = p.getObject();
		
		if (storageClient.isClosed()) {
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public void activateObject(String key, PooledObject<StorageClient> p) throws Exception {
	}
	
	@Override
	public void passivateObject(String key, PooledObject<StorageClient> p) throws Exception {
	}
	
}
