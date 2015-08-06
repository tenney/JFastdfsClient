package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class TruncateFileCmd extends AbstractCmd<Boolean> {
	
	private String fileName;
	private long truncatedFileSize;
	
	/**
	 * 实例化
	 * 
	 * @param fileName remoteFileName
	 * @param truncatedFileSize 裁剪后的大小
	 */
	public TruncateFileCmd(String fileName, long truncatedFileSize) {
		this.fileName = fileName;
		this.truncatedFileSize = truncatedFileSize;
	}
	
	@Override
	protected RequestContext getRequestContext() {
		byte[] truncatedFileSizeByte = ByteUtils.long2bytes(truncatedFileSize);
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] fileNameLenByte = ByteUtils.long2bytes(fileName.length());
		byte[] params = new byte[2 * Context.FDFS_PROTO_PKG_LEN_SIZE + fileNameByte.length];
		
		Arrays.fill(params, (byte) 0);
		
		System.arraycopy(fileNameLenByte, 0, params, 0, fileNameLenByte.length);
		System.arraycopy(truncatedFileSizeByte, 0, params, fileNameLenByte.length, truncatedFileSizeByte.length);
		System.arraycopy(fileNameByte, 0, params, fileNameLenByte.length + truncatedFileSizeByte.length, fileNameByte.length);
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_TRUNCATE_FILE, params);
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
	protected Result<Boolean> callback(ResponseContext responseContext) throws FastdfsClientException {
		return new Result<Boolean>(responseContext.getCode(), responseContext.isSuccess());
	}
	
}
