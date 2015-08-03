package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class TruncateFileCmd extends AbstractCmd<Boolean> {
	
	private String fileName;
	private long truncatedFileSize;
	
	public TruncateFileCmd(String fileName, long truncatedFileSize) {
		this.fileName = fileName;
		this.truncatedFileSize = truncatedFileSize;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] truncatedFileSizeByte = ByteUtils.long2bytes(truncatedFileSize);
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] fileNameLenByte = ByteUtils.long2bytes(fileName.length());
		
		byte[] body = new byte[2 * Context.FDFS_PROTO_PKG_LEN_SIZE + fileNameByte.length];
		
		Arrays.fill(body, (byte) 0);
		
		System.arraycopy(fileNameLenByte, 0, body, 0, fileNameLenByte.length);
		System.arraycopy(truncatedFileSizeByte, 0, body, fileNameLenByte.length, truncatedFileSizeByte.length);
		System.arraycopy(fileNameByte, 0, body, fileNameLenByte.length + truncatedFileSizeByte.length, fileNameByte.length);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_TRUNCATE_FILE, body);
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
