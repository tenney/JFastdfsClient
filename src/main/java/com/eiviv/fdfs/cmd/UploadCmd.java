package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class UploadCmd extends AbstractCmd<String> {
	
	private byte storePathIndex;
	private InputStream inputStream;
	private long size;
	private String extName;
	
	/**
	 * 实例化
	 * 
	 * @param inputStream
	 * @param extName
	 * @param storePathIndex
	 */
	public UploadCmd(InputStream inputStream, long size, String extName, byte storePathIndex) {
		this.inputStream = inputStream;
		this.size = size;
		this.extName = extName;
		this.storePathIndex = storePathIndex;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] body = new byte[15];
		Arrays.fill(body, (byte) 0);
		body[0] = storePathIndex;
		byte[] fileSizeByte = ByteUtils.long2bytes(size);
		byte[] fileExtNameByte = extName != null ? extName.getBytes(Context.CHARSET) : new byte[0];
		int fileExtNameByteLen = fileExtNameByte.length;
		
		if (fileExtNameByteLen > Context.FDFS_FILE_EXT_NAME_MAX_LEN) {
			fileExtNameByteLen = Context.FDFS_FILE_EXT_NAME_MAX_LEN;
		}
		
		System.arraycopy(fileSizeByte, 0, body, 1, fileSizeByte.length);
		System.arraycopy(fileExtNameByte, 0, body, fileSizeByte.length + 1, fileExtNameByteLen);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_UPLOAD_FILE, body, inputStream, size);
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
		return -1;
	}
	
	@Override
	protected Result<String> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		Result<String> result = new Result<String>(response.getCode());
		
		if (!response.isSuccess()) {
			result.setMessage("Error");
			return result;
		}
		
		byte[] data = response.getData();
		String group = new String(data, 0, Context.FDFS_GROUP_NAME_MAX_LEN).trim();
		String remoteFileName = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, data.length - Context.FDFS_GROUP_NAME_MAX_LEN);
		result.setData(group + "/" + remoteFileName);
		
		return result;
	}
}
