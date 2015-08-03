package com.eiviv.fdfs.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

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
	
	/**
	 * 构造
	 * 
	 * @param trackerAddrs {trackerHost1:port, trackerHost2:port, ...}
	 * @throws Exception
	 */
	public FastdfsClient(List<String> trackerAddrs) throws Exception {
		this.trackerAddrs = trackerAddrs;
		this.trackerClientPool = new GenericKeyedObjectPool<String, TrackerClient>(new TrackerClientFactory());
		this.storageClientPool = new GenericKeyedObjectPool<String, StorageClient>(new StorageClientFactory());
		
		updateStorageIpMap();
	}
	
	/**
	 * 构造
	 * 
	 * @param trackerAddrs {trackerHost1:port, trackerHost2:port, ...}
	 * @param trackerClientPool trackerClient 连接池
	 * @param storageClientPool storageClient 连接池
	 */
	public FastdfsClient(	List<String> trackerAddrs,
							GenericKeyedObjectPool<String, TrackerClient> trackerClientPool,
							GenericKeyedObjectPool<String, StorageClient> storageClientPool) {
		this.trackerAddrs = trackerAddrs;
		this.trackerClientPool = trackerClientPool;
		this.storageClientPool = storageClientPool;
	}
	
	/**
	 * 文件上传
	 * 
	 * @param file 文件
	 * @param extName 文件扩展名
	 * @param meta 元信息
	 * @return fileId "group/remoteFileName"
	 * @throws Exception
	 */
	public String upload(File file, String extName, Map<String, String> meta) throws Exception {
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
			
			if (extName == null) {
				extName = getFileExtName(file);
			}
			
			Result<String> uploadResult = storageClient.upload(file, extName, result.getData().getPathIndex());
			
			if (uploadResult.getCode() != Context.SUCCESS_CODE) {
				return fileId;
			}
			
			fileId = uploadResult.getData();
			
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
	
	/**
	 * 上传文件
	 * 
	 * @param file 文件
	 * @param extName 扩展名
	 * @return
	 * @throws Exception
	 */
	public String upload(File file, String extName) throws Exception {
		return upload(file, extName, null);
	}
	
	/**
	 * 上传文件
	 * 
	 * @param file 文件
	 * @return
	 * @throws Exception
	 */
	public String upload(File file) throws Exception {
		String fileName = file.getName();
		return upload(file, fileName);
	}
	
	/**
	 * 上传文件副本
	 * 
	 * @param file 文件
	 * @param fileid 原文件ID "group/remoteFileName"
	 * @param prefix 副本文件名后缀
	 * @param extName 扩展名
	 * @return 副本fileId "group/remoteFileName"
	 * @throws Exception
	 */
	public String uploadSlave(File file, String fileid, String prefix, String extName) throws Exception {
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
			
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(groupname, filename);
			
			if (updateStorageResult.getCode() != Context.SUCCESS_CODE) {
				return fileId;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<String> updaloadSlave = storageClient.uploadSlave(file, filename, prefix, extName, null);
			
			if (updaloadSlave.getCode() == Context.SUCCESS_CODE) {
				fileId = updaloadSlave.getData();
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
	
	/**
	 * 设置元信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param meta 元信息
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean setMeta(String fileId, Map<String, String> meta) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		boolean result = false;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (updateStorageResult.getCode() != Context.SUCCESS_CODE) {
				return result;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Boolean> setMetaResult = storageClient.setMeta(fastDfsFile.group, fastDfsFile.fileName, meta);
			
			result = setMetaResult.getData();
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
	
	/**
	 * 获取元信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return 元信息
	 * @throws Exception
	 */
	public Map<String, String> getMeta(String fileId) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		Map<String, String> meta = null;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (updateStorageResult.getCode() != Context.SUCCESS_CODE) {
				return meta;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Map<String, String>> getMetaResult = storageClient.getMeta(fastDfsFile.group, fastDfsFile.fileName);
			
			if (getMetaResult.getCode() == Context.SUCCESS_CODE) {
				meta = getMetaResult.getData();
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
	
	/**
	 * 获取http下载地址
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return http下载地址
	 * @throws Exception
	 */
	public String getDownloadUrl(String fileId) throws Exception {
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
	
	/**
	 * 删除文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean delete(String fileId) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		boolean result = false;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (updateStorageResult.getCode() != Context.SUCCESS_CODE) {
				return result;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Boolean> deleteResult = storageClient.delete(fastDfsFile.group, fastDfsFile.fileName);
			
			result = deleteResult.getData();
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
	
	/**
	 * 断点下载
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param os OutputStream
	 * @param offset 开始点
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean download(String fileId, OutputStream os, long offset) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		boolean result = false;
		String storageAddr = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (updateStorageResult.getCode() != Context.SUCCESS_CODE) {
				return result;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			Result<Boolean> downloadResult = storageClient.download(fastDfsFile.group, fastDfsFile.fileName, os, offset);
			
			result = downloadResult.getData();
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
	
	/**
	 * 下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param os OutputStream
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean download(String fileId, OutputStream os) throws Exception {
		return download(fileId, os, 0);
	}
	
	/**
	 * 断点下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param localFile 本地文件
	 * @param offset 下载开始点
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean download(String fileId, File localFile, long offset) throws Exception {
		return download(fileId, new FileOutputStream(localFile), offset);
	}
	
	/**
	 * 下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param localFile 本地文件
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean download(String fileId, File localFile) throws Exception {
		return download(fileId, new FileOutputStream(localFile));
	}
	
	/**
	 * 断点下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param localFileName 本地文件名
	 * @param offset 下载开始点
	 * @return
	 * @throws Exception
	 */
	public Boolean downloadFile(String fileId, String localFileName, long offset) throws Exception {
		return download(fileId, new File(localFileName), offset);
	}
	
	/**
	 * 关闭 trackerClient 连接池 和 storageClient连接池
	 */
	public void close() {
		this.trackerClientPool.close();
		this.storageClientPool.close();
	}
	
	/**
	 * 获取随机 tracker server 链接地址
	 * 
	 * @return
	 */
	private String getTrackerAddr() {
		Random r = new Random();
		int i = r.nextInt(trackerAddrs.size());
		
		return trackerAddrs.get(i);
	}
	
	/**
	 * 更新 storage ip
	 * 
	 * @throws Exception
	 */
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
	
	/**
	 * 获取获取下载host
	 * 
	 * @param storageAddr storage 地址
	 * @return 下载host
	 * @throws Exception
	 */
	private String getDownloadHostPort(String storageAddr) throws Exception {
		String downloadHostPort = storageIpMap.get(storageAddr);
		
		if (downloadHostPort == null) {
			updateStorageIpMap();
			downloadHostPort = storageIpMap.get(storageAddr);
		}
		
		return downloadHostPort;
	}
	
	/**
	 * 根据文件,获取扩展名
	 * 
	 * @param file 文件
	 * @return 扩展名
	 */
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
	
	/**
	 * fileId 实体类
	 */
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
