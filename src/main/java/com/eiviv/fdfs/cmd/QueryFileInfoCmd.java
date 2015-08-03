package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
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
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] groupByte = group.getBytes(Context.CHARSET);
		int group_len = groupByte.length;
		
		if (group_len > Context.FDFS_GROUP_NAME_MAX_LEN) {
			group_len = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] body = new byte[Context.FDFS_GROUP_NAME_MAX_LEN + fileNameByte.length];
		
		Arrays.fill(body, (byte) 0);
		
		System.arraycopy(groupByte, 0, body, 0, group_len);
		System.arraycopy(fileNameByte, 0, body, Context.FDFS_GROUP_NAME_MAX_LEN, fileNameByte.length);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_QUERY_FILE_INFO, body);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected byte getResponseCmdCode() {
		return Context.STORAGE_PROTO_CMD_RESP;
	}
	
	@Override
	protected long getFixedBodyLength() {
		return 3 * Context.FDFS_PROTO_PKG_LEN_SIZE + Context.FDFS_IPADDR_SIZE;
	}
	
	@Override
	protected Result<FileInfo> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		FileInfo fileInfo = null;
		
		if (!response.isSuccess()) {
			return new Result<FileInfo>(response.getCode(), fileInfo);
		}
		
		byte[] data = response.getData();
		long fileSize = ByteUtils.bytes2long(data, 0);
		int createTime = (int) ByteUtils.bytes2long(data, Context.FDFS_PROTO_PKG_LEN_SIZE);
		int crc32 = (int) ByteUtils.bytes2long(data, 2 * Context.FDFS_PROTO_PKG_LEN_SIZE);
		String ip = (new String(data, 3 * Context.FDFS_PROTO_PKG_LEN_SIZE, Context.FDFS_IPADDR_SIZE)).trim();
		
		return new Result<FileInfo>(response.getCode(), new FileInfo(fileSize, createTime, crc32, ip));
	}
	
}
