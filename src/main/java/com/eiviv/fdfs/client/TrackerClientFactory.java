package com.eiviv.fdfs.client;

import java.io.IOException;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.eiviv.fdfs.config.FastdfsClientConfig;

public class TrackerClientFactory implements KeyedPooledObjectFactory<String, TrackerClient> {
	
	private Integer connectTimeout = FastdfsClientConfig.DEFAULT_CONNECT_TIMEOUT * 1000;
	private Integer networkTimeout = FastdfsClientConfig.DEFAULT_NETWORK_TIMEOUT * 1000;
	
	public TrackerClientFactory() {
	}
	
	/**
	 * 实例化
	 * 
	 * @param connectTimeout 连接超时时间 (秒)
	 * @param networkTimeout 网络超时时间(秒)
	 */
	public TrackerClientFactory(Integer connectTimeout, Integer networkTimeout) {
		this.connectTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
	}
	
	@Override
	public PooledObject<TrackerClient> makeObject(String key) {
		TrackerClient trackerClient = new TrackerClient(key, connectTimeout, networkTimeout);
		PooledObject<TrackerClient> pooledTrackerClient = new DefaultPooledObject<TrackerClient>(trackerClient);
		return pooledTrackerClient;
	}
	
	@Override
	public void destroyObject(String key, PooledObject<TrackerClient> pooledTrackerClient) throws IOException {
		TrackerClient trackerClient = pooledTrackerClient.getObject();
		trackerClient.close();
	}
	
	@Override
	public boolean validateObject(String key, PooledObject<TrackerClient> p) {
		return true;
	}
	
	@Override
	public void activateObject(String key, PooledObject<TrackerClient> p) throws Exception {
	}
	
	@Override
	public void passivateObject(String key, PooledObject<TrackerClient> p) throws Exception {
	}
	
}
