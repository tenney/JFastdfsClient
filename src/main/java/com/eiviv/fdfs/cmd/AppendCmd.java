package com.eiviv.fdfs.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
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
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] fileSizeByte = ByteUtils.long2bytes(fileByte.length);
		byte[] fileNameByte = fileName.getBytes(Context.CHARSET);
		byte[] fileNameLenByte = ByteUtils.long2bytes(fileName.length());
		
		byte[] body = new byte[fileSizeByte.length + fileNameByte.length + fileNameLenByte.length];
		
		Arrays.fill(body, (byte) 0);
		
		System.arraycopy(fileNameLenByte, 0, body, 0, fileNameLenByte.length);
		System.arraycopy(fileSizeByte, 0, body, fileNameLenByte.length, fileSizeByte.length);
		System.arraycopy(fileNameByte, 0, body, fileNameLenByte.length + fileSizeByte.length, fileNameByte.length);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_APPEND_FILE, body, fileByte);
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
