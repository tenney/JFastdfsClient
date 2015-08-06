package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;

public class QueryMetaDataCmd extends AbstractCmd<HashMap<String, String>> {
	
	private String group;
	private String fileName;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 */
	public QueryMetaDataCmd(String group, String fileName) {
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
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_GET_METADATA, params);
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
	protected Result<HashMap<String, String>> callback(ResponseContext responseContext) throws FastdfsClientException {
		HashMap<String, String> metaData = null;
		
		if (!responseContext.isSuccess()) {
			return new Result<HashMap<String, String>>(responseContext.getCode(), metaData);
		}
		
		String metaStr = new String(responseContext.getData(), Context.CHARSET);
		String[] rows = metaStr.split(Context.FDFS_RECORD_SEPERATOR);
		metaData = new HashMap<String, String>();
		
		for (String row : rows) {
			String[] cols = row.split(Context.FDFS_FIELD_SEPERATOR);
			metaData.put(cols[0], cols[1]);
		}
		
		return new Result<HashMap<String, String>>(responseContext.getCode(), metaData);
	}
	
}
