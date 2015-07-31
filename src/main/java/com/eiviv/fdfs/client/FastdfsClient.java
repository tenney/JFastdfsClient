package com.eiviv.fdfs.client;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.eiviv.fdfs.client.AbstractClient;
import com.eiviv.fdfs.client.StorageClient;
import com.eiviv.fdfs.client.StorageClientFactory;
import com.eiviv.fdfs.client.TrackerClient;
import com.eiviv.fdfs.client.TrackerClientFactory;
import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.GroupInfo;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.model.StorageInfo;
import com.eiviv.fdfs.model.UploadStorage;

public class FastdfsClient extends AbstractClient {
	
	private GenericKeyedObjectPool<String, TrackerClient> trackerClientPool;
	private GenericKeyedObjectPool<String, StorageClient> storageClientPool;
	private List<String> trackerAddrs = new ArrayList<String>();
	private Map<String, String> storageIpMap = new ConcurrentHashMap<String, String>();
	
	public FastdfsClient(List<String> trackerAddrs) throws Exception {
		this.trackerAddrs = trackerAddrs;
		this.trackerClientPool = new GenericKeyedObjectPool<String, TrackerClient>(new TrackerClientFactory());
		this.storageClientPool = new GenericKeyedObjectPool<String, StorageClient>(new StorageClientFactory());
		
		updateStorageIpMap();
	}
	
	public FastdfsClient(	List<String> trackerAddrs,
							GenericKeyedObjectPool<String, TrackerClient> trackerClientPool,
							GenericKeyedObjectPool<String, StorageClient> storageClientPool) {
		this.trackerAddrs = trackerAddrs;
		this.trackerClientPool = trackerClientPool;
		this.storageClientPool = storageClientPool;
	}
	
	public String upload(File file, String ext, Map<String, String> meta) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		String fileId = null;
		
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<UploadStorage> result = trackerClient.getUploadStorage();
			
			if (result.getCode() != Context.SUCCESS_CODE) {
				return fileId;
			}
			
			storageAddr = result.getData().getAddress();
			storageClient = storageClientPool.borrowObject(storageAddr);
			
			String extname = ext;
			
			if (ext == null) {
				extname = getFileExtName(file);
			}
			
			Result<String> result2 = storageClient.upload(file, extname, result.getData().getPathIndex());
			
			if (result2.getCode() != Context.SUCCESS_CODE) {
				return fileId;
			}
			
			fileId = result2.getData();
			
			if (meta != null) {
				setMeta(fileId, meta);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageClient != null) {
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return fileId;
	}
	
	public String uploadSlave(File file, String fileid, String prefix, String ext) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		String fileId = null;
		
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			
			if (fileid == null) {
				return fileId;
			}
			
			String[] tupple = splitFileId(fileid);
			String groupname = tupple[0];
			String filename = tupple[1];
			
			Result<String> result = trackerClient.getUpdateStorageAddr(groupname, filename);
			
			if (result.getCode() != Context.SUCCESS_CODE) {
				return fileId;
			}
			
			storageAddr = result.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<String> result2 = storageClient.uploadSlave(file, filename, prefix, ext, null);
			
			if (result2.getCode() == Context.SUCCESS_CODE) {
				fileId = result2.getData();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageClient != null) {
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return fileId;
	}
	
	public Boolean setMeta(String fileId, Map<String, String> meta) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		boolean result = false;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (result2.getCode() != Context.SUCCESS_CODE) {
				return result;
			}
			
			storageAddr = result2.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Boolean> result3 = storageClient.setMeta(fastDfsFile.group, fastDfsFile.fileName, meta);
			
			result = result3.getData();
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageClient != null) {
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return result;
	}
	
	public Map<String, String> getMeta(String fileId) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Map<String, String> meta = null;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (result2.getCode() != Context.SUCCESS_CODE) {
				return meta;
			}
			
			storageAddr = result2.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Map<String, String>> result3 = storageClient.getMeta(fastDfsFile.group, fastDfsFile.fileName);
			
			if (result3.getCode() == Context.SUCCESS_CODE) {
				meta = result3.getData();
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageClient != null) {
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return meta;
	}
	
	public String getUrl(String fileId) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		String url = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result = trackerClient.getDownloadStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			if (result.getCode() == Context.SUCCESS_CODE) {
				String hostPort = getDownloadHostPort(result.getData());
				url = "http://" + hostPort + "/" + fastDfsFile.fileName;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return url;
	}
	
	public String upload(File file) throws Exception {
		String fileName = file.getName();
		return upload(file, fileName);
	}
	
	public String upload(File file, String fileName) throws Exception {
		return upload(file, fileName, null);
	}
	
	public Boolean delete(String fileId) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		boolean result = false;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (result2.getCode() != Context.SUCCESS_CODE) {
				return result;
			}
			
			storageAddr = result2.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Boolean> result3 = storageClient.delete(fastDfsFile.group, fastDfsFile.fileName);
			
			result = result3.getData();
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageClient != null) {
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return result;
	}
	
	public Boolean download(String fileId, OutputStream os) throws Exception {
		return download(fileId, os, 0);
	}
	
	public Boolean download(String fileId, OutputStream os, long offset) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		boolean result = false;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> result2 = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (result2.getCode() != Context.SUCCESS_CODE) {
				return result;
			}
			
			storageAddr = result2.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Boolean> result3 = storageClient.download(fastDfsFile.group, fastDfsFile.fileName, os, offset);
			
			result = result3.getData();
		} catch (Exception e) {
			throw e;
		} finally {
			if (storageClient != null) {
				storageClientPool.returnObject(storageAddr, storageClient);
			}
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return result;
	}
	
	public void close() {
		this.trackerClientPool.close();
		this.storageClientPool.close();
	}
	
	private String getTrackerAddr() {
		Random r = new Random();
		int i = r.nextInt(trackerAddrs.size());
		
		return trackerAddrs.get(i);
	}
	
	private void updateStorageIpMap() throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<List<GroupInfo>> result = trackerClient.getGroupInfos();
			
			if (result.getCode() != Context.SUCCESS_CODE) {
				throw new Exception("Get getGroupInfos Error");
			}
			
			List<GroupInfo> groupInfos = result.getData();
			
			for (GroupInfo groupInfo : groupInfos) {
				Result<List<StorageInfo>> result2 = trackerClient.getStorageInfos(groupInfo.getGroupName());
				
				if (result2.getCode() != 0) {
					continue;
				}
				
				List<StorageInfo> storageInfos = result2.getData();
				
				for (StorageInfo storageInfo : storageInfos) {
					String hostPort = storageInfo.getDomainName();
					
					if (storageInfo.getStorageHttpPort() != 80) {
						hostPort = hostPort + ":" + storageInfo.getStorageHttpPort();
					}
					
					storageIpMap.put(storageInfo.getIpAddr() + ":" + storageInfo.getStoragePort(), hostPort);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
	}
	
	private String getDownloadHostPort(String storageAddr) throws Exception {
		String downloadHostPort = storageIpMap.get(storageAddr);
		
		if (downloadHostPort == null) {
			updateStorageIpMap();
			downloadHostPort = storageIpMap.get(storageAddr);
		}
		
		return downloadHostPort;
	}
	
	private String getFileExtName(File file) {
		String name = file.getName();
		
		if (name != null) {
			int i = name.lastIndexOf('.');
			
			if (i > -1) {
				return name.substring(i + 1);
			}
		}
		
		return null;
	}
	
	private static final class FastDfsFile {
		
		private String group;
		private String fileName;
		
		public FastDfsFile(String fileId) {
			int pos = fileId.indexOf("/");
			group = fileId.substring(0, pos);
			fileName = fileId.substring(pos + 1);
		}
	}
	
}
