package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class QueryDownloadCmd extends AbstractCmd<String> {
	
	private String group;
	private String fileName;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 */
	public QueryDownloadCmd(String group, String fileName) {
		this.group = group;
		this.fileName = fileName;
	}
	
	@Override
	protected RequestContext getRequestContext() {
		byte[] groupByte = group.getBytes(Context.CHARSET);
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] params = new byte[Context.FDFS_GROUP_NAME_MAX_LEN + fileNameByte.length];
		
		int groupLen = groupByte.length;
		
		if (groupLen > Context.FDFS_GROUP_NAME_MAX_LEN) {
			groupLen = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		System.arraycopy(groupByte, 0, params, 0, groupLen);
		System.arraycopy(fileNameByte, 0, params, Context.FDFS_GROUP_NAME_MAX_LEN, fileNameByte.length);
		
		return new RequestContext(Context.TRACKER_PROTO_CMD_SERVICE_QUERY_FETCH_ONE, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return Context.TRACKER_QUERY_STORAGE_FETCH_BODY_LEN;
	}
	
	@Override
	protected Result<String> callback(ResponseContext responseContext) {
		Result<String> result = new Result<String>(responseContext.getCode());
		
		if (!responseContext.isSuccess()) {
			return result;
		}
		
		byte[] data = responseContext.getData();
		String ip = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, Context.FDFS_IPADDR_SIZE - 1).trim();
		int port = (int) ByteUtils.bytes2long(data, Context.FDFS_GROUP_NAME_MAX_LEN + Context.FDFS_IPADDR_SIZE - 1);
		String url = ip + ":" + String.valueOf(port);
		
		result.setData(url);
		
		return result;
	}
	
}
