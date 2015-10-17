package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;

public class DeleteCmd extends AbstractCmd<Boolean> {
	
	private String group;
	private String fileName;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 */
	public DeleteCmd(String group, String fileName) {
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
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_DELETE_FILE, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return 0;
	}
	
	@Override
	protected Result<Boolean> callback(ResponseContext responseContext) {
		return new Result<Boolean>(responseContext.getCode(), responseContext.isSuccess());
	}
	
}
