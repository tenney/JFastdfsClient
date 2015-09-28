package com.eiviv.fdfs.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

import com.eiviv.fdfs.exception.FastdfsClientException;
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
	private int trackerIndex = 0;
	
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
		Result<T> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception;
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
	 * @param inputStream 文件流
	 * @param size 文件大小
	 * @param extName 文件扩展名
	 * @param meta 元信息
	 * @return result
	 * @throws Exception
	 */
	public Result<String> upload(InputStream inputStream, long size, String extName, HashMap<String, String> meta) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		Result<String> uploadResult = null;
		
		try {
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<UploadStorage> result = trackerClient.getUploadStorage();
			
			if (!result.isSuccess()) {
				uploadResult = new Result<String>(result.getCode());
				uploadResult.setMessage("get upload storage faild");
				
				return uploadResult;
			}
			
			storageAddr = result.getData().getAddress();
			storageClient = storageClientPool.borrowObject(storageAddr);
			
			uploadResult = storageClient.upload(inputStream, size, extName, result.getData().getPathIndex());
			
			if (!uploadResult.isSuccess()) {
				return uploadResult;
			}
			
			if (meta != null) {
				setMeta(uploadResult.getData(), meta);
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
		
		return uploadResult;
	}
	
	/**
	 * 文件上传
	 * 
	 * @param file 文件
	 * @param extName 文件扩展名
	 * @param meta 元信息
	 * @return result
	 * @throws Exception
	 */
	public Result<String> upload(File file, String extName, HashMap<String, String> meta) throws Exception {
		return upload(new FileInputStream(file), file.length(), extName, meta);
	}
	
	/**
	 * 上传文件
	 * 
	 * @param file 文件
	 * @param extName 扩展名
	 * @return result
	 * @throws Exception
	 */
	public Result<String> upload(File file, String extName) throws Exception {
		return upload(file, extName, null);
	}
	
	/**
	 * 上传文件
	 * 
	 * @param file 文件
	 * @return result
	 * @throws Exception
	 */
	public Result<String> upload(File file) throws Exception {
		return upload(file, getFileExtName(file));
	}
	
	/**
	 * 上传文件副本
	 * 
	 * @param inputStream 文件输入流
	 * @param size 文件大小
	 * @param metaFileId 原fileId
	 * @param prefix 副本名后缀
	 * @param extName 扩展名
	 * @return result
	 * @throws Exception
	 */
	public Result<String> uploadSlave(final InputStream inputStream, final long size, String metaFileId, final String prefix, final String extName) throws Exception {
		return fixedStorageExec(metaFileId, new StorageExecutor<String>() {
			@Override
			public Result<String> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.uploadSlave(inputStream, size, fastDfsFile.fileName, prefix, extName, null);
			}
		});
	}
	
	/**
	 * 上传文件副本
	 * 
	 * @param file 文件
	 * @param fileid 原文件ID "group/remoteFileName"
	 * @param prefix 副本文件名后缀
	 * @param extName 扩展名
	 * @return result
	 * @throws Exception
	 */
	public Result<String> uploadSlave(final File file, String metaFileId, final String prefix, final String extName) throws Exception {
		return uploadSlave(new FileInputStream(file), file.length(), metaFileId, prefix, extName);
	}
	
	/**
	 * 断点上传
	 * 
	 * @param fileId
	 * @param fileByte
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> append(String fileId, final byte[] fileByte) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Result<Boolean> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.append(fastDfsFile.fileName, fileByte);
			}
		});
	}
	
	/**
	 * 删除文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> delete(String fileId) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Result<Boolean> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.delete(fastDfsFile.group, fastDfsFile.fileName);
			}
		});
	}
	
	/**
	 * 断点下载
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param os OutputStream
	 * @param offset 开始点
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> download(String fileId, final OutputStream os, final long offset) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Result<Boolean> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.download(fastDfsFile.group, fastDfsFile.fileName, os, offset);
			}
		});
	}
	
	/**
	 * 下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param os OutputStream
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> download(String fileId, OutputStream os) throws Exception {
		return download(fileId, os, 0);
	}
	
	/**
	 * 断点下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param localFile 本地文件
	 * @param offset 下载开始点
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> download(String fileId, File localFile, long offset) throws Exception {
		return download(fileId, new FileOutputStream(localFile), offset);
	}
	
	/**
	 * 下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param localFile 本地文件
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> download(String fileId, File localFile) throws Exception {
		return download(fileId, new FileOutputStream(localFile));
	}
	
	/**
	 * 断点下载文件
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param localFileName 本地文件名
	 * @param offset 下载开始点
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> download(String fileId, String localFileName, long offset) throws Exception {
		return download(fileId, new File(localFileName), offset);
	}
	
	/**
	 * 剪裁文件
	 * 
	 * @param fileId
	 * @param truncatedFileSize
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> truncate(String fileId, final long truncatedFileSize) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Result<Boolean> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.truncate(fastDfsFile.fileName, truncatedFileSize);
			}
		});
	}
	
	/**
	 * 设置元信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @param meta 元信息
	 * @return result
	 * @throws Exception
	 */
	public Result<Boolean> setMeta(String fileId, final HashMap<String, String> meta) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<Boolean>() {
			@Override
			public Result<Boolean> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.setMeta(fastDfsFile.group, fastDfsFile.fileName, meta);
			}
		});
	}
	
	/**
	 * 获取元信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return result
	 * @throws Exception
	 */
	public Result<HashMap<String, String>> getMeta(String fileId) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<HashMap<String, String>>() {
			@Override
			public Result<HashMap<String, String>> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.getMeta(fastDfsFile.group, fastDfsFile.fileName);
			}
		});
	}
	
	/**
	 * 获取上传文件信息
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return result
	 * @throws Exception
	 */
	public Result<FileInfo> getFileInfo(String fileId) throws Exception {
		return fixedStorageExec(fileId, new StorageExecutor<FileInfo>() {
			@Override
			public Result<FileInfo> exec(StorageClient storageClient, FastDfsFile fastDfsFile) throws Exception {
				return storageClient.getFileInfo(fastDfsFile.group, fastDfsFile.fileName);
			}
		});
	}
	
	/**
	 * 获取http下载地址
	 * 
	 * @param fileId "group/remoteFileName"
	 * @return result
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
			
			if (result.isSuccess()) {
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
	 * @return result
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
	 * @return result
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
	 * @return result
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
			
			if (!result.isSuccess()) {
				throw new Exception("Get getGroupInfos Error");
			}
			
			ArrayList<GroupInfo> groupInfos = result.getData();
			Result<ArrayList<StorageInfo>> storageInfoResult = null;
			ArrayList<StorageInfo> storageInfos = null;
			String hostPort = null;
			
			for (GroupInfo groupInfo : groupInfos) {
				storageInfoResult = trackerClient.getStorageInfos(groupInfo.getGroupName());
				
				if (storageInfoResult.getCode() != 0) {
					continue;
				}
				
				storageInfos = storageInfoResult.getData();
				
				for (StorageInfo storageInfo : storageInfos) {
					hostPort = storageInfo.getDomainName();
					
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
	 * @return result
	 * @throws Exception
	 */
	private <T extends Serializable> Result<T> fixedStorageExec(String fileId, StorageExecutor<T> executor) throws Exception {
		String trackerAddr = getTrackerAddr();
		TrackerClient trackerClient = null;
		StorageClient storageClient = null;
		String storageAddr = null;
		Result<T> execResult = null;
		
		try {
			FastDfsFile fastDfsFile = new FastDfsFile(fileId);
			trackerClient = trackerClientPool.borrowObject(trackerAddr);
			Result<String> updateStorageResult = trackerClient.getUpdateStorageAddr(fastDfsFile.group, fastDfsFile.fileName);
			
			if (!updateStorageResult.isSuccess()) {
				execResult = new Result<T>(updateStorageResult.getCode());
				execResult.setMessage("get update strorage address faild");
				
				return execResult;
			}
			
			storageAddr = updateStorageResult.getData();
			storageClient = storageClientPool.borrowObject(storageAddr);
			
			execResult = executor.exec(storageClient, fastDfsFile);
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
		
		return execResult;
	}
	
	/**
	 * 检验tracker服务是否可以连接
	 * 
	 * @param trackerAddr
	 * @return
	 */
	private boolean validateConnection(String trackerAddr) {
		String[] hostport = trackerAddr.split(":");
		String host = hostport[0];
		Integer port = Integer.valueOf(hostport[1]);
		
		Socket socket = new Socket();
		
		try {
			socket.setSoTimeout(10000);
			socket.connect(new InetSocketAddress(host, port), 10000);
			return true;
		} catch (SocketException e) {
			return false;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			} finally {
				socket = null;
			}
		}
	}
	
	/**
	 * 获取可用tracker server连接地址
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getTrackerAddr() throws Exception {
		int currIdx;
		
		synchronized (FastdfsClient.class) {
			
			if (trackerIndex >= trackerAddrs.size()) {
				trackerIndex = 0;
			}
			
			currIdx = trackerIndex;
		}
		
		if (validateConnection(trackerAddrs.get(currIdx))) {
			return trackerAddrs.get(currIdx);
		}
		
		for (int i = 0; i < trackerAddrs.size(); i++) {
			
			if (currIdx == i) {
				continue;
			}
			
			if (validateConnection(trackerAddrs.get(i))) {
				
				synchronized (FastdfsClient.class) {
					if (currIdx == trackerIndex) {
						trackerIndex = i;
					}
				}
				
				return trackerAddrs.get(i);
			}
		}
		
		throw new FastdfsClientException("can not connect all tracker server");
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
