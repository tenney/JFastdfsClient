package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class UploadSlaveCmd extends AbstractCmd<String> {
	
	private InputStream inputStream;
	private long size;
	private String fileId;
	private String prefix;
	private String extName;
	
	/**
	 * 实例化
	 * 
	 * @param file 文件
	 * @param fileId 原文件fileId
	 * @param prefix 副本文件名后缀
	 * @param ext 扩展名
	 */
	public UploadSlaveCmd(InputStream inputStream, long size, String fileId, String prefix, String extName) {
		this.inputStream = inputStream;
		this.size = size;
		this.fileId = fileId;
		this.prefix = prefix;
		this.extName = extName;
	}
	
	@Override
	protected RequestContext getRequestContext() {
		byte[] fileIdNameByte = ByteUtils.long2bytes(fileId.length());
		byte[] fileSizeLenByte = ByteUtils.long2bytes(size);
		byte[] prefixByte = prefix != null ? prefix.getBytes(Context.CHARSET) : new byte[0];
		byte[] fileExtNameByte = extName != null ? extName.getBytes(Context.CHARSET) : new byte[0];
		byte[] fileIdByte = fileId.getBytes(Context.CHARSET);
		byte[] params = new byte[2 * Context.FDFS_PROTO_PKG_LEN_SIZE + Context.FDFS_FILE_PREFIX_MAX_LEN + Context.FDFS_FILE_EXT_NAME_MAX_LEN
				+ fileIdByte.length];
		
		int fileExtNameByteLen = fileExtNameByte.length;
		
		if (fileExtNameByteLen > Context.FDFS_FILE_EXT_NAME_MAX_LEN) {
			fileExtNameByteLen = Context.FDFS_FILE_EXT_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		int pos = 0;
		System.arraycopy(fileIdNameByte, 0, params, pos, fileIdNameByte.length);
		pos += Context.FDFS_PROTO_PKG_LEN_SIZE;
		System.arraycopy(fileSizeLenByte, 0, params, pos, fileSizeLenByte.length);
		pos += Context.FDFS_PROTO_PKG_LEN_SIZE;
		System.arraycopy(prefixByte, 0, params, pos, prefixByte.length);
		pos += Context.FDFS_FILE_PREFIX_MAX_LEN;
		System.arraycopy(fileExtNameByte, 0, params, pos, fileExtNameByteLen);
		pos += Context.FDFS_FILE_EXT_NAME_MAX_LEN;
		System.arraycopy(fileIdByte, 0, params, pos, fileIdByte.length);
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_UPLOAD_SLAVE_FILE, params, inputStream, size);
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
