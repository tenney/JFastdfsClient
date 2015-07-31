package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class QueryUpdateCmd extends AbstractCmd<String> {
	
	private String group;
	private String fileName;
	
	public QueryUpdateCmd(String group, String fileName) {
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
		
		return new RequestBody(Context.TRACKER_PROTO_CMD_SERVICE_QUERY_UPDATE, body);
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
		return Context.TRACKER_QUERY_STORAGE_FETCH_BODY_LEN;
	}
	
	@Override
	protected Result<String> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		Result<String> result = new Result<String>(response.getCode());
		String url = "";
		
		if (response.isSuccess()) {
			byte[] data = response.getData();
			String ip = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, Context.FDFS_IPADDR_SIZE - 1).trim();
			int port = (int) ByteUtils.bytes2long(data, Context.FDFS_GROUP_NAME_MAX_LEN + Context.FDFS_IPADDR_SIZE - 1);
			url = ip + ":" + String.valueOf(port);
		}
		
		result.setData(url);
		
		return result;
	}
	
}
