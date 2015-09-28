package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
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
	protected RequestContext getRequestContext() {
		byte[] fileSizeByte = ByteUtils.long2bytes(size);
		byte[] fileExtNameByte = extName != null ? extName.getBytes(Context.CHARSET) : new byte[0];
		byte[] params = new byte[15];
		
		int fileExtNameByteLen = fileExtNameByte.length;
		
		if (fileExtNameByteLen > Context.FDFS_FILE_EXT_NAME_MAX_LEN) {
			fileExtNameByteLen = Context.FDFS_FILE_EXT_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		params[0] = storePathIndex;
		
		System.arraycopy(fileSizeByte, 0, params, 1, fileSizeByte.length);
		System.arraycopy(fileExtNameByte, 0, params, fileSizeByte.length + 1, fileExtNameByteLen);
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_UPLOAD_FILE, params, inputStream, size);
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
	protected Result<String> callback(ResponseContext responseContext) throws FastdfsClientException {
		Result<String> result = new Result<String>(responseContext.getCode());
		
		if (!responseContext.isSuccess()) {
			result.setMessage("Upload Error");
			return result;
		}
		
		byte[] data = responseContext.getData();
		String group = new String(data, 0, Context.FDFS_GROUP_NAME_MAX_LEN).trim();
		String remoteFileName = new String(data, Context.FDFS_GROUP_NAME_MAX_LEN, data.length - Context.FDFS_GROUP_NAME_MAX_LEN);
		result.setData(group + "/" + remoteFileName);
		
		return result;
	}
}
