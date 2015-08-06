package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.FileInfo;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class QueryFileInfoCmd extends AbstractCmd<FileInfo> {
	
	private String group;
	private String fileName;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName remoteFileName
	 */
	public QueryFileInfoCmd(String group, String fileName) {
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
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_QUERY_FILE_INFO, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return 3 * Context.FDFS_PROTO_PKG_LEN_SIZE + Context.FDFS_IPADDR_SIZE;
	}
	
	@Override
	protected Result<FileInfo> callback(ResponseContext responseContext) throws FastdfsClientException {
		FileInfo fileInfo = null;
		
		if (!responseContext.isSuccess()) {
			return new Result<FileInfo>(responseContext.getCode(), fileInfo);
		}
		
		byte[] data = responseContext.getData();
		long fileSize = ByteUtils.bytes2long(data, 0);
		int createTime = (int) ByteUtils.bytes2long(data, Context.FDFS_PROTO_PKG_LEN_SIZE);
		int crc32 = (int) ByteUtils.bytes2long(data, 2 * Context.FDFS_PROTO_PKG_LEN_SIZE);
		String ip = (new String(data, 3 * Context.FDFS_PROTO_PKG_LEN_SIZE, Context.FDFS_IPADDR_SIZE)).trim();
		
		return new Result<FileInfo>(responseContext.getCode(), new FileInfo(fileSize, createTime, crc32, ip));
	}
	
}
