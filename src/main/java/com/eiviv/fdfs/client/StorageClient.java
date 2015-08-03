package com.eiviv.fdfs.client;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;

import com.eiviv.fdfs.cmd.ActiveTestCmd;
import com.eiviv.fdfs.cmd.CloseCmd;
import com.eiviv.fdfs.cmd.Cmd;
import com.eiviv.fdfs.cmd.DeleteCmd;
import com.eiviv.fdfs.cmd.DownloadCmd;
import com.eiviv.fdfs.cmd.QueryMetaDataCmd;
import com.eiviv.fdfs.cmd.UpdateMetaDataCmd;
import com.eiviv.fdfs.cmd.UploadCmd;
import com.eiviv.fdfs.cmd.UploadSlaveCmd;
import com.eiviv.fdfs.config.FastdfsClientConfig;
import com.eiviv.fdfs.model.Result;

public class StorageClient extends AbstractClient {
	
	private Integer connectTimeout = FastdfsClientConfig.DEFAULT_CONNECT_TIMEOUT * 1000;
	private Integer networkTimeout = FastdfsClientConfig.DEFAULT_NETWORK_TIMEOUT * 1000;
	private Socket socket;
	private String host;
	private Integer port;
	
	/**
	 * 实例化
	 * 
	 * @param address "host:port"
	 */
	public StorageClient(String address) {
		String[] hostport = address.split(":");
		
		this.host = hostport[0];
		this.port = Integer.valueOf(hostport[1]);
	}
	
	/**
	 * 实例化
	 * 
	 * @param address "host:port"
	 * @param connectTimeout 连接server超时(秒)
	 * @param networkTimeout 传输超时(秒)
	 */
	public StorageClient(String address, Integer connectTimeout, Integer networkTimeout) {
		this(address);
		
		this.connectTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
	}
	
	/**
	 * 上传副本
	 * 
	 * @param file 文件
	 * @param fileId "group/remoteFileName"
	 * @param slavePrefix 副本名后缀
	 * @param ext 扩展名
	 * @param meta 元信息
	 * @return Result
	 * @throws IOException
	 */
	public Result<String> uploadSlave(File file, String fileId, String slavePrefix, String ext, Map<String, String> meta) throws IOException {
		Cmd<String> cmd = new UploadSlaveCmd(file, fileId, slavePrefix, ext);
		Result<String> result = cmd.exec(getSocket());
		
		if (meta != null) {
			String[] tupple = splitFileId(fileId);
			
			if (tupple != null) {
				String group = tupple[0];
				String fileName = tupple[1];
				setMeta(group, fileName, meta);
			}
		}
		
		return result;
	}
	
	/**
	 * 上传文件
	 * 
	 * @param file 文件
	 * @param extName 扩展名
	 * @param storePathIndex 存储地址
	 * @return fileId
	 * @throws IOException
	 */
	public Result<String> upload(File file, String extName, byte storePathIndex) throws IOException {
		Cmd<String> cmd = new UploadCmd(file, extName, storePathIndex);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 删除文件
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 * @return boolean
	 * @throws IOException
	 */
	public Result<Boolean> delete(String group, String fileName) throws IOException {
		Cmd<Boolean> cmd = new DeleteCmd(group, fileName);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 设置元信息
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 * @param meta 元信息
	 * @return boolean
	 * @throws IOException
	 */
	public Result<Boolean> setMeta(String group, String fileName, Map<String, String> meta) throws IOException {
		Cmd<Boolean> cmd = new UpdateMetaDataCmd(group, fileName, meta);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 获取元信息
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 * @return map 元信息
	 * @throws IOException
	 */
	public Result<Map<String, String>> getMeta(String group, String fileName) throws IOException {
		Cmd<Map<String, String>> cmd = new QueryMetaDataCmd(group, fileName);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 下载文件
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 * @param os OutputStream
	 * @return boolean
	 * @throws IOException
	 */
	public Result<Boolean> download(String group, String fileName, OutputStream os) throws IOException {
		return download(group, fileName, os, 0);
	}
	
	/**
	 * 断点下载文件
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 * @param os OutputStream
	 * @param offset 下载开始点
	 * @return boolean
	 * @throws IOException
	 */
	public Result<Boolean> download(String group, String fileName, OutputStream os, long offset) throws IOException {
		return download(group, fileName, os, offset, 0);
	}
	
	/**
	 * 断点下载文件
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 * @param os OutputStream
	 * @param offset 下载开始点
	 * @param size 要下载的长度
	 * @return boolean
	 * @throws IOException
	 */
	public Result<Boolean> download(String group, String fileName, OutputStream os, long offset, long size) throws IOException {
		Cmd<Boolean> cmd = new DownloadCmd(group, fileName, os, offset, size);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 判断socket是否已经关闭
	 * 
	 * @return
	 */
	public boolean isClosed() {
		
		if (this.socket == null) {
			return true;
		}
		
		if (this.socket.isClosed()) {
			return true;
		}
		
		ActiveTestCmd atcmd = new ActiveTestCmd();
		
		try {
			Result<Boolean> result = atcmd.exec(getSocket());
			
			if (result.getData()) {
				return false;
			}
		} catch (IOException e) {
		}
		
		return true;
	}
	
	/**
	 * 关闭socket
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		Socket socket = getSocket();
		Cmd<Boolean> cmd = new CloseCmd();
		cmd.exec(socket);
		socket.close();
		socket = null;
	}
	
	/**
	 * 获取socket
	 * 
	 * @return
	 * @throws IOException
	 */
	private Socket getSocket() throws IOException {
		
		if (socket == null) {
			socket = new Socket();
			socket.setSoTimeout(networkTimeout);
			socket.connect(new InetSocketAddress(host, port), connectTimeout);
		}
		
		return socket;
	}
}
