package com.eiviv.fdfs.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import com.eiviv.fdfs.cmd.CloseCmd;
import com.eiviv.fdfs.cmd.Cmd;
import com.eiviv.fdfs.cmd.QueryDownloadCmd;
import com.eiviv.fdfs.cmd.QueryGroupInfoCmd;
import com.eiviv.fdfs.cmd.QueryStorageInfoCmd;
import com.eiviv.fdfs.cmd.QueryUpdateCmd;
import com.eiviv.fdfs.cmd.QueryUploadCmd;
import com.eiviv.fdfs.config.FastdfsClientConfig;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.GroupInfo;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.model.StorageInfo;
import com.eiviv.fdfs.model.UploadStorage;

public class TrackerClient extends AbstractClient {
	
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
	public TrackerClient(String address) {
		String[] hostport = address.split(":");
		
		this.host = hostport[0];
		this.port = Integer.valueOf(hostport[1]);
	}
	
	/**
	 * 实例化
	 * 
	 * @param address "host:port"
	 * @param connectTimeout 连接超时时间(秒)
	 * @param networkTimeout 传输超时时间(秒)
	 */
	public TrackerClient(String address, Integer connectTimeout, Integer networkTimeout) {
		this(address);
		
		this.connectTimeout = connectTimeout;
		this.networkTimeout = networkTimeout;
	}
	
	/**
	 * 获取上传 storage
	 * 
	 * @return
	 * @throws FastdfsClientException
	 */
	public Result<UploadStorage> getUploadStorage() throws FastdfsClientException {
		Cmd<UploadStorage> command = new QueryUploadCmd();
		
		return command.exec(getSocket());
	}
	
	/**
	 * 获取更新 storage 地址
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 * @return 更新 storage 地址
	 * @throws FastdfsClientException
	 */
	public Result<String> getUpdateStorageAddr(String group, String fileName) throws FastdfsClientException {
		Cmd<String> cmd = new QueryUpdateCmd(group, fileName);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 获取下载 storage 地址
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 * @return 下载 storage 地址
	 * @throws FastdfsClientException
	 */
	public Result<String> getDownloadStorageAddr(String group, String fileName) throws FastdfsClientException {
		Cmd<String> cmd = new QueryDownloadCmd(group, fileName);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 获取组信息
	 * 
	 * @return 组信息
	 * @throws FastdfsClientException
	 */
	public Result<ArrayList<GroupInfo>> getGroupInfos() throws FastdfsClientException {
		Cmd<ArrayList<GroupInfo>> cmd = new QueryGroupInfoCmd();
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 获取 StorageInfos
	 * 
	 * @param group 组名
	 * @return StorageInfo 集合
	 * @throws FastdfsClientException
	 */
	public Result<ArrayList<StorageInfo>> getStorageInfos(String group) throws FastdfsClientException {
		Cmd<ArrayList<StorageInfo>> cmd = new QueryStorageInfoCmd(group);
		
		return cmd.exec(getSocket());
	}
	
	/**
	 * 关闭socket
	 * 
	 * @throws FastdfsClientException
	 */
	public void close() throws FastdfsClientException {
		Socket socket = getSocket();
		Cmd<Boolean> cmd = new CloseCmd();
		cmd.exec(socket);
		try {
			socket.close();
		} catch (IOException e) {
			throw new FastdfsClientException(e);
		}
		socket = null;
	}
	
	/**
	 * 获取socket
	 * 
	 * @return
	 * @throws FastdfsClientException
	 */
	private Socket getSocket() throws FastdfsClientException {
		
		if (socket == null) {
			socket = new Socket();
			
			try {
				socket.setSoTimeout(networkTimeout);
				socket.connect(new InetSocketAddress(host, port), connectTimeout);
			} catch (SocketException e) {
				throw new FastdfsClientException(e);
			} catch (IOException e) {
				throw new FastdfsClientException(e);
			}
		}
		
		return socket;
	}
}
