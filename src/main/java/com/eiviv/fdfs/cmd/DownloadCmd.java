package com.eiviv.fdfs.cmd;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.model.Result;
import com.eiviv.fdfs.utils.ByteUtils;

public class DownloadCmd extends AbstractCmd<Boolean> {
	
	private String group;
	private String fileName;
	private long offset;
	private long size;
	private OutputStream os;
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 * @param os OutputStream
	 */
	public DownloadCmd(String group, String fileName, OutputStream os) {
		this.group = group;
		this.fileName = fileName;
		this.os = os;
	}
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 * @param os OutputSteam
	 * @param offset 下载开始点
	 */
	public DownloadCmd(String group, String fileName, OutputStream os, long offset) {
		this(group, fileName, os);
		this.offset = offset;
	}
	
	/**
	 * 实例化
	 * 
	 * @param group 组名
	 * @param fileName 文件名
	 * @param os OutputSteam
	 * @param offset 下载开始点
	 * @param size 要下载的长度
	 */
	public DownloadCmd(String group, String fileName, OutputStream os, long offset, long size) {
		this(group, fileName, os, offset);
		this.size = size;
	}
	
	@Override
	protected com.eiviv.fdfs.cmd.AbstractCmd.RequestBody getRequestBody() {
		byte[] offsetBytes = ByteUtils.long2bytes(offset);
		byte[] sizeBytes = ByteUtils.long2bytes(size);
		byte[] groupBytes = group.getBytes(Context.CHARSET);
		byte[] filenameBytes = fileName.getBytes(Context.CHARSET);
		int groupLen = groupBytes.length;
		
		if (groupLen > Context.FDFS_GROUP_NAME_MAX_LEN) {
			groupLen = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		byte[] body = new byte[offsetBytes.length + sizeBytes.length + Context.FDFS_GROUP_NAME_MAX_LEN + filenameBytes.length];
		
		Arrays.fill(body, (byte) 0);
		
		System.arraycopy(offsetBytes, 0, body, 0, offsetBytes.length);
		System.arraycopy(sizeBytes, 0, body, offsetBytes.length, sizeBytes.length);
		System.arraycopy(groupBytes, 0, body, offsetBytes.length + sizeBytes.length, groupLen);
		System.arraycopy(filenameBytes, 0, body, offsetBytes.length + sizeBytes.length + Context.FDFS_GROUP_NAME_MAX_LEN, filenameBytes.length);
		
		return new RequestBody(Context.STORAGE_PROTO_CMD_DOWNLOAD_FILE, body);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return os;
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
	protected Result<Boolean> callback(com.eiviv.fdfs.cmd.AbstractCmd.Response response) throws IOException {
		return new Result<Boolean>(response.getCode(), response.isSuccess());
	}
	
}
