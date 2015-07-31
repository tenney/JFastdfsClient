package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.model.StorageInfo;

public class QueryStorageInfoCmd extends AbstractCmd<List<StorageInfo>> {
	
	private String group;
	private String ip;
	
	public QueryStorageInfoCmd(String group) {
		this.group = group;
	}
	
	public QueryStorageInfoCmd(String group, String ip) {
		this(group);
		this.ip = ip;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		
		byte[] groupByte = group.getBytes(Context.CHARSET);
		byte[] ipByte = null;
		byte[] body = null;
		
		if (ip != null && ip.trim() != "") {
			ipByte = ip.getBytes(Context.CHARSET);
			body = new byte[Context.FDFS_GROUP_NAME_MAX_LEN + ipByte.length];
		} else {
			body = new byte[Context.FDFS_GROUP_NAME_MAX_LEN];
		}
		
		int group_len;
		
		if (groupByte.length <= Context.FDFS_GROUP_NAME_MAX_LEN) {
			group_len = groupByte.length;
		} else {
			group_len = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(body, (byte) 0);
		System.arraycopy(groupByte, 0, body, 0, group_len);
		
		if (ipByte != null) {
			System.arraycopy(ipByte, 0, body, Context.FDFS_GROUP_NAME_MAX_LEN, ipByte.length);
		}
		
		return new RequestBody(Context.TRACKER_PROTO_CMD_SERVER_LIST_STORAGE, body);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected byte getResponseCmdCode() {
		return Context.TRACKER_PROTO_CMD_RESP;
	}
	
	@Override
	protected long getFixedBodyLength() {
		return -1;
	}
	
	@Override
	protected Result<List<StorageInfo>> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		
		if (!response.isSuccess()) {
			return new Result<List<StorageInfo>>(response.getCode(), "Error");
		}
		
		byte[] data = response.getData();
		int dataLength = data.length;
		
		if (dataLength % StorageInfo.BYTE_SIZE != 0) {
			throw new IOException("recv body length: " + data.length + " is not correct");
		}
		
		List<StorageInfo> storageInfos = new ArrayList<StorageInfo>();
		int offset = 0;
		
		while (offset < dataLength) {
			StorageInfo storageInfo = new StorageInfo(data, offset);
			storageInfos.add(storageInfo);
			offset += StorageInfo.BYTE_SIZE;
		}
		
		return new Result<List<StorageInfo>>(response.getCode(), storageInfos);
	}
	
}
