package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class AppendCmd extends AbstractCmd<Boolean> {
	
	private String fileName;
	private byte[] fileByte;
	
	/**
	 * 实例化
	 * 
	 * @param fileName 文件名 remoteFileName
	 * @param fileByte 文件字节流
	 */
	public AppendCmd(String fileName, byte[] fileByte) {
		this.fileName = fileName;
		this.fileByte = fileByte;
	}
	
	@Override
	protected RequestContext getRequestContext() {
		byte[] fileNameLenByte = ByteUtils.long2bytes(fileName.length());
		byte[] fileSizeByte = ByteUtils.long2bytes(fileByte.length);
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] params = new byte[2 * Context.FDFS_PROTO_PKG_LEN_SIZE + fileNameByte.length];
		
		Arrays.fill(params, (byte) 0);
		
		System.arraycopy(fileNameLenByte, 0, params, 0, fileNameLenByte.length);
		System.arraycopy(fileSizeByte, 0, params, fileNameLenByte.length, fileSizeByte.length);
		System.arraycopy(fileNameByte, 0, params, fileNameLenByte.length + fileSizeByte.length, fileNameByte.length);
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_APPEND_FILE, params, fileByte);
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
