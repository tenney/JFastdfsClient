package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
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
	protected RequestContext getRequestContext() {
		byte requestCmdCode = Context.TRACKER_PROTO_CMD_SERVICE_QUERY_STORE_WITHOUT_GROUP_ONE;
		
		if (group == null || group.trim() == "") {
			return new RequestContext(requestCmdCode);
		}
		
		byte[] bs = group.getBytes(Context.CHARSET);
		byte[] params = new byte[Context.FDFS_GROUP_NAME_MAX_LEN];
		
		int groupLen;
		
		if (bs.length <= Context.FDFS_GROUP_NAME_MAX_LEN) {
			groupLen = bs.length;
		} else {
			groupLen = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		System.arraycopy(bs, 0, params, 0, groupLen);
		
		return new RequestContext(requestCmdCode, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return new ByteArrayOutputStream();
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return Context.TRACKER_QUERY_STORAGE_STORE_BODY_LEN;
	}
	
	@Override
	protected Result<UploadStorage> callback(ResponseContext responseContext) {
		Result<UploadStorage> result = new Result<UploadStorage>(responseContext.getCode());
		
		if (!responseContext.isSuccess()) {
			return result;
		}
		
		byte[] data = responseContext.getData();
		String ip_addr = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, Context.FDFS_IPADDR_SIZE - 1).trim();
		int port = (int) ByteUtils.bytes2long(data, Context.FDFS_GROUP_NAME_MAX_LEN + Context.FDFS_IPADDR_SIZE - 1);
		byte storePath = data[Context.TRACKER_QUERY_STORAGE_STORE_BODY_LEN - 1];
		UploadStorage uploadStorage = new UploadStorage(ip_addr + ":" + String.valueOf(port), storePath);
		
		result.setData(uploadStorage);
		
		return result;
	}
	
}
