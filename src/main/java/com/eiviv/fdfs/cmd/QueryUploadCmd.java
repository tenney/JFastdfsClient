package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.model.UploadStorage;
import com.eiviv.fdfs.utils.ByteUtils;

public class QueryUploadCmd extends AbstractCmd<UploadStorage> {
	
	private String group;
	
	/**
	 * 实例化
	 */
	public QueryUploadCmd() {
	}
	
	/**
	 * 实例化
	 * 
	 * @param group
	 */
	public QueryUploadCmd(String group) {
		this.group = group;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte requestCmdCode = Context.TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE;
		
		if (group == null || group.trim() == "") {
			return new RequestBody(requestCmdCode);
		}
		
		byte[] bs = group.getBytes(Context.CHARSET);
		byte[] body = new byte[Context.FDFS_GROUP_NAME_MAX_LEN];
		
		int group_len;
		
		if (bs.length <= Context.FDFS_GROUP_NAME_MAX_LEN) {
			group_len = bs.length;
		} else {
			group_len = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(body, (byte) 0);
		System.arraycopy(bs, 0, body, 0, group_len);
		
		return new RequestBody(requestCmdCode, body);
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
		return Context.TRACKER_QUERY_STORAGE_STORE_BODY_LEN;
	}
	
	@Override
	protected Result<UploadStorage> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		
		if (!response.isSuccess()) {
			return new Result<UploadStorage>(response.getCode(), "Error");
		}
		
		byte[] data = response.getData();
		String ip_addr = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, Context.FDFS_IPADDR_SIZE - 1).trim();
		int port = (int) ByteUtils.bytes2long(data, Context.FDFS_GROUP_NAME_MAX_LEN + Context.FDFS_IPADDR_SIZE - 1);
		byte storePath = data[Context.TRACKER_QUERY_STORAGE_STORE_BODY_LEN - 1];
		UploadStorage uploadStorage = new UploadStorage(ip_addr + ":" + String.valueOf(port), storePath);
		
		return new Result<UploadStorage>(response.getCode(), uploadStorage);
	}
	
}
