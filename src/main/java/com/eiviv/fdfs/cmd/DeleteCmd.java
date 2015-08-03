package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_DELETE_FILE, body);
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
		return 0;
	}
	
	@Override
	protected Result<Boolean> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		return new Result<Boolean>(response.getCode(), response.isSuccess());
	}
	
}
