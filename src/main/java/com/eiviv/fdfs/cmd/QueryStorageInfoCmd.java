package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.model.StorageInfo;

public class QueryStorageInfoCmd extends AbstractCmd<ArrayList<StorageInfo>> {
	
	private String group;
	private String ip;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 */
	public QueryStorageInfoCmd(String group) {
		this.group = group;
	}
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param ip IP
	 */
	public QueryStorageInfoCmd(String group, String ip) {
		this(group);
		this.ip = ip;
	}
	
	@Override
	protected RequestContext getRequestContext() {
		byte[] groupByte = group.getBytes(Context.CHARSET);
		byte[] ipByte = null;
		byte[] params = null;
		
		if (ip != null && ip.trim() != "") {
			ipByte = ip.getBytes(Context.CHARSET);
			params = new byte[Context.FDFS_GROUP_NAME_MAX_LEN + ipByte.length];
		} else {
			params = new byte[Context.FDFS_GROUP_NAME_MAX_LEN];
		}
		
		int groupLen;
		
		if (groupByte.length <= Context.FDFS_GROUP_NAME_MAX_LEN) {
			groupLen = groupByte.length;
		} else {
			groupLen = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		System.arraycopy(groupByte, 0, params, 0, groupLen);
		
		if (ipByte != null) {
			System.arraycopy(ipByte, 0, params, Context.FDFS_GROUP_NAME_MAX_LEN, ipByte.length);
		}
		
		return new RequestContext(Context.TRACKER_PROTO_CMD_SERVER_LIST_STORAGE, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return -1;
	}
	
	@Override
	protected Result<ArrayList<StorageInfo>> callback(ResponseContext responseContext) throws FastdfsClientException {
		
		if (!responseContext.isSuccess()) {
			return new Result<ArrayList<StorageInfo>>(responseContext.getCode(), "Error");
		}
		
		byte[] data = responseContext.getData();
		int dataLength = data.length;
		
		if (dataLength % StorageInfo.BYTE_SIZE != 0) {
			throw new FastdfsClientException("recv body length: " + data.length + " is not correct");
		}
		
		ArrayList<StorageInfo> storageInfos = new ArrayList<StorageInfo>();
		int offset = 0;
		
		while (offset < dataLength) {
			StorageInfo storageInfo = new StorageInfo(data, offset);
			storageInfos.add(storageInfo);
			offset += StorageInfo.BYTE_SIZE;
		}
		
		return new Result<ArrayList<StorageInfo>>(responseContext.getCode(), storageInfos);
	}
	
}
