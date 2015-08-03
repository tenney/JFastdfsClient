package com.eiviv.fdfs.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.FileInfo;
import com.eiviv.fdfs.model.GroupInfo;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.model.StorageInfo;
import com.eiviv.fdfs.model.UploadStorage;

public class FastdfsClient extends AbstractClient {
	
	private GenericKeyedObjectPool<String, TrackerClient> trackerClientPool;
	private GenericKeyedObjectPool<String, StorageClient> storageClientPool;
	private List<String> trackerAddrs = new ArrayList<String>();
	private Map<String, String> storageIpMap = new ConcurrentHashMap<String, String>();
	
	private static final class FastDfsFile {
		private String group;
		private String fileName;
		
		public FastDfsFile(String fileId) {
			int pos = fileId.indexOf("/");
			group = fileId.substring(0, pos);
			fileName = fileId.substring(pos + 1);
		}
	}
	
	private static interface StorageExecutor<T extends Serializable> {
		T exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception;
	}
	
	/**
	 * 实例化
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
	 * 实例化
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
	public String upload(File file, String extName, HashMap<String, String> meta) throws Exception {
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
	public String uploadSlave(final File file, String metaFileId, final String prefix, final String extName) throws Exception {
		return fixedStorageExec(metaFileId, new StorageExecutor<String>() {
			@Override
			public String exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<String> updaloadSlaveResult = storageClient.uploadSlave(file, fastDfsFile.fileName, prefix, extName, null);
				
				return updaloadSlaveResult.getData();
			}
		});
	}
	
	/**
	 * 断点上传
	 * 
	 * @param fileId
	 * @param fileByte
	 * @return
	 * @throws Exception
	 */
	public boolean appendFile(String fileId, final byte[] fileByte) throws Exception {
		Boolean result = fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Boolean exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<Boolean> appendResult = storageClient.append(fastDfsFile.fileName, fileByte);
				
				return appendResult.getData();
			}
		});
		
		return result == null ? false : result.booleanValue();
	}
	
	/**
	 * 删除文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return boolean
	 * @throws Exception
	 */
	public boolean delete(String fileId) throws Exception {
		Boolean result = fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Boolean exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<Boolean> deleteResult = storageClient.delete(fastDfsFile.group, fastDfsFile.fileName);
				
				return deleteResult.getData();
			}
		});
		
		return result == null ? false : result.booleanValue();
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
	public boolean download(String fileId, final OutputStream os, final long offset) throws Exception {
		Boolean result = fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Boolean exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<Boolean> downloadResult = storageClient.download(fastDfsFile.group, fastDfsFile.fileName, os, offset);
				
				return downloadResult.getData();
			}
		});
		
		return result == null ? false : result.booleanValue();
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
	 * 剪裁文件
	 * 
	 * @param fileId
	 * @param truncatedFileSize
	 * @return boolean
	 * @throws Exception
	 */
	public Boolean truncateFile(String fileId, final long truncatedFileSize) throws Exception {
		Boolean result = fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Boolean exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<Boolean> truncateResult = storageClient.truncate(fastDfsFile.fileName, truncatedFileSize);
				
				return truncateResult.getData();
			}
		});
		
		return result == null ? false : result.booleanValue();
	}
	
	/**
	 * 设置元信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param meta 元信息
	 * @return boolean
	 * @throws Exception
	 */
	public boolean setMeta(String fileId, final HashMap<String, String> meta) throws Exception {
		Boolean result = fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Boolean exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<Boolean> setMetaResult = storageClient.setMeta(fastDfsFile.group, fastDfsFile.fileName, meta);
				
				return setMetaResult.getData();
			}
		});
		
		return result == null ? false : result.booleanValue();
	}
	
	/**
	 * 获取元信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return 元信息
	 * @throws Exception
	 */
	public HashMap<String, String> getMeta(String fileId) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<HashMap<String, String>>() {
			@Override
			public HashMap<String, String> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<HashMap<String, String>> getMetaResult = storageClient.getMeta(fastDfsFile.group, fastDfsFile.fileName);
				
				return getMetaResult.getData();
			}
		});
	}
	
	/**
	 * 获取上传文件信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return
	 * @throws Exception
	 */
	public FileInfo getFileInfo(String fileId) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<FileInfo>() {
			@Override
			public FileInfo exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				Result<FileInfo> getFileInfoResult = storageClient.getFileInfo(fastDfsFile.group, fastDfsFile.fileName);
				
				return getFileInfoResult.getData();
			}
		});
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
	 * 获取所有的groupInfo
	 * 
	 * @param trackerAddr
	 * @return 获取所有的groupInfo
	 * @throws Exception
	 */
	public ArrayList<GroupInfo> getGroupInfos() throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		ArrayList<GroupInfo> groupInfos = null;
		
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			groupInfos = trackerClient.getGroupInfos().getData();
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return groupInfos;
	}
	
	/**
	 * 根据group名, 获取改组所有的storage信息
	 * 
	 * @param group 组名
	 * @return 获取改组所有的storage信息
	 * @throws Exception
	 */
	public ArrayList<StorageInfo> getStorageInfos(String group) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		ArrayList<StorageInfo> storageInfos = null;
		
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			storageInfos = trackerClient.getStorageInfos(group).getData();
		} catch (Exception e) {
			throw e;
		} finally {
			if (trackerClient != null) {
				trackerClientPool.returnObject(trackerAddr, trackerClient);
			}
		}
		
		return storageInfos;
	}
	
	/**
	 * 获取所有的 storage 信息
	 * 
	 * @return 所有的 storage 信息
	 * @throws Exception
	 */
	public Map<String, ArrayList<StorageInfo>> getAllStorageInfo() throws Exception {
		ArrayList<GroupInfo> groupInfos = getGroupInfos();
		Map<String, ArrayList<StorageInfo>> storageInfoMap = null;
		
		if (groupInfos == null || groupInfos.isEmpty()) {
			return storageInfoMap;
		}
		
		storageInfoMap = new HashMap<String, ArrayList<StorageInfo>>();
		
		ArrayList<StorageInfo> storageInfos = null;
		
		for (GroupInfo groupInfo : groupInfos) {
			storageInfos = getStorageInfos(groupInfo.getGroupName());
			storageInfoMap.put(groupInfo.getGroupName(), storageInfos);
		}
		
		return storageInfoMap;
	}
	
	/**
	 * 关闭 trackerClient 连接池 和 storageClient连接池
	 */
	public void close() {
		this.trackerClientPool.close();
		this.storageClientPool.close();
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
			Result<ArrayList<GroupInfo>> result = trackerClient.getGroupInfos();
			
			if (result.getCode() != Context.SUCCESS_CODE) {
				throw new Exception("Get getGroupInfos Error");
			}
			
			ArrayList<GroupInfo> groupInfos = result.getData();
			
			for (GroupInfo groupInfo : groupInfos) {
				Result<ArrayList<StorageInfo>> storageInfoResult = trackerClient.getStorageInfos(groupInfo.getGroupName());
				
				if (storageInfoResult.getCode() != 0) {
					continue;
				}
				
				ArrayList<StorageInfo> storageInfos = storageInfoResult.getData();
				
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
	 * 找到固定storage执行cmd
	 * 
	 * @param fileId
	 * @param callBack
	 * @return
	 * @throws Exception
	 */
	private <T extends Serializable> T fixedStorageExec(String fileId, StorageExecutor<T> executor) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		T t = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (updateStorageResult.getCode() != Context.SUCCESS_CODE) {
				return t;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			
			t = executor.exec(storageClient, fastDfsFile);
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
		
		return t;
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
}
