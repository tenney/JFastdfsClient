package com.eiviv.fdfs.cmd;

import java.io.OutputStream;
import java.util.Arrays;

import com.eiviv.fdfs.context.Context;
import com.eiviv.fdfs.exception.FastdfsClientException;
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
	protected RequestContext getRequestContext() {
		byte[] offsetByte = ByteUtils.long2bytes(offset);
		byte[] sizeByte = ByteUtils.long2bytes(size);
		byte[] groupByte = group.getBytes(Context.CHARSET);
		byte[] filenameByte = fileName.getBytes(Context.CHARSET);
		byte[] params = new byte[offsetByte.length + sizeByte.length + Context.FDFS_GROUP_NAME_MAX_LEN + filenameByte.length];
		
		int groupLen = groupByte.length;
		
		if (groupLen > Context.FDFS_GROUP_NAME_MAX_LEN) {
			groupLen = Context.FDFS_GROUP_NAME_MAX_LEN;
		}
		
		Arrays.fill(params, (byte) 0);
		
		System.arraycopy(offsetByte, 0, params, 0, offsetByte.length);
		System.arraycopy(sizeByte, 0, params, offsetByte.length, sizeByte.length);
		System.arraycopy(groupByte, 0, params, offsetByte.length + sizeByte.length, groupLen);
		System.arraycopy(filenameByte, 0, params, offsetByte.length + sizeByte.length + Context.FDFS_GROUP_NAME_MAX_LEN, filenameByte.length);
		
		return new RequestContext(Context.STORAGE_PROTO_CMD_DOWNLOAD_FILE, params);
	}
	
	@Override
	protected OutputStream getOutputStream() {
		return os;
	}
	
	@Override
	protected long getLongOfFixedResponseEntity() {
		return -1;
	}
	
	@Override
	protected Result<Boolean> callback(ResponseContext responseContext) throws FastdfsClientException {
		return new Result<Boolean>(responseContext.getCode(), responseContext.isSuccess());
	}
	
}
